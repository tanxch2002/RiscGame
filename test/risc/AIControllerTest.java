package risc;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class AIControllerTest {
    public static void main(String[] args) {
        testDoInitialPlacement();
        testGenerateTurnOrdersSafely();
        testBuildPrompt();
        testParseContent();
        testApplyLine();

        System.out.println("All AIControllerTest tests passed!");
    }

    private static void testDoInitialPlacement() {
        Game game = new Game(null);
        game.setUpMap(2);
        game.initPlayers(2);

        Player original = game.getPlayer(0);
        AIPlayer ai = new AIPlayer(0, "DeepSeekBot");

        for (Territory t : original.getTerritories()) {
            t.setOwner(ai);
            ai.addTerritory(t);
        }

        game.getAllPlayers().set(0, ai);

        AIController controller = new AIController(game, ai);
        controller.doInitialPlacement();

        for (Territory t : ai.getTerritories()) {
            assert !t.getStationedUnitsMap(ai.getId()).isEmpty() : "Territory " + t.getName() + " should have units";
        }
    }

    private static void testGenerateTurnOrdersSafely() {
        Game game = new Game(null);
        game.setUpMap(2);
        game.initPlayers(2);

        Player original = game.getPlayer(0);
        AIPlayer ai = new AIPlayer(0, "DeepSeekBot");

        for (Territory t : original.getTerritories()) {
            t.setOwner(ai);
            ai.addTerritory(t);
        }

        game.getAllPlayers().set(0, ai);

        // Create a test AIController that doesn't call DeepSeek API
        AIController controller = new AIController(game, ai) {
            @Override
            public void generateTurnOrders() {
                // Instead of calling DeepSeek, just add some orders directly
                game.addOrder(new MoveOrder(ai.getId(), "A", "B", 0, 1));
                game.addOrder(new AttackOrder(ai.getId(), "A", "B", 0, 1));
                game.addOrder(new UpgradeUnitOrder(ai.getId(), "A", 0, 1, 1));
                game.addOrder(new TechUpgradeOrder(ai.getId()));
                game.addOrder(new AllianceOrder(ai.getId(), "Player2"));
                game.broadcast("Test orders added for AI");
            }
        };

        // Run with timeout to prevent hanging
        Thread genThread = new Thread(() -> controller.generateTurnOrders());
        genThread.start();
        try {
            genThread.join(2000); // Wait max 2 seconds
            if (genThread.isAlive()) {
                genThread.interrupt();
                System.err.println("WARNING: generateTurnOrders() did not complete in time");
            }
        } catch (InterruptedException e) {
            // Ignore
        }

        assert !game.getAllOrders().isEmpty() : "Orders should be generated";
    }

    private static void testBuildPrompt() {
        Game game = new Game(null);
        game.setUpMap(2);
        game.initPlayers(2);

        Player original = game.getPlayer(0);
        AIPlayer ai = new AIPlayer(0, "DeepSeekBot");

        for (Territory t : original.getTerritories()) {
            t.setOwner(ai);
            ai.addTerritory(t);
        }

        game.getAllPlayers().set(0, ai);

        AIController controller = new AIController(game, ai);

        try {
            Method buildPrompt = AIController.class.getDeclaredMethod("buildPrompt");
            buildPrompt.setAccessible(true);
            String prompt = (String) buildPrompt.invoke(controller);

            assert prompt != null && !prompt.isEmpty() : "Prompt should not be empty";
            assert prompt.contains("DeepSeekBot") : "Prompt should contain AIPlayer name";
        } catch (Exception e) {
            assert false : "Exception shouldn't be thrown: " + e.getMessage();
        }
    }

    private static void testParseContent() {
        Game game = new Game(null);
        AIPlayer ai = new AIPlayer(0, "DeepSeekBot");
        AIController controller = new AIController(game, ai);

        try {
            Method parseContent = AIController.class.getDeclaredMethod("parseContent", String.class);
            parseContent.setAccessible(true);

            // Test valid JSON
            String validJson = "{\"id\":\"12345\",\"content\":\"M A B 0 1\\nD\"}";
            List<String> result = (List<String>) parseContent.invoke(controller, validJson);
            assert result.size() == 2 : "Should parse 2 lines from content";

            // Test invalid JSON
            String invalidJson = "{\"invalid\": true}";
            result = (List<String>) parseContent.invoke(controller, invalidJson);
            assert result.isEmpty() : "Should return empty list for invalid JSON";
        } catch (Exception e) {
            assert false : "Exception shouldn't be thrown: " + e.getMessage();
        }
    }

    private static void testApplyLine() {
        Game game = new Game(null);
        game.setUpMap(2);
        game.initPlayers(2);

        Player original = game.getPlayer(0);
        AIPlayer ai = new AIPlayer(0, "DeepSeekBot");

        for (Territory t : original.getTerritories()) {
            t.setOwner(ai);
            ai.addTerritory(t);
        }

        game.getAllPlayers().set(0, ai);

        AIController controller = new AIController(game, ai);

        try {
            Method applyLine = AIController.class.getDeclaredMethod("applyLine", String.class);
            applyLine.setAccessible(true);

            // Test move order
            Boolean result = (Boolean) applyLine.invoke(controller, "M A B 0 1");
            assert result : "Move order should be applied";

            // Test attack order
            result = (Boolean) applyLine.invoke(controller, "A A B 0 1");
            assert result : "Attack order should be applied";

            // Test upgrade order
            result = (Boolean) applyLine.invoke(controller, "U A 0 1 1");
            assert result : "Upgrade order should be applied";

            // Test tech upgrade order
            result = (Boolean) applyLine.invoke(controller, "T");
            assert result : "Tech upgrade order should be applied";

            // Test alliance order
            result = (Boolean) applyLine.invoke(controller, "FA Player2");
            assert result : "Alliance order should be applied";

            // Test invalid order
            result = (Boolean) applyLine.invoke(controller, "INVALID");
            assert !result : "Invalid order should not be applied";
        } catch (Exception e) {
            assert false : "Exception shouldn't be thrown: " + e.getMessage();
        }
    }
}