package risc;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 将 DeepSeek 模型输出转成具体 Order 并注入 Game，
 * 并在每回合结束时广播 AI 玩家本回合的全部指令。
 */
public class AIController {

    private final Game game;
    private final AIPlayer ai;
    private final DeepSeekClient client = new DeepSeekClient();

    /* 指令正则 */
    private static final Pattern MOVE_P = Pattern.compile("^M\\s+(\\w+)\\s+(\\w+)\\s+(\\d+)\\s+(\\d+)", Pattern.CASE_INSENSITIVE);
    private static final Pattern ATT_P  = Pattern.compile("^A\\s+(\\w+)\\s+(\\w+)\\s+(\\d+)\\s+(\\d+)", Pattern.CASE_INSENSITIVE);
    private static final Pattern UPG_P  = Pattern.compile("^U\\s+(\\w+)\\s+(\\d+)\\s+(\\d+)\\s+(\\d+)", Pattern.CASE_INSENSITIVE);
    private static final Pattern TECH_P = Pattern.compile("^T$", Pattern.CASE_INSENSITIVE);
    private static final Pattern FA_P   = Pattern.compile("^FA\\s+(\\w+)", Pattern.CASE_INSENSITIVE);

    public AIController(Game game, AIPlayer ai) {
        this.game = game;
        this.ai   = ai;
    }

    /* -------- 初始布兵（平均分配示例） -------- */
    public void doInitialPlacement() {
        int remain = game.getInitialUnits();
        List<Territory> lands = ai.getTerritories();
        int per = remain / lands.size();
        int extra = remain % lands.size();
        for (Territory t : lands) {
            int put = per + (extra-- > 0 ? 1 : 0);
            t.addUnits(ai.getId(), 0, put);
        }
        game.broadcast("DeepSeekBot 已完成初始布兵。");
    }

    /* -------- 每回合生成指令并统一广播 -------- */
    public void generateTurnOrders() {
        String prompt = buildPrompt();
        final int MAX_RETRIES = 3;
        int attempt = 0;
        List<String> lines;

        while (attempt < MAX_RETRIES) {
            attempt++;
            String reply = client.chat(prompt);
            lines = parseContent(reply);

            // 检查是否包含终止标记 D
            boolean hasDoneMarker = lines.stream().anyMatch(ln -> ln.equalsIgnoreCase("D"));
            if (!hasDoneMarker) {
                game.broadcast("DeepSeekBot 未在指令末尾输出 D，重新生成指令（第 " + attempt + " 次尝试）…");
                continue;
            }

            // 解析并应用指令
            List<String> accepted = new ArrayList<>();
            for (String ln : lines) {
                String t = ln.trim();
                if (t.equalsIgnoreCase("D")) {
                    // 正常遇到 D 就停止读取后续行
                    break;
                }
                if (applyLine(t)) {
                    accepted.add(t);
                }
            }

            // 广播最终采纳的指令
            if (!accepted.isEmpty()) {
                StringBuilder msg = new StringBuilder("【DeepSeekBot 指令】\n");
                accepted.forEach(l -> msg.append("  ").append(l).append('\n'));
                game.broadcast(msg.toString());
            } else {
                game.broadcast("DeepSeekBot 本回合未生成任何可执行指令，仅输出了 D。");
            }
            return;
        }

        // 重试多次仍不合格时，跳过本回合
        game.broadcast("DeepSeekBot 多次未按格式输出 D，跳过本回合指令。");
    }



