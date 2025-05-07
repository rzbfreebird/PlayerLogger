package com.ren.playerlogger.compat;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.block.Block;
import net.minecraft.util.math.BlockPos;

/**
 * 处理不同MC版本之间API差异的兼容性类
 */
public class VersionCompat {
    /**
     * 获取方块名称（避免版本差异）
     */
    public static String getBlockName(Block block) {
        try {
            return block.getName().getString();
        } catch (Exception e) {
            // 备用方法
            return block.toString();
        }
    }

    /**
     * 获取物品名称（避免版本差异）
     */
    public static String getItemName(ItemStack stack) {
        try {
            return stack.getName().getString();
        } catch (Exception e) {
            // 备用方法
            return stack.getItem().toString();
        }
    }

    // 更多兼容性方法...
}