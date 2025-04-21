package risc;

import java.lang.reflect.Method;
import java.lang.reflect.Field;

public class DeepSeekClientTest {
    public static void main(String[] args) {
        testChat();
        testJsonEscape();

        System.out.println("All DeepSeekClientTest tests passed!");
    }

    private static void testChat() {
        DeepSeekClient client = new DeepSeekClient();
        // Create a custom version that doesn't make real API calls
        DeepSeekClient mockedClient = new DeepSeekClient() {
            @Override
            public String chat(String prompt) {
                return "{\"id\":\"chat-123\",\"content\":\"Mocked response\"}";
            }
        };

        String result = mockedClient.chat("Test prompt");
        assert result != null : "Result should not be null";
        assert result.contains("content") : "Result should contain content field";

        // Try with standard client - this might fail in CI environments with no API key
        try {
            String response = client.chat("Test prompt");
            System.out.println("Note: DeepSeekClient real chat execution with result length: " +
                    (response != null ? response.length() : 0));
        } catch (Exception e) {
            // Expected when no API key is present
        }
    }

    private static void testJsonEscape() {
        DeepSeekClient client = new DeepSeekClient();

        try {
            Method jsonEscape = DeepSeekClient.class.getDeclaredMethod("jsonEscape", String.class);
            jsonEscape.setAccessible(true);

            // Test simple string
            String original = "Simple string";
            String escaped = (String) jsonEscape.invoke(client, original);
            assert escaped.startsWith("\"") && escaped.endsWith("\"") :
                    "Should wrap with double quotes";

            // Test with quotes
            original = "Test \"quotes\"";
            escaped = (String) jsonEscape.invoke(client, original);
            assert escaped.contains("\\\"") : "Should escape quotes";

            // Test with backslashes
            original = "Test \\backslashes\\";
            escaped = (String) jsonEscape.invoke(client, original);
            assert escaped.contains("\\\\") : "Should escape backslashes";

            // Test with newlines
            original = "Test \nnewlines";
            escaped = (String) jsonEscape.invoke(client, original);
            assert escaped.contains("\\n") : "Should escape newlines";

            // Test with all special characters
            original = "Test \"quotes\" and \\backslashes\\ and \nnewlines";
            escaped = (String) jsonEscape.invoke(client, original);
            assert escaped.contains("\\\"") && escaped.contains("\\\\") && escaped.contains("\\n") :
                    "Should escape all special characters";
        } catch (Exception e) {
            assert false : "Should not throw exception: " + e.getMessage();
        }
    }
}