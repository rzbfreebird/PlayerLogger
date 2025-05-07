package com.ren.playerlogger.log.events;

import net.minecraft.util.math.BlockPos;

import java.util.Date;

// 方块事件类，实现LogEvent接口
public class BlockEvent implements LogEvent {
    private final Date timestamp;
    private final String playerName;
    private final String description; // 改为description以匹配接口
    private final BlockPos pos;
    private final String blockName;
    private final String action;

    public BlockEvent(String playerName, BlockPos pos, String blockName, String action) {
        this.timestamp = new Date();
        this.playerName = playerName;
        this.pos = pos;
        this.blockName = blockName;
        this.action = action;
        this.description = playerName + " " + action + "了方块 " + blockName + " 在 " + pos;
    }

    @Override
    public String getPlayerName() {
        return playerName;
    }

    // 实现getDescription()方法替代getMessage()
    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public Date getTimestamp() {
        return timestamp;
    }

    // 实现getEventType()方法
    @Override
    public String getEventType() {
        return "方块" + action;
    }

    public BlockPos getPos() {
        return pos;
    }

    public String getBlockName() {
        return blockName;
    }

    public String getAction() {
        return action;
    }
}