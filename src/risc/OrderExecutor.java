package risc;

import java.util.*;

/**
 * Handles the execution of move and attack orders.
 */
public class OrderExecutor {
    private final Game game;

    public OrderExecutor(Game game) {
        this.game = game;
    }

    public void executeMoveOrders() {
        List<MoveOrder> moves = new ArrayList<>();
        for (Order o : game.getAllOrders()) {
            if (o instanceof MoveOrder) {
                moves.add((MoveOrder) o);
            }
        }
        for (MoveOrder m : moves) {
            if (!validateMove(m)) {
                continue;
            }
            Territory src = game.getTerritoryByName(m.getSourceName());
            src.setUnits(src.getUnits() - m.getNumUnits());
            Territory dest = game.getTerritoryByName(m.getDestName());
            dest.setUnits(dest.getUnits() + m.getNumUnits());
        }
    }

    public void executeAttackOrders() {
        List<AttackOrder> mutualOrders = new ArrayList<>();
        List<AttackOrder> allAttackOrders = new ArrayList<>();
        for (Order o : game.getAllOrders()) {
            if (o instanceof AttackOrder) {
                AttackOrder ao = (AttackOrder) o;
                if (validateAttack(ao)) {
                    allAttackOrders.add(ao);
                }
            }
        }
        List<AttackOrder> processedOrders = new ArrayList<>();
        for (AttackOrder ao : allAttackOrders) {
            if (processedOrders.contains(ao))
                continue;
            AttackOrder reverse = null;
            for (AttackOrder other : allAttackOrders) {
                if (other == ao)
                    continue;
                if (processedOrders.contains(other))
                    continue;
                if (other.getSourceName().equalsIgnoreCase(ao.getDestName())
                        && other.getDestName().equalsIgnoreCase(ao.getSourceName())) {
                    reverse = other;
                    break;
                }
            }
            if (reverse != null) {
                Territory src = game.getTerritoryByName(ao.getSourceName());
                Territory dest = game.getTerritoryByName(ao.getDestName());
                if (src != null && dest != null) {
                    if (src.getUnits() == ao.getNumUnits() && dest.getUnits() == reverse.getNumUnits()) {
                        Player attackerForDest = game.getPlayer(ao.getPlayerID());
                        Player attackerForSrc = game.getPlayer(reverse.getPlayerID());

                        dest.setOwner(attackerForDest);
                        dest.setUnits(ao.getNumUnits());
                        attackerForDest.addTerritory(dest);

                        src.setOwner(attackerForSrc);
                        src.setUnits(reverse.getNumUnits());
                        attackerForSrc.addTerritory(src);

                        processedOrders.add(ao);
                        processedOrders.add(reverse);
                        mutualOrders.add(ao);
                        mutualOrders.add(reverse);
                    }
                }
            }
        }
        game.getAllOrders().removeAll(mutualOrders);

        Map<String, List<AttackOrder>> attacksByTarget = new HashMap<>();
        for (Order o : game.getAllOrders()) {
            if (o instanceof AttackOrder) {
                AttackOrder ao = (AttackOrder) o;
                if (!validateAttack(ao)) {
                    continue;
                }
                attacksByTarget.computeIfAbsent(ao.getDestName(), k -> new ArrayList<>()).add(ao);
            }
        }

        for (String targetName : attacksByTarget.keySet()) {
            Territory target = game.getTerritoryByName(targetName);
            List<AttackOrder> attackers = attacksByTarget.get(targetName);
            Collections.shuffle(attackers, game.getRandom());

            for (AttackOrder ao : attackers) {
                Territory src = game.getTerritoryByName(ao.getSourceName());
                target = game.getTerritoryByName(targetName);
                Player attacker = game.getPlayer(ao.getPlayerID());

                if (src.getUnits() < ao.getNumUnits()) {
                    continue;
                }
                src.setUnits(src.getUnits() - ao.getNumUnits());

                if (target.getUnits() == 0 || !target.getOwner().isAlive()) {
                    target.setOwner(attacker);
                    target.setUnits(ao.getNumUnits());
                    attacker.addTerritory(target);
                    continue;
                }

                int attackerUnits = ao.getNumUnits();
                int defenderUnits = target.getUnits();
                Player defender = target.getOwner();

                while (attackerUnits > 0 && defenderUnits > 0) {
                    int attackRoll = DiceRoller.rollD20();
                    int defenseRoll = DiceRoller.rollD20();
                    if (attackRoll > defenseRoll) {
                        defenderUnits--;
                    } else {
                        attackerUnits--;
                    }
                }
                if (attackerUnits > 0) {
                    target.setOwner(attacker);
                    target.setUnits(attackerUnits);
                    defender.removeTerritory(target);
                    attacker.addTerritory(target);
                } else {
                    target.setUnits(defenderUnits);
                }
            }
        }
    }

    private boolean validateMove(MoveOrder move) {
        Territory src = game.getTerritoryByName(move.getSourceName());
        Territory dest = game.getTerritoryByName(move.getDestName());
        Player p = game.getPlayer(move.getPlayerID());
        if (src == null || dest == null) return false;
        if (!src.getOwner().equals(p) || !dest.getOwner().equals(p)) return false;
        if (src.getUnits() < move.getNumUnits()) return false;
        return canReach(src, dest, p);
    }

    private boolean canReach(Territory src, Territory dest, Player p) {
        Set<Territory> visited = new HashSet<>();
        Queue<Territory> queue = new LinkedList<>();
        queue.add(src);
        visited.add(src);
        while (!queue.isEmpty()) {
            Territory current = queue.poll();
            if (current.equals(dest)) {
                return true;
            }
            for (Territory nbr : current.getNeighbors()) {
                if (!visited.contains(nbr) && nbr.getOwner().equals(p)) {
                    visited.add(nbr);
                    queue.add(nbr);
                }
            }
        }
        return false;
    }

    private boolean validateAttack(AttackOrder ao) {
        Territory src = game.getTerritoryByName(ao.getSourceName());
        Territory dest = game.getTerritoryByName(ao.getDestName());
        Player p = game.getPlayer(ao.getPlayerID());
        if (src == null || dest == null)
            return false;
        if (!src.getOwner().equals(p))
            return false;
        if (src.getUnits() < ao.getNumUnits())
            return false;
        if (!src.getNeighbors().contains(dest))
            return false;
        if (dest.getOwner().equals(p))
            return false;
        return true;
    }
}
