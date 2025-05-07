package com.ren.playerlogger.mixin;

import com.ren.playerlogger.PlayerLogger;
import com.ren.playerlogger.log.events.PlayerEvent;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerEntity.class)
public class ServerPlayerEntityMixin {

    @Inject(method = "closeHandledScreen", at = @At("HEAD"))
    private void onCloseScreen(CallbackInfo ci) {
        try {
            // 确保配置允许记录容器访问
            if (PlayerLogger.getConfigManager() != null &&
                    PlayerLogger.getConfigManager().shouldLogContainerAccess()) {

                // 获取当前玩家
                ServerPlayerEntity player = (ServerPlayerEntity)(Object)this;

                // 获取当前打开的容器
                ScreenHandler handler = player.currentScreenHandler;
                if (handler != null && handler != player.playerScreenHandler) {
                    String containerName = handler.getClass().getSimpleName() + " (关闭)";

                    // 使用玩家当前位置作为BlockPos
                    BlockPos playerPos = player.getBlockPos();

                    // 记录容器关闭事件
                    PlayerLogger.getLogManager().logEvent(new PlayerEvent.ContainerAccess(
                            player,
                            containerName,
                            playerPos  // 使用玩家位置，而不是字符串"关闭"
                    ));
                }
            }
        } catch (Exception e) {
            PlayerLogger.LOGGER.error("记录容器关闭事件时出错", e);
        }
    }
}