package com.ren.playerlogger.log.events;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;

// 玩家事件基类
public abstract class PlayerEvent extends AbstractLogEvent {
    private final ServerPlayerEntity player;

    public PlayerEvent(ServerPlayerEntity player, String message) {
        super(player.getName().getString(), message);
        this.player = player;
    }

    public ServerPlayerEntity getPlayer() {
        return player;
    }

    // 将BlockPos格式化为可读坐标
    protected static String formatBlockPos(BlockPos pos) {
        if (pos == null) return "未知位置";
        return "(" + pos.getX() + ", " + pos.getY() + ", " + pos.getZ() + ")";
    }

    // 处理混淆的类名并转换为具体容器名称
    protected static String formatContainerName(String containerName) {
        if (containerName == null) {
            return "容器";
        }

        // 处理混淆类名
        if (containerName.startsWith("class_")) {
            // 根据类名包含的关键字识别容器类型
            if (containerName.contains("Chest")) return "箱子";
            if (containerName.contains("Barrel")) return "木桶";
            if (containerName.contains("EnderChest")) return "末影箱";
            if (containerName.contains("Shulker")) return "潜影盒";
            if (containerName.contains("Dispenser")) return "发射器";
            if (containerName.contains("Dropper")) return "投掷器";
            if (containerName.contains("Hopper")) return "漏斗";
            if (containerName.contains("Furnace")) return "熔炉";
            if (containerName.contains("Blast")) return "高炉";
            if (containerName.contains("Smoker")) return "烟熏炉";
            return "容器";
        }

        return containerName;
    }

    // 格式化位置字符串
    protected static String formatPosition(String position) {
        if (position == null || position.isEmpty()) {
            return "";
        }
        // 替换class_2338格式
        if (position.contains("class_2338")) {
            return position.replaceAll("class_2338\\{x=(-?\\d+), y=(-?\\d+), z=(-?\\d+)\\}", "($1, $2, $3)");
        }
        return position;
    }

    // 聊天事件
    public static class Chat extends PlayerEvent {
        public Chat(ServerPlayerEntity player, String message) {
            super(player, "发送了消息: " + message);
        }

        @Override
        public String getEventType() {
            return "聊天";
        }
    }

    // 登录事件
    public static class Login extends PlayerEvent {
        public Login(ServerPlayerEntity player) {
            super(player, "登录");
        }

        @Override
        public String getEventType() {
            return "登录";
        }
    }

    // 登出事件
    public static class Logout extends PlayerEvent {
        public Logout(ServerPlayerEntity player) {
            super(player, "登出");
        }

        @Override
        public String getEventType() {
            return "登出";
        }
    }

    // 方块破坏事件
    public static class BlockBreak extends PlayerEvent {
        public BlockBreak(ServerPlayerEntity player, BlockPos pos, String blockName) {
            super(player, "破坏了方块 " + blockName + " 在 " + formatBlockPos(pos));
        }

        @Override
        public String getEventType() {
            return "破坏方块";
        }
    }

    // 方块放置事件
    public static class BlockPlace extends PlayerEvent {
        public BlockPlace(ServerPlayerEntity player, BlockPos pos, String blockName) {
            super(player, "放置了方块 " + blockName + " 在 " + formatBlockPos(pos));
        }

        @Override
        public String getEventType() {
            return "放置方块";
        }
    }

    // 方块交互事件
    public static class BlockInteract extends PlayerEvent {
        public BlockInteract(ServerPlayerEntity player, BlockPos pos, String blockName, String action) {
            super(player, action + "了方块 " + blockName + " 在 " + formatBlockPos(pos));
        }

        @Override
        public String getEventType() {
            return "方块交互";
        }
    }

    // 物品使用事件
    public static class ItemUse extends PlayerEvent {
        public ItemUse(ServerPlayerEntity player, String itemName) {
            super(player, "使用了物品 " + itemName);
        }

        @Override
        public String getEventType() {
            return "使用物品";
        }
    }

    // 物品拾取事件
    public static class ItemPickup extends PlayerEvent {
        public ItemPickup(ServerPlayerEntity player, String itemName, int count) {
            super(player, "拾取了 " + count + " 个 " + itemName);
        }

        @Override
        public String getEventType() {
            return "拾取物品";
        }
    }

    // 物品丢弃事件
    public static class ItemDrop extends PlayerEvent {
        public ItemDrop(ServerPlayerEntity player, String itemName, int count) {
            super(player, "丢弃了 " + count + " 个 " + itemName);
        }

        @Override
        public String getEventType() {
            return "丢弃物品";
        }
    }

    // 实体交互事件
    public static class EntityInteract extends PlayerEvent {
        public EntityInteract(ServerPlayerEntity player, String entityName, String actionType, String position) {
            super(player, actionType + "了 " + entityName +
                    (position != null && !position.isEmpty() ? " 在 " + formatPosition(position) : ""));
        }

        @Override
        public String getEventType() {
            return "实体交互";
        }
    }

    // 实体攻击事件
    public static class EntityAttack extends PlayerEvent {
        public EntityAttack(ServerPlayerEntity player, String entityName, String position, float damage) {
            super(player, "攻击了 " + entityName + " 造成 " + damage + " 点伤害" +
                    (position != null && !position.isEmpty() ? " 在 " + formatPosition(position) : ""));
        }

        @Override
        public String getEventType() {
            return "攻击实体";
        }
    }

    // 物品移动事件（容器交互）
    public static class ItemMove extends PlayerEvent {
        public ItemMove(ServerPlayerEntity player, String containerName, String itemName, int count, String action, BlockPos containerPos) {
            super(player, action + " " + count + " 个 " + itemName + " 在 " + formatContainerName(containerName) +
                    (containerPos != null ? " 在 " + formatBlockPos(containerPos) : ""));
        }

        @Override
        public String getEventType() {
            return "物品移动";
        }
    }

    // 容器访问事件
    public static class ContainerAccess extends PlayerEvent {
        public ContainerAccess(ServerPlayerEntity player, String containerName, BlockPos pos) {
            super(player, "打开了容器 " + formatContainerName(containerName) + " 在 " + formatBlockPos(pos));
        }

        @Override
        public String getEventType() {
            return "容器访问";
        }
    }

    // 玩家死亡事件
    public static class PlayerDeath extends PlayerEvent {
        public PlayerDeath(ServerPlayerEntity player, String cause, String position) {
            super(player, "死亡，原因: " + cause +
                    (position != null && !position.isEmpty() ? " 在 " + formatPosition(position) : ""));
        }

        @Override
        public String getEventType() {
            return "玩家死亡";
        }
    }
}