    /* -------- Prompt 生成 -------- */
    private String buildPrompt() {
        StringBuilder sb = new StringBuilder();
        // 基本指令说明
        sb.append("你是 DeepSeekBot，需要在战棋游戏 RISC 中扮演高水平电脑玩家，输出若干合法指令：\n");
        // 当前地图状态
        sb.append(game.getMapState()).append("\n");
        // AI 自身状态
        sb.append("===My Status===\n")
                .append("Food=").append(ai.getFood())
                .append(" Tech=").append(ai.getTech())
                .append(" MaxTechLevel=").append(ai.getMaxTechLevel())
                .append(" Allies=").append(ai.getAllies()).append("\n\n");

        // 游戏规则讲解
        sb.append("## 一、游戏核心要素\n")
                .append("1. **玩家（Player）**\n")
                .append("   - 数量：2–5 人（可扩展）。\n")
                .append("   - 胜利条件：控制所有领地；失败条件：失去所有领地。\n")
                .append("   - 属性：\n")
                .append("     - **食物（Food）**：用于执行移动和攻击消耗。\n")
                .append("     - **科技（Tech）**：用于升级科技等级和部队升级。\n")
                .append("     - **最大科技等级（MaxTechLevel）**：初始为1，可通过“科技升级”提升。\n")
                .append("2. **领地（Territory）**\n")
                .append("   - 每个领地由且仅由一名玩家拥有。\n")
                .append("   - 驻军分等级，可存在多种类型，每种类型有数量。\n")
                .append("   - 属性：邻接关系构成连通图；Size 决定移动代价；每回合产出 food/tech 并额外+1 基础单位。\n")
                .append("3. **资源**\n")
                .append("   - **食物（Food）**：移动消耗=路径总Size×单位数；攻击消耗=1×单位数。\n")
                .append("   - **科技（Tech）**：单位升级按差价消耗；科技升级按固定表消耗，次回合生效。\n\n")
                .append("------\n\n")
                .append("## 二、游戏流程\n")
                .append("### 1. 初始设置\n")
                .append("- 平均分配领地所有权；同时、保密地放置相同数量的初始基础单位。\n\n")
                .append("### 2. 回合结构（同时进行）\n")
                .append("1. **Issue Orders（下令阶段）**：可重复发以下命令，直到输入 `D`：\n")
                .append("   - `M <src> <dst> <level> <num>`：移动\n")
                .append("   - `A <src> <dst> <level> <num>`：攻击（仅限相邻）\n")
                .append("   - `U <territory> <curLvl> <tgtLvl> <num>`：部队升级\n")
                .append("   - `T`：科技等级升级\n")
                .append("   - `FA <playerName>`：结盟请求（需双向同意）\n\n")
                .append("2. **Execute Orders（执行阶段）**：顺序固定\n")
                .append("   1. **Move Orders**：离开发出领地，不参与原地防御；到达后参与防御。\n")
                .append("   2. **Attack Orders**：所有攻击部队同时离开并同时抵达；同目标多方按随机顺序决斗。\n")
                .append("   3. **Alliance Orders**：双向同意才生效；盟友可互通道移动；攻击盟友即破盟并召回驻军。\n")
                .append("   4. **Upgrade Orders**：按科技差价升级部队。\n")
                .append("   5. **Tech Upgrade Orders**：提升 MaxTechLevel，一回合后生效。\n\n")
                .append("3. **End Turn（结束阶段）**：\n")
                .append("   - **产资源**：按领地 Size 增加 food/tech。\n")
                .append("   - **产兵**：每领地+1基础单位。\n")
                .append("   - **状态更新**：失去所有领地者出局；仅存一位者胜利。\n\n")
                .append("------\n\n")
                .append("## 三、战斗决斗规则\n")
                .append("- 攻防双方各 Roll D20，一高一低扣1单位（平局防守方胜）。\n")
                .append("- 部队按等级有 Bonus 加成；先比最高 vs 最低，再比最低 vs 最高。\n")
                .append("- 多源合并；多玩家冲突按随机顺序逐一解决。\n\n")
                .append("------\n\n")
                .append("## 四、升级系统（Evolution 2）\n")
                .append("1. **部队升级**：Level0→1:3；1→2:8；2→3:19；3→4:25；4→5:35；5→6:50，可跨级按差价一次完成。\n")
                .append("2. **科技升级**：1→2:50；2→3:75；3→4:125；4→5:200；5→6:300，下回合生效。\n\n")
                .append("------\n\n")
                // 最后再附上指令格式
                .append("===合法指令格式===\n")
                .append("M <src> <dest> <level> <numUnits>\n")
                .append("A <src> <dest> <level> <numUnits>\n")
                .append("U <territory> <curLvl> <tgtLvl> <numUnits>\n")
                .append("T\n")
                .append("FA <username>\n")
                .append("D   （必须最后输出）\n");

        return sb.toString();
    }


    /* -------- 解析 DeepSeek JSON，为演示仅做字符串切割 -------- */
    private List<String> parseContent(String json) {
        int idx = json.indexOf("\"content\"");
        if (idx < 0) return List.of();
        int colon  = json.indexOf(':', idx);
        int quote1 = json.indexOf('"', colon + 1) + 1;
        int quote2 = json.indexOf('"', quote1);
        String content = json.substring(quote1, quote2)
                .replace("\\n", "\n")
                .replace("\\\"", "\"");
        return Arrays.stream(content.split("\\R"))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
    }

    /* -------- 把单行文本转成 Order 注入 Game -------- */
    private boolean applyLine(String ln) {
        Matcher m;
        if ((m = MOVE_P.matcher(ln)).matches()) {
            game.addOrder(new MoveOrder(ai.getId(),
                    m.group(1), m.group(2),
                    Integer.parseInt(m.group(3)),
                    Integer.parseInt(m.group(4))));
            return true;
        }
        if ((m = ATT_P.matcher(ln)).matches()) {
            game.addOrder(new AttackOrder(ai.getId(),
                    m.group(1), m.group(2),
                    Integer.parseInt(m.group(3)),
                    Integer.parseInt(m.group(4))));
            return true;
        }
        if ((m = UPG_P.matcher(ln)).matches()) {
            game.addOrder(new UpgradeUnitOrder(ai.getId(),
                    m.group(1),
                    Integer.parseInt(m.group(2)),
                    Integer.parseInt(m.group(3)),
                    Integer.parseInt(m.group(4))));
            return true;
        }
        if (TECH_P.matcher(ln).matches()) {
            game.addOrder(new TechUpgradeOrder(ai.getId()));
            return true;
        }
        if ((m = FA_P.matcher(ln)).matches()) {
            game.addOrder(new AllianceOrder(ai.getId(), m.group(1)));
            return true;
        }
        return false; // 未识别
    }
}
