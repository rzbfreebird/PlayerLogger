package com.ren.playerlogger.mixin;

import com.ren.playerlogger.PlayerLogger;
import com.ren.playerlogger.log.events.PlayerEvent;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Mixin(Block.class)
public class BlockBreakMixin {
    // 缓存最近破坏的方块信息
    private static final Map<String, Long> RECENT_BREAKS = new HashMap<>();
    // 冷却时间 (毫秒)
    private static final long COOLDOWN_MS = 500;

    @Inject(method = "afterBreak", at = @At("TAIL"))
    private void onBlockBreak(World world, PlayerEntity player, BlockPos pos, BlockState state, BlockEntity blockEntity, ItemStack tool, CallbackInfo ci
            /* 其他参数 */) {
        if (world.isClient() || !(player instanceof ServerPlayerEntity)) {
            return;
        }

        ServerPlayerEntity serverPlayer = (ServerPlayerEntity) player;
        UUID playerUUID = player.getUuid();

        // 创建唯一标识符：玩家UUID + 方块位置
        String key = playerUUID + "-" + pos.toString();
        long currentTime = System.currentTimeMillis();

        // 检查是否在冷却期内
        Long lastBreakTime = RECENT_BREAKS.get(key);
        if (lastBreakTime != null && (currentTime - lastBreakTime) < COOLDOWN_MS) {
            // 在冷却期内，不记录
            return;
        }

        // 更新最后破坏时间
        RECENT_BREAKS.put(key, currentTime);

        // 清理过期记录
        if (RECENT_BREAKS.size() > 1000) {
            RECENT_BREAKS.entrySet().removeIf(entry ->
                    (currentTime - entry.getValue()) > 10000);
        }

        // 记录事件
        String blockName = state.getBlock().getName().getString();
        PlayerLogger.getLogManager().logEvent(
                new PlayerEvent.BlockBreak(serverPlayer, pos, blockName)
        );
    }
}