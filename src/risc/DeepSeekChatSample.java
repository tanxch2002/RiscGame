package risc;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;


public class DeepSeekChatSample {

    /** DeepSeek Chat Completion endpoint */
    private static final String ENDPOINT = "https://api.deepseek.com/chat/completions";

    public static void main(String[] args) throws Exception {

        // 1. Obtain API key
        String apiKey = (args.length > 0) ? args[0] : System.getenv("DEEPSEEK_API_KEY");
        if (apiKey == null || apiKey.isBlank()) {
            System.err.println("❌ No DeepSeek API Key provided! Please set the DEEPSEEK_API_KEY environment variable or pass it as a command-line argument.");
            return;
        }

        // 2. Construct JSON request body equivalent to curl
        String jsonBody = """
          {
            "model": "deepseek-chat",
            "messages": [
              {"role": "system", "content": "You are a helpful assistant."},
              {"role": "user",   "content": "Hello!"}
            ],
            "stream": false
          }""";

        // 3. Send POST using Java 11 HttpClient
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

        System.out.println("→ Sending request to DeepSeek...");
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // 4. Simple parsing: extract "content":"..."
        if (response.statusCode() / 100 == 2) {
            String content = extractContent(response.body());
            System.out.println("✅ DeepSeek response:\n" + content);
        } else {
            System.err.println("❌ Request failed, HTTP " + response.statusCode());
            System.err.println(response.body());
        }
    }

    /**
     * Minimal JSON parser: find "content":"..." in the response body
     * - For demonstration only; use a proper JSON library in production.
     */
    private static String extractContent(String json) {
        int idx = json.indexOf("\"content\"");
        if (idx < 0) return "(content field not found)";
        int colon  = json.indexOf(':', idx);
        int quote1 = json.indexOf('"', colon + 1) + 1;
        int quote2 = json.indexOf('"', quote1);
        if (quote1 <= 0 || quote2 <= quote1) return "(parsing error)";
        return json.substring(quote1, quote2)
                .replace("\\n", "\n")
                .replace("\\\"", "\"")
                .replace("\\\\", "\\")
                .trim();
    }
}
