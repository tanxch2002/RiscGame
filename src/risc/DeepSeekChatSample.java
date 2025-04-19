package risc;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * DeepSeek Chat API 演示
 *
 * 编译：
 *     javac DeepSeekChatSample.java
 *
 * 运行：
 *     # 方式一：事先导出环境变量
 *     export DEEPSEEK_API_KEY="sk-xxxxxxxx"
 *     java DeepSeekChatSample
 *
 *     # 方式二：直接把 key 当命令行参数
 *     java DeepSeekChatSample sk-xxxxxxxx
 *
 * 依赖：JDK 11+
 */
public class DeepSeekChatSample {

    /* DeepSeek Chat Completion 端点 */
    private static final String ENDPOINT = "https://api.deepseek.com/chat/completions";

    public static void main(String[] args) throws Exception {

        /* 1. 获取 API‑Key */
        String apiKey = (args.length > 0) ? args[0] : System.getenv("DEEPSEEK_API_KEY");
        if (apiKey == null || apiKey.isBlank()) {
            System.err.println("❌ 未提供 DeepSeek API Key！请设置环境变量 DEEPSEEK_API_KEY 或在命令行传入。");
            return;
        }

        /* 2. 组装与 curl 等价的 JSON 请求体 */
        String jsonBody = """
          {
            "model": "deepseek-chat",
            "messages": [
              {"role": "system", "content": "You are a helpful assistant."},
              {"role": "user",   "content": "Hello!"}
            ],
            "stream": false
          }""";

        /* 3. 使用 Java 11 HttpClient 发送 POST */
        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(ENDPOINT))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + apiKey.trim())
                .timeout(Duration.ofSeconds(15))
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        System.out.println("→ 正在向 DeepSeek 发送请求…");
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        /* 4. 简易解析：提取 "content":"..." */
        if (response.statusCode() / 100 == 2) {
            String content = extractContent(response.body());
            System.out.println("✅ DeepSeek 回复：\n" + content);
        } else {
            System.err.println("❌ 请求失败，HTTP " + response.statusCode());
            System.err.println(response.body());
        }
    }

    /**
     * 极简 JSON 解析器：在返回体里搜 `"content":"..."`
     * ‑ 仅用于演示连通性；生产环境请使用正规 JSON 库。
     */
    private static String extractContent(String json) {
        int idx = json.indexOf("\"content\"");
        if (idx < 0) return "(未找到 content 字段)";
        int colon  = json.indexOf(':', idx);
        int quote1 = json.indexOf('"', colon + 1) + 1;
        int quote2 = json.indexOf('"', quote1);
        if (quote1 <= 0 || quote2 <= quote1) return "(解析失败)";
        return json.substring(quote1, quote2)
                .replace("\\n", "\n")
                .replace("\\\"", "\"")
                .replace("\\\\", "\\")
                .trim();
    }
}
