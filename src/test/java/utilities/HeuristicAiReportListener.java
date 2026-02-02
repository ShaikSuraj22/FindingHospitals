
package utilities;



import com.google.genai.types.Schema;

import io.qameta.allure.Allure;
import org.openqa.selenium.*;
import org.testng.*;
import org.testng.xml.XmlSuite;

import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.*;

// Gemini SDK + JSON
import com.google.genai.Client;
import com.google.genai.types.GenerateContentConfig;
import com.google.genai.types.GenerateContentResponse;
import com.google.genai.types.HttpOptions; // <-- added for baseUrl override
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class HeuristicAiReportListener implements ITestListener, ISuiteListener {

    private static class TestRecord {
        String name;
        String className;
        String status; // PASSED / FAILED / SKIPPED
        String url;
        String error;
        String screenshotPath;
        String lastLogs;
        String diagnosis;      // AI-style root cause
        String howToFix;       // concrete steps
        String suggestion;     // locator/wait snippet
        String sanity;         // extra validations to add
        String copilotPrompt;  // prompt text you can paste into Copilot Chat
    }

    private final List<TestRecord> records = Collections.synchronizedList(new ArrayList<>());
    private String logFilePath;
    private int logTailLines;

    // --- Gemini wiring ---
    private static volatile Client geminiClient; // lazy-initialized once
    private static final String GEMINI_MODEL = "gemini-2.5-flash";
    private static final ObjectMapper JSON = new ObjectMapper();

    // (Optional) Local override for quick, throwaway testing ONLY. Never commit real keys.
    // Prefer setting GOOGLE_API_KEY in IntelliJ Run/Debug Configuration -> Environment variables.
    private static final String OVERRIDE_API_KEY = null; // e.g., "YOUR_API_KEY_HERE" (temporary local test)

    // Developer API base endpoint (explicit override). By default the SDK chooses the right endpoint,
    // but per your request we set it explicitly.
    // Ref: Google Gen AI Java SDK HttpOptions supports baseUrl/apiVersion customization.  [1](https://support.google.com/gemini/thread/348900199/need-to-update-api-key-in-envirmonetal-settings?hl=en)
    private static final String GEMINI_BASE_URL = "https://generativelanguage.googleapis.com";

    // Optionally pin API version used by the SDK HTTP layer (e.g., "v1" or "v1alpha").
    private static final String GEMINI_API_VERSION = "v1";

    @Override
    public void onStart(ISuite suite) {
        // Defaults (configurable later if needed)
        logFilePath = System.getProperty("user.dir") + "/Logs/automation.log";
        logTailLines = 300;

        try {
            Files.createDirectories(Path.of(System.getProperty("user.dir"), "AIReports", "screenshots"));
        } catch (Exception ignored) {}
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        records.add(baseRecord(result, "PASSED"));
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        TestRecord r = baseRecord(result, "SKIPPED");
        r.error = throwableToString(result.getThrowable());
        records.add(r);
    }

    @Override
    public void onTestFailure(ITestResult result) {
        TestRecord r = baseRecord(result, "FAILED");

        // Auto-resolve WebDriver (no code required in test classes)
        WebDriver driver = resolveDriver(result);

        if (driver != null) {
            try { r.url = driver.getCurrentUrl(); } catch (Exception ignored) {}
            // Screenshot
            try {
                byte[] png = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
                String path="C:\\Users\\2457259\\OneDrive - Cognizant\\Desktop\\FindingHospitals\\Screenshot\\screenshot1.png";
                String ssPath = System.getProperty("user.dir")
                        + "/AIReports/screenshots/" + safeFileName(r.className + "_" + r.name) + ".png";
                Files.write(Path.of(path), png);
                r.screenshotPath = ssPath;

                // Allure attach (optional)
                try {
                    Allure.addAttachment("Failure Screenshot", "image/png",
                            new ByteArrayInputStream(png), ".png");
                } catch (Throwable ignored) {}
            } catch (Exception ignored) {}
        }

        r.error = throwableToString(result.getThrowable());
        r.lastLogs = tail(logFilePath, logTailLines);

        // --- Analysis: try Gemini first, then fall back to HeuristicEngine ---
        HeuristicOutput ho;
        try {
            if (getGeminiClientOrNull() != null) {
                ho = callGemini(r);
            } else {
                ho = HeuristicEngine.analyze(r.error, r.lastLogs);
            }
        } catch (Throwable aiEx) {
            ho = HeuristicEngine.analyze(r.error, r.lastLogs);
        }

        r.diagnosis  = ho.diagnosis;
        r.howToFix   = ho.howToFix;
        r.suggestion = ho.suggestion;
        r.sanity     = ho.sanity;

        // Copilot-ready prompt
        r.copilotPrompt = buildCopilotPrompt(r);

        // Allure attach prompt + analysis (optional)
        try {
            Allure.addAttachment("AI-style Diagnosis", "text/plain",
                    new ByteArrayInputStream(
                            (r.diagnosis + "\n\nHOW TO FIX:\n" + r.howToFix + "\n\nSUGGESTION:\n" + r.suggestion).getBytes(StandardCharsets.UTF_8)
                    ),
                    ".txt");
        } catch (Throwable ignored) {}

        records.add(r);
    }

    @Override
    public void onFinish(ISuite suite) {
        String html = buildHtmlReport(suite.getXmlSuite());
        Path out = Path.of(System.getProperty("user.dir"), "AIReports", "ai-report.html");
        try {
            Files.createDirectories(out.getParent());
            Files.writeString(out, html, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            System.out.println("AI Report generated at: " + out);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // ---- Gemini helpers ----

    // Prefer GOOGLE_API_KEY; fall back to GEMINI_API_KEY (legacy).
    // Also supports a local hard-coded OVERRIDE_API_KEY for quick non-committed testing.
    private Client getGeminiClientOrNull() {
        if (geminiClient != null) return geminiClient;
        synchronized (HeuristicAiReportListener.class) {
            if (geminiClient != null) return geminiClient;

            String key =
                    (OVERRIDE_API_KEY != null && !OVERRIDE_API_KEY.isBlank())
                            ? OVERRIDE_API_KEY
                            : Optional.ofNullable(System.getenv("GOOGLE_API_KEY"))
                            .orElse(System.getenv("GEMINI_API_KEY"));

            if (key == null || key.isBlank()) return null;

            // Build HttpOptions to explicitly set base URL and API version (optional).
            // The SDK allows customizing baseUrl/apiVersion/timeouts via HttpOptions.  [1](https://support.google.com/gemini/thread/348900199/need-to-update-api-key-in-envirmonetal-settings?hl=en)
            HttpOptions httpOptions = HttpOptions
                    .builder()
                    .baseUrl(GEMINI_BASE_URL)
                    .apiVersion(GEMINI_API_VERSION)
                    .timeout(60) // seconds
                    .build();

            geminiClient = Client.builder()
                    .apiKey(key)
                    .httpOptions(httpOptions)      // <-- explicit endpoint wired here
                    .build();
            return geminiClient;
        }
    }

    private String buildGeminiPrompt(TestRecord r) {
        String errShort  = r.error == null ? "" : r.error.substring(0, Math.min(r.error.length(), 6000));
        String logsShort = r.lastLogs == null ? "" : r.lastLogs.substring(0, Math.min(r.lastLogs.length(), 6000));

        return "You are a senior SDET. Analyze a failing Selenium+TestNG test and return ONLY JSON.\n\n"
                + "Test: " + r.className + "." + r.name + "\n"
                + "URL: " + (r.url == null ? "(unknown)" : r.url) + "\n\n"
                + "Stack trace (trimmed):\n" + errShort + "\n\n"
                + "Recent Logs (trimmed):\n" + logsShort + "\n\n"
                + "Return ONLY a compact JSON object with fields: "
                + "{ \"diagnosis\": string, \"howToFix\": string, \"suggestion\": string, \"sanity\": string }";
    }

    private HeuristicOutput callGemini(TestRecord r) throws Exception {
        Client client = getGeminiClientOrNull();
        if (client == null) throw new IllegalStateException("No GOOGLE_API_KEY/GEMINI_API_KEY or OVERRIDE_API_KEY found");

        Map<String, Object> schema = Map.of(
                "type", "object",
                "properties", Map.of(
                        "diagnosis",  Map.of("type", "string"),
                        "howToFix",   Map.of("type", "string"),
                        "suggestion", Map.of("type", "string"),
                        "sanity",     Map.of("type", "string")
                ),
                "required", List.of("diagnosis", "howToFix", "suggestion", "sanity")
        );

        GenerateContentConfig cfg = GenerateContentConfig.builder()
                .responseMimeType("application/json")
                .responseJsonSchema(schema)  // Map/JSON-schema variant; alternatively use Schema + responseSchema(...)
                .candidateCount(1)
                .maxOutputTokens(1200)
                .build();

        String prompt = buildGeminiPrompt(r);

        GenerateContentResponse resp =
                client.models.generateContent(GEMINI_MODEL, prompt, cfg);

        String json = resp.text(); // strict JSON expected due to schema
        JsonNode node = JSON.readTree(json);

        HeuristicOutput out = new HeuristicOutput();
        out.diagnosis  = node.path("diagnosis").asText("");
        out.howToFix   = node.path("howToFix").asText("");
        out.suggestion = node.path("suggestion").asText("");
        out.sanity     = node.path("sanity").asText("");

        if (out.diagnosis.isBlank() && out.howToFix.isBlank()
                && out.suggestion.isBlank() && out.sanity.isBlank()) {
            // If the model didn't follow the schema, at least return the raw output
            out.diagnosis = json;
        }
        return out;
    }

    // ---- WebDriver Resolution (no test code needed) ----

    private WebDriver resolveDriver(ITestResult result) {
        // 1) From TestNG context (if some frameworks already set it)
        try {
            Object ctxDriver = result.getTestContext().getAttribute("driver");
            if (ctxDriver instanceof WebDriver) return (WebDriver) ctxDriver;
            if (ctxDriver instanceof ThreadLocal) {
                Object got = tryThreadLocalGet(ctxDriver);
                if (got instanceof WebDriver) return (WebDriver) got;
            }
        } catch (Throwable ignored) {}

        // 2) From test instance fields (driver, BaseTest, etc.)
        Object instance = result.getInstance();
        if (instance != null) {
            WebDriver fromInstance = findDriverInObjectGraph(instance);
            if (fromInstance != null) return fromInstance;

            // 3) Try common getter methods
            WebDriver fromGetter = findDriverViaGetter(instance);
            if (fromGetter != null) return fromGetter;
        }

        // 4) From static fields in test class or its superclasses (e.g., ThreadLocal<WebDriver>)
        Class<?> cls = result.getTestClass().getRealClass();
        WebDriver fromStatics = findDriverInStatics(cls);
        if (fromStatics != null) return fromStatics;

        return null;
    }

    private WebDriver findDriverInObjectGraph(Object obj) {
        if (obj == null) return null;
        Class<?> c = obj.getClass();
        while (c != null && c != Object.class) {
            WebDriver d = findDriverInFields(c, obj);
            if (d != null) return d;
            c = c.getSuperclass();
        }
        return null;
    }

    @SuppressWarnings({"rawtypes","unchecked"})
    private WebDriver findDriverInFields(Class<?> c, Object target) {
        for (Field f : c.getDeclaredFields()) {
            try {
                f.setAccessible(true);
                Object val = f.get(target);
                if (val instanceof WebDriver) return (WebDriver) val;
                if (val instanceof ThreadLocal) {
                    Object got = tryThreadLocalGet(val);
                    if (got instanceof WebDriver) return (WebDriver) got;
                }
            } catch (Throwable ignored) {}
        }
        return null;
    }

    private WebDriver findDriverViaGetter(Object target) {
        try {
            Method m = target.getClass().getMethod("getDriver");
            Object val = m.invoke(target);
            if (val instanceof WebDriver) return (WebDriver) val;
        } catch (Throwable ignored) {}

        try {
            Method m = target.getClass().getMethod("driver");
            Object val = m.invoke(target);
            if (val instanceof WebDriver) return (WebDriver) val;
        } catch (Throwable ignored) {}

        return null;
    }

    private WebDriver findDriverInStatics(Class<?> cls) {
        Class<?> c = cls;
        while (c != null && c != Object.class) {
            for (Field f : c.getDeclaredFields()) {
                try {
                    if (!java.lang.reflect.Modifier.isStatic(f.getModifiers())) continue;
                    f.setAccessible(true);
                    Object val = f.get(null);
                    if (val instanceof WebDriver) return (WebDriver) val;
                    if (val instanceof ThreadLocal) {
                        Object got = tryThreadLocalGet(val);
                        if (got instanceof WebDriver) return (WebDriver) got;
                    }
                } catch (Throwable ignored) {}
            }
            c = c.getSuperclass();
        }
        return null;
    }

    @SuppressWarnings({"rawtypes","unchecked"})
    private Object tryThreadLocalGet(Object threadLocalObj) {
        try {
            Method get = ThreadLocal.class.getMethod("get");
            return get.invoke(threadLocalObj);
        } catch (Throwable ignored) {
            return null;
        }
    }

    // ---- Helpers ----

    private TestRecord baseRecord(ITestResult result, String status) {
        TestRecord r = new TestRecord();
        r.name = result.getMethod().getMethodName();
        r.className = result.getTestClass().getName();
        r.status = status;
        return r;
    }

    private String throwableToString(Throwable t) {
        if (t == null) return "";
        StringWriter sw = new StringWriter();
        t.printStackTrace(new PrintWriter(sw));
        return sw.toString();
    }

    private String tail(String path, int lines) {
        try {
            Path p = Path.of(path);
            if (!Files.exists(p)) return "(log file not found: " + path + ")";
            List<String> all = Files.readAllLines(p, StandardCharsets.UTF_8);
            int from = Math.max(0, all.size() - lines);
            return String.join("\n", all.subList(from, all.size()));
        } catch (Exception e) {
            return "(failed to read logs: " + e.getMessage() + ")";
        }
    }

    private String buildCopilotPrompt(TestRecord r) {
        String errShort = r.error == null ? "" : r.error.substring(0, Math.min(r.error.length(), 1500));
        String logsShort = r.lastLogs == null ? "" : r.lastLogs.substring(0, Math.min(r.lastLogs.length(), 1500));
        return """
        You are my coding assistant inside IntelliJ. Help me fix this failing Selenium+TestNG test.

        Test: %s.%s
        URL: %s

        Stack trace (trimmed):
        %s

        Recent Logs (trimmed):
        %s

        Provide:
        1) Likely root cause in 1-2 sentences.
        2) Concrete code changes with Java snippets (locators/waits).
        3) A safer locator based on typical practo.com markup (prefer id, name, data-*; avoid brittle class).
        4) A sanity check I should add after the action.

        Keep it short and specific.
        """.formatted(r.className, r.name, r.url == null ? "(unknown)" : r.url, errShort, logsShort);
    }

    private String buildHtmlReport(XmlSuite suite) {
        long passed = records.stream().filter(r -> "PASSED".equals(r.status)).count();
        long failed = records.stream().filter(r -> "FAILED".equals(r.status)).count();
        long skipped = records.stream().filter(r -> "SKIPPED".equals(r.status)).count();

        // Build sections (HTML already escaped)
        StringBuilder passedHtml = new StringBuilder();
        records.stream().filter(r -> "PASSED".equals(r.status))
                .forEach(r -> passedHtml.append("<div class='test-card passed'><b>")
                        .append(esc(r.className)).append(".").append(esc(r.name))
                        .append("</b></div>"));

        StringBuilder skippedHtml = new StringBuilder();
        records.stream().filter(r -> "SKIPPED".equals(r.status))
                .forEach(r -> skippedHtml.append("<div class='test-card skipped'><b>")
                        .append(esc(r.className)).append(".").append(esc(r.name))
                        .append("</b></div>"));

        StringBuilder failedHtml = new StringBuilder();
        records.stream().filter(r -> "FAILED".equals(r.status)).forEach(r -> {
            failedHtml.append("<div class='test-card failed'>")
                    .append("<div><b>").append(esc(r.className)).append(".").append(esc(r.name)).append("</b></div>");

            if (r.url != null) {
                failedHtml.append("<div class='small'><b>URL:</b> ").append(esc(r.url)).append("</div>");
            }
            if (r.screenshotPath != null) {
                String rel = esc(relPath(r.screenshotPath));
                failedHtml.append("<div style='margin-top:8px'><b>Screenshot:</b> <img class='sshot' src='")
                        .append(rel).append("'/></div>");
            }
            if (r.error != null && !r.error.isBlank()) {
                failedHtml.append("<details class='details'><summary><b>Stack Trace</b></summary><pre><code>")
                        .append(esc(r.error)).append("</code></pre></details>");
            }
            if (r.lastLogs != null && !r.lastLogs.isBlank()) {
                failedHtml.append("<details class='details'><summary><b>Recent Logs</b></summary><pre><code>")
                        .append(esc(r.lastLogs)).append("</code></pre></details>");
            }
            if (r.diagnosis != null && !r.diagnosis.isBlank()) {
                failedHtml.append("<div class='h2' style='margin-top:14px'>Likely Root Cause</div><pre><code>")
                        .append(esc(r.diagnosis)).append("</code></pre>");
            }
            if (r.howToFix != null && !r.howToFix.isBlank()) {
                failedHtml.append("<div class='h2'>How to Fix</div><pre><code>")
                        .append(esc(r.howToFix)).append("</code></pre>");
            }
            if (r.suggestion != null && !r.suggestion.isBlank()) {
                failedHtml.append("<div class='h2'>Suggested Locator/Wait</div><pre><code>")
                        .append(esc(r.suggestion)).append("</code></pre>");
            }
            if (r.sanity != null && !r.sanity.isBlank()) {
                failedHtml.append("<div class='h2'>Sanity Check to Add</div><pre><code>")
                        .append(esc(r.sanity)).append("</code></pre>");
            }
            if (r.copilotPrompt != null && !r.copilotPrompt.isBlank()) {
                failedHtml.append(
                        "<details class='details'><summary><b>Copilot Prompt (paste in IDE)</b></summary>" +
                                "<pre><code>" + esc(r.copilotPrompt) + "</code></pre></details>"
                );
            }
            failedHtml.append("</div>");
        });

        String html = """
<!doctype html>
<html>
<head>
<meta charset="utf-8">
<title>AI-style Test Report (Heuristic + Gemini)</title>
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<style>
:root {
  --bg:#0b1220; --panel:#0f172a; --border:#1f2937; --text:#e5e7eb; --muted:#94a3b8;
  --ok:#22c55e; --fail:#ef4444; --skip:#f59e0b; --link:#93c5fd; --btn:#1f2937; --btnb:#334155;
}
*{box-sizing:border-box}
body{margin:0;padding:20px;font-family:Segoe UI, Inter, Roboto, Arial, sans-serif;background:var(--bg);color:var(--text)}
.container{max-width:1120px;margin:0 auto}
.header{display:flex;align-items:center;justify-content:space-between;gap:12px;margin-bottom:16px;flex-wrap:wrap}
.h1{font-size:22px;font-weight:700}
.small{color:var(--muted);font-size:12px}
.actions{display:flex;gap:8px;flex-wrap:wrap}
.btn{background:linear-gradient(180deg,#111827,#0b1320);border:1px solid var(--btnb);color:#e5e7eb;padding:8px 12px;border-radius:8px;cursor:pointer}
.btn:hover{filter:brightness(1.1)}
.cards{display:grid;grid-template-columns:repeat(3,1fr);gap:12px;margin:12px 0 8px}
@media (max-width: 780px){ .cards{grid-template-columns:1fr;}}
.card{background:linear-gradient(180deg,#0f172a,#0b1320);border:1px solid var(--border);border-radius:12px;padding:14px;box-shadow:0 2px 16px rgba(0,0,0,.25)}
.card .badge{display:inline-block;font-size:12px;border-radius:999px;padding:2px 8px;border:1px solid var(--btnb);color:#cbd5e1;margin-bottom:8px}
.badge.ok{background:rgba(34,197,94,.12);border-color:rgba(34,197,94,.35);color:#86efac}
.badge.fail{background:rgba(239,68,68,.12);border-color:rgba(239,68,68,.35);color:#fca5a5}
.badge.skip{background:rgba(245,158,11,.12);border-color:rgba(245,158,11,.35);color:#fcd34d}
.card .num{font-size:28px;font-weight:700}
.section{margin:26px 0}
.kpi{display:flex;gap:18px;align-items:center;flex-wrap:wrap}
canvas{max-width:260px;background:#0b1220;border-radius:12px;border:1px solid var(--border)}
.legend{display:flex;gap:12px;flex-wrap:wrap}
.legend span{display:flex;align-items:center;gap:6px;color:#94a3b8;font-size:13px}
.dot{width:10px;height:10px;border-radius:50%}
hr{border:none;border-top:1px solid var(--border);margin:24px 0}
.test-card{margin:12px 0;padding:12px;border-radius:10px;background:var(--panel);border:1px solid var(--border)}
.failed,.passed,.skipped{border-left:4px solid transparent}
.failed{border-left-color:#ef4444}
.passed{border-left-color:#22c55e}
.skipped{border-left-color:#f59e0b}
summary{cursor:pointer;color:#93c5fd}
pre{background:#0b1220;border:1px solid var(--border);border-radius:8px;padding:10px;white-space:pre-wrap;color:#cbd5e1}
code{white-space:pre-wrap}
img.sshot{max-width:100%;border-radius:8px;border:1px solid var(--border)}
.h2{font-size:18px;margin:0 0 10px 0}
.footer{margin-top:24px;color:#94a3b8;font-size:12px}
@media print{
  .actions{display:none}
  body{background:#fff;color:#000}
  .card,.test-card,pre{border-color:#ccc}
}
</style>
</head>
<body>
  <div class="container">
    <div class="header">
      <div>
        <div class="h1">AI-style Test Report (Heuristic + Gemini)</div>
        <div class="small">Suite: {{SUITE}} &nbsp;|&nbsp; Generated: {{GENERATED}}</div>
      </div>
      <div class="actions">
        <button class="btn" onclick="downloadHtml()">⬇️ Download HTML</button>
        <button class="btn" onclick="window.print()">🖨️ Print / Save PDF</button>
      </div>
    </div>

    <div class="cards">
      <div class="card">
        <span class="badge ok">Passed</span>
        <div class="num">{{P_COUNT}}</div>
      </div>
      <div class="card">
        <span class="badge fail">Failed</span>
        <div class="num">{{F_COUNT}}</div>
      </div>
      <div class="card">
        <span class="badge skip">Skipped</span>
        <div class="num">{{S_COUNT}}</div>
      </div>
    </div>

    <div class="section kpi">
      <canvas id="summaryPie" width="260" height="260"></canvas>
      <div class="legend">
        <span><i class="dot" style="background:#22c55e"></i>Passed ({{P_COUNT}})</span>
        <span><i class="dot" style="background:#ef4444"></i>Failed ({{F_COUNT}})</span>
        <span><i class="dot" style="background:#f59e0b"></i>Skipped ({{S_COUNT}})</span>
      </div>
    </div>

    {{PASSED_SECTION}}
    {{SKIPPED_SECTION}}
    {{FAILED_SECTION}}

    <hr/>
    <div class="section">
      <div class="h2">Suggested Additions</div>
      <ul class="small">
        <li><b>Flakiness tracking</b>: persist pass/fail history JSON per test.</li>
        <li><b>Top failing locators</b>: parse stack traces and rank brittle selectors.</li>
        <li><b>Timing insights</b>: capture time to first meaningful element with waits.</li>
        <li><b>Environment fingerprint</b>: OS, browser, base URL, build number.</li>
        <li><b>Auto-fix snippets</b>: embed ready-to-copy code for common failures.</li>
      </ul>
    </div>

    <div class="footer">© {{YEAR}} — Generated locally. Buttons work offline.</div>
  </div>

  <script>
  (function(){
    var data = [{{P_COUNT}},{{F_COUNT}},{{S_COUNT}}];
    var colors = ['#22c55e','#ef4444','#f59e0b'];
    var total = data.reduce(function(a,b){return a+b;}, 0) || 1;
    var c = document.getElementById('summaryPie');
    if(!c) return;
    var ctx = c.getContext('2d');
    var cx = c.width/2, cy = c.height/2, r = Math.min(cx,cy) - 10;
    var start = -Math.PI/2;
    for (var i=0;i<data.length;i++){
      var angle = (data[i]/total)*Math.PI*2;
      ctx.beginPath();
      ctx.moveTo(cx,cy);
      ctx.arc(cx,cy,r,start,start+angle);
      ctx.closePath();
      ctx.fillStyle = colors[i];
      ctx.fill();
      start += angle;
    }
    ctx.globalCompositeOperation = 'destination-out';
    ctx.beginPath();
    ctx.arc(cx,cy,r*0.6,0,Math.PI*2);
    ctx.fill();
    ctx.globalCompositeOperation = 'source-over';
  })();

  function downloadHtml(){
    try {
      var html = document.documentElement.outerHTML;
      var blob = new Blob([html], {type:'text/html'});
      var a = document.createElement('a');
      a.href = URL.createObjectURL(blob);
      a.download = 'ai-report.html';
      document.body.appendChild(a);
      a.click();
      setTimeout(function(){ URL.revokeObjectURL(a.href); document.body.removeChild(a); }, 500);
    } catch(e) {
      alert('Download failed: ' + e);
    }
  }
  </script>

</body>
</html>
""";

        // Optional sections rendered only when non-empty
        String passedSection = passed > 0
                ? "<div class='section'><div class='h2'>Passed</div>" + passedHtml + "</div>"
                : "";
        String skippedSection = skipped > 0
                ? "<div class='section'><div class='h2'>Skipped</div>" + skippedHtml + "</div>"
                : "";
        String failedSection = failed > 0
                ? "<div class='section'><div class='h2'>Failures (with Heuristic Analysis)</div>" + failedHtml + "</div>"
                : "";

        // Replace tokens
        html = html.replace("{{SUITE}}", esc(suite.getName()))
                .replace("{{GENERATED}}", esc(LocalDateTime.now().toString()))
                .replace("{{YEAR}}", String.valueOf(LocalDateTime.now().getYear()))
                .replace("{{P_COUNT}}", String.valueOf(passed))
                .replace("{{F_COUNT}}", String.valueOf(failed))
                .replace("{{S_COUNT}}", String.valueOf(skipped))
                .replace("{{PASSED_SECTION}}", passedSection)
                .replace("{{SKIPPED_SECTION}}", skippedSection)
                .replace("{{FAILED_SECTION}}", failedSection);

        return html;
    }

    private String esc(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }

    private String relPath(String abs) {
        try {
            Path base = Path.of(System.getProperty("user.dir"), "AIReports");
            return base.toUri().relativize(Path.of(abs).toUri()).getPath();
        } catch (Exception e) {
            return abs;
        }
    }

    private String safeFileName(String raw) {
        if (raw == null) return "image";
        String s = raw.replaceAll("[\\\\/:*?\"<>|]", "_");
        if (s.length() > 120) s = s.substring(0, 120);
        return s;
    }

    // --- Heuristic Engine (no network) ---

    static class HeuristicOutput {
        String diagnosis;
        String howToFix;
        String suggestion;
        String sanity;
    }

    static class HeuristicEngine {

        static HeuristicOutput analyze(String error, String logs) {
            String e = (error == null ? "" : error);
            HeuristicOutput out = new HeuristicOutput();

            if (e.contains("NoSuchElementException")) {
                out.diagnosis = "Element not found. Locator may be brittle or the element hasn't appeared yet.";
                out.howToFix = """
                • Prefer stable locators (id, name, data-testid) over dynamic class combos.
                • Add an explicit wait for visibility or presence before interacting.
                • Re-check the page flow; ensure you navigated to the correct state.
                """;
                out.suggestion = codeWaitSnippet("By.id(\"username\")");
                out.sanity = "After clicking the action, assert that the expected container/header becomes visible.";
                return out;
            }

            if (e.contains("TimeoutException")) {
                out.diagnosis = "Wait condition timed out. The element or state didn't become ready in time.";
                out.howToFix = """
                • Increase explicit wait or use a more appropriate ExpectedCondition (visibility/clickable/urlContains).
                • Ensure network or animations complete; add wait for document.readyState or specific container.
                """;
                out.suggestion = codeWaitSnippet("By.cssSelector(\"button[type='submit']\")");
                out.sanity = "Assert the URL or key header text to confirm page readiness.";
                return out;
            }

            if (e.contains("StaleElementReferenceException")) {
                out.diagnosis = "Element went stale after a DOM refresh (navigation/AJAX).";
                out.howToFix = """
                • Re-find the element just before use.
                • Wrap interaction in a retry with wait for presence/visibility.
                """;
                out.suggestion = """
                WebElement btn = new WebDriverWait(driver, java.time.Duration.ofSeconds(10))
                        .until(org.openqa.selenium.support.ui.ExpectedConditions.elementToBeClickable(By.id("login")));
                btn.click();""";
                out.sanity = "After click, verify the next page's unique element is visible.";
                return out;
            }

            if (e.contains("ElementClickInterceptedException")) {
                out.diagnosis = "Click blocked by overlay or element not in viewport.";
                out.howToFix = """
                • Scroll element into view and wait for overlay to disappear.
                • Use ExpectedConditions.elementToBeClickable on the target.
                """;
                out.suggestion = """
                WebElement el = driver.findElement(By.xpath("//button[normalize-space()='Login']"));
                ((JavascriptExecutor)driver).executeScript("arguments[0].scrollIntoView({block:'center'});", el);
                new org.openqa.selenium.support.ui.WebDriverWait(driver, java.time.Duration.ofSeconds(10))
                        .until(org.openqa.selenium.support.ui.ExpectedConditions.elementToBeClickable(el)).click();""";
                out.sanity = "Assert that the overlay or spinner is gone before clicking.";
                return out;
            }

            if (e.contains("AssertionError")) {
                out.diagnosis = "Assertion failed. The expected state wasn't met (data, visibility or URL).";
                out.howToFix = """
                • Print actual value in logs to compare with expected.
                • Add prerequisite waits to ensure the state is ready before asserting.
                • Re-check the test data or environment config (base.url, credentials).
                """;
                out.suggestion = """
                boolean visible = new org.openqa.selenium.support.ui.WebDriverWait(driver, java.time.Duration.ofSeconds(10))
                        .until(org.openqa.selenium.support.ui.ExpectedConditions.visibilityOfElementLocated(By.cssSelector("img.img-responsive"))).isDisplayed();
                org.testng.Assert.assertTrue(visible, "Login image should be visible after clicking Login.");""";
                out.sanity = "Add an assertion for URL contains '/login' or specific heading text.";
                return out;
            }

            // Default fallback
            out.diagnosis = "Generic failure. Review stack trace and logs.";
            out.howToFix = """
            • Add targeted waits around the failing step.
            • Inspect locator stability; replace brittle class-based selectors.
            • Capture and review a screenshot and the last 300 log lines (attached).""";
            out.suggestion = codeWaitSnippet("By.xpath(\"//input[@id='username']\")");
            out.sanity = "Add a post-action check (URL/title/heading) to confirm correct page state.";
            return out;
        }

        private static String codeWaitSnippet(String byExpr) {
            return """
            WebElement el = new org.openqa.selenium.support.ui.WebDriverWait(driver, java.time.Duration.ofSeconds(10))
                    .until(org.openqa.selenium.support.ui.ExpectedConditions.visibilityOfElementLocated(%s));
            el.click();""".formatted(byExpr);
        }
    }
}
