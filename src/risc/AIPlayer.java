package risc;

/**
 * 专门用于标识电脑玩家（DeepSeekBot）。
 * 其数据结构完全继承自 Player，仅重写 isAI() 供服务器识别。
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
