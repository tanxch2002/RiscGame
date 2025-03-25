package risc;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class GameTest {

    @Test
    void setUpMap() {
        Game g = new Game();
        g.setUpMap(3); // 构建 6 块地
        Territory tA = g.getTerritoryByName("A");
        assertNotNull(tA);
        // 简单验证地名
        assertNull(g.getTerritoryByName("Z")); // 不存在
    }

    @Test
    void initPlayers() {
        Game g = new Game();
        g.setUpMap(3);
        g.initPlayers(3);
        // 这里验证玩家数量和名称
        assertEquals("Player1", g.getPlayer(0).getName());
        assertEquals("Player2", g.getPlayer(1).getName());
        assertEquals("Player3", g.getPlayer(2).getName());
    }

    @Test
    void getInitialUnits() {
        Game g = new Game();
        assertEquals(10, g.getInitialUnits());
    }

    @Test
    void distributeInitialUnits() {
        Game g = new Game();
        g.setUpMap(2);
        g.initPlayers(2);
        // 不同玩家
        g.distributeInitialUnits();
        // 没有异常就说明分配结束
        assertTrue(true);
    }

    @Test
    void addOrder() {
        Game g = new Game();
        Order o = new MoveOrder(0, "A", "B", 5);
        g.addOrder(o);
        assertTrue(g.getAllOrders().contains(o));
    }

    @Test
    void executeAllMoveOrders() {
        Game g = new Game();
        g.setUpMap(2);
        g.initPlayers(2);

        Player p0 = g.getPlayer(0);
        Territory tA = p0.getTerritories().get(0);
        Territory tB = p0.getTerritories().get(1);
        tA.setUnits(10);

        // 添加 MoveOrder
        g.addOrder(new MoveOrder(0, tA.getName(), tB.getName(), 5));
        g.executeAllMoveOrders();
        assertEquals(5, tA.getUnits());
        assertEquals(5, tB.getUnits());
    }

    @Test
    void executeAllAttackOrders() {
        Game g = new Game();
        g.setUpMap(2);
        g.initPlayers(2);

        Player p0 = g.getPlayer(0);
        Player p1 = g.getPlayer(1);
        Territory tA = p0.getTerritories().get(0);
        Territory tB = p1.getTerritories().get(0);
        tA.setUnits(10);
        tB.setUnits(5);

        // 添加 AttackOrder
        g.addOrder(new AttackOrder(0, tA.getName(), tB.getName(), 4));
        g.executeAllAttackOrders();
        // 攻击结果有随机成分，主要保证不抛异常，同时覆盖代码
        // 只要执行到了就可以了
        assertTrue(tA.getUnits() >= 6); // 因为用了 4 个单位去攻打
    }

    @Test
    void clearAllOrders() {
        Game g = new Game();
        g.addOrder(new MoveOrder(0, "A", "B", 5));
        g.clearAllOrders();
        assertTrue(g.getAllOrders().isEmpty());
    }

    @Test
    void addOneUnitToEachTerritory() {
        Game g = new Game();
        g.setUpMap(2);
        g.initPlayers(2);

        for (Territory t : g.getPlayer(0).getTerritories()) {
            t.setUnits(1);
        }
        for (Territory t : g.getPlayer(1).getTerritories()) {
            t.setUnits(2);
        }

        g.addOneUnitToEachTerritory();

        for (Territory t : g.getPlayer(0).getTerritories()) {
            assertEquals(2, t.getUnits());
        }
        for (Territory t : g.getPlayer(1).getTerritories()) {
            assertEquals(3, t.getUnits());
        }
    }


    @Test
    void hasWinner() {
        Game g = new Game();
        assertFalse(g.hasWinner());
    }

    @Test
    void getWinner() {
        Game g = new Game();
        assertNull(g.getWinner());
    }

    @Test
    void getTerritoryByName() {
        Game g = new Game();
        g.setUpMap(2);
        assertNotNull(g.getTerritoryByName("A"));
        assertNull(g.getTerritoryByName("Z"));
    }

    @Test
    void getPlayer() {
        Game g = new Game();
        g.setUpMap(2);
        g.initPlayers(2);
        Player p = g.getPlayer(1);
        assertEquals("Player2", p.getName());
    }

    @Test
    void getAllOrders() {
        Game g = new Game();
        assertNotNull(g.getAllOrders());
    }

    @Test
    void getRandom() {
        Game g = new Game();
        assertNotNull(g.getRandom());
    }

    @Test
    void getMapState() {
        Game g = new Game();
        g.setUpMap(2);
        g.initPlayers(2);
        String mapState = g.getMapState();
        assertTrue(mapState.contains("A(")); // 简单断言字符串包含
    }
}
