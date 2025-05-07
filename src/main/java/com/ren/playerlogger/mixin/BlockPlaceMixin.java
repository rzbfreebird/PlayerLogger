package com.ren.playerlogger.mixin;

import com.ren.playerlogger.PlayerLogger;
import com.ren.playerlogger.log.events.PlayerEvent;
import net.minecraft.block.BlockState;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockItem.class)  // 指定目标类为BlockItem
public class BlockPlaceMixin {

    @Inject(method = "place", at = @At("RETURN"))
    private void onBlockPlace(ItemPlacementContext context, CallbackInfoReturnable<ActionResult> cir) {
        // 只处理放置成功的情况
        if (cir.getReturnValue() == null || cir.getReturnValue() == ActionResult.FAIL) {
            return;
        }

        // 确保配置允许记录方块放置
        if (PlayerLogger.getConfigManager() != null && PlayerLogger.getConfigManager().shouldLogBlockPlace()) {
            // 获取放置信息
            try {
                if (context.getPlayer() instanceof ServerPlayerEntity) {
                    ServerPlayerEntity player = (ServerPlayerEntity) context.getPlayer();
                    BlockPos pos = context.getBlockPos();

                    // 直接从世界获取方块状态，而不是从返回值
                    BlockState state = context.getWorld().getBlockState(pos);
                    String blockName = Text.translatable(state.getBlock().getTranslationKey()).getString();

                    // 使用PlayerEvent.BlockPlace事件
                    PlayerLogger.getLogManager().logEvent(new PlayerEvent.BlockPlace(
                            player,
                            pos,
                            blockName
                    ));
                }
            } catch (Exception e) {
                PlayerLogger.LOGGER.error("记录方块放置事件时出错", e);
            }
        }
    }
}