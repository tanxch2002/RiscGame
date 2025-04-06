package risc;

/**
 * 发起一次最大科技等级升级的指令。
 * 例如从 techLevel=1 升级到2 (花费50点科技)，下一回合生效。
 */
public class TechUpgradeOrder extends Order {

    public TechUpgradeOrder(int playerID) {
        // 这里 sourceName,destName,numUnits都不需要，所以传null或0
        super(playerID, null, null, 0);
    }

    // 也可添加目标等级字段，但通常由Player内部自己判断下个等级
}
