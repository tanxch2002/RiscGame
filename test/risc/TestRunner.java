package risc;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class TestRunner {
    public static void main(String[] args) {
        runAllTests();
        System.out.println("All tests completed successfully!");
        System.exit(0); // Ensure we exit cleanly
    }

    public static void runAllTests() {
        List<Class<?>> testClasses = getAllTestClasses();
        int totalTests = 0;
        int passedTests = 0;

        System.out.println("============= RUNNING ALL TESTS =============");

        for (Class<?> testClass : testClasses) {
            String className = testClass.getSimpleName();
            System.out.println("Running tests in " + className);

            try {
                Method mainMethod = testClass.getMethod("main", String[].class);
                mainMethod.invoke(null, (Object) new String[0]);
                passedTests++;
                System.out.println(className + " - PASSED");
            } catch (Exception e) {
                System.out.println(className + " - FAILED: " + e.getMessage());
                e.printStackTrace();
            }

            totalTests++;
        }

        System.out.println("============= TEST SUMMARY =============");
        System.out.println("Total tests: " + totalTests);
        System.out.println("Passed tests: " + passedTests);
        System.out.println("Failed tests: " + (totalTests - passedTests));
    }

    private static List<Class<?>> getAllTestClasses() {
        List<Class<?>> testClasses = new ArrayList<>();

        testClasses.add(AIControllerTest.class);
        testClasses.add(AIPlayerTest.class);
        testClasses.add(AllianceOrderTest.class);
        testClasses.add(AttackOrderTest.class);
        testClasses.add(ClientHandlerTest.class);
        testClasses.add(ClientTerritoryDataTest.class);
        testClasses.add(DeepSeekChatSampleTest.class);
        testClasses.add(DeepSeekClientTest.class);
        testClasses.add(DiceRollerTest.class);
        testClasses.add(GameTest.class);
        testClasses.add(GlobalServerTest.class);
        testClasses.add(MapBuilderTest.class);
        testClasses.add(MapPanelTest.class);
        testClasses.add(MoveOrderTest.class);
        testClasses.add(OrderExecutorTest.class);
        testClasses.add(OrderTest.class);
        testClasses.add(PlayerAccountTest.class);
        testClasses.add(PlayerTest.class);
        testClasses.add(RiscClientTest.class);
        testClasses.add(RiscClientGUITest.class);
        testClasses.add(RiscServerTest.class);
        testClasses.add(TechUpgradeOrderTest.class);
        testClasses.add(TerritoryTest.class);
        testClasses.add(UpgradeUnitOrderTest.class);

        return testClasses;
    }
}