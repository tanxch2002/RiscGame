package risc;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

/**
 * 极简 DeepSeek 大模型 HTTP 调用封装。
 */
public class DeepSeekClient {
    private static final String ENDPOINT = "https://api.deepseek.com/chat/completions";
    private static final String API_KEY = System.getenv("DEEPSEEK_API_KEY");

    private final HttpClient http = HttpClient.newHttpClient();

    public String chat(String prompt) {
        try {
            String body = """
              {
                "model":"deepseek-chat",
                "messages":[{"role":"user","content":%s}],
                "temperature":0.2,
                "max_tokens":512
              }""".formatted(jsonEscape(prompt));

            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(ENDPOINT))
                    .header("Authorization", "Bearer " + API_KEY)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();

            HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
            // 这里只做演示，实际需检查 statusCode 与 JSON 解析
            return resp.body();
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    /* 把普通字符串包装成 JSON 字面量 */
    private String jsonEscape(String s) {
        return "\"" + s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n") + "\"";
    }
}
