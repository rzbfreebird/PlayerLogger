package com.ren.playerlogger.mixin;

import com.ren.playerlogger.PlayerLogger;
import com.ren.playerlogger.log.events.PlayerEvent;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {

    @Inject(method = "onDeath", at = @At("HEAD"))
    private void onEntityDeath(DamageSource damageSource, CallbackInfo ci) {
        try {
            // 如果死亡的实体是玩家
            if ((Object)this instanceof ServerPlayerEntity) {
                ServerPlayerEntity player = (ServerPlayerEntity)(Object)this;

                // 检查是否应该记录玩家伤害/死亡
                if (PlayerLogger.getConfigManager().shouldLogPlayerDamage()) {
                    // 获取死亡原因和位置
                    String cause = damageSource.getName();
                    String position = player.getPos().toString();

                    // 记录玩家死亡事件
                    PlayerLogger.getLogManager().logEvent(new PlayerEvent.PlayerDeath(
                            player,
                            cause,
                            position
                    ));
                }
            }
        } catch (Exception e) {
            PlayerLogger.LOGGER.error("记录玩家死亡事件时出错", e);
        }
    }
}