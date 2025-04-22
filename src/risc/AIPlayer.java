package risc;

/**
 * Specifically marks a computer player (DeepSeekBot).
 * Inherits all data structures from Player, overriding only isAI() for server identification.
 */
public class AIPlayer extends Player {
    public AIPlayer(int id, String name) {
        super(id, name);
    }

    @Override
    public boolean isAI() {
        return true;
    }
}
