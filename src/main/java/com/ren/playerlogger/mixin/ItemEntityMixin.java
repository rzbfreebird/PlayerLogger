package com.ren.playerlogger.mixin;

import com.ren.playerlogger.PlayerLogger;
import com.ren.playerlogger.log.events.PlayerEvent;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemEntity.class)
public abstract class ItemEntityMixin {
    @Shadow public abstract ItemStack getStack();

    // 监听物品拾取
    @Inject(method = "onPlayerCollision", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/entity/player/PlayerEntity;sendPickup(Lnet/minecraft/entity/Entity;I)V"))
    private void onItemPickup(PlayerEntity player, CallbackInfo ci) {
        // 修改这里：使用getWorld()方法替代直接访问world字段
        if (!player.getWorld().isClient && player instanceof ServerPlayerEntity) {
            ServerPlayerEntity serverPlayer = (ServerPlayerEntity) player;
            ItemStack stack = this.getStack();

            if (!stack.isEmpty() && PlayerLogger.getConfigManager().shouldLogItemPickup()) {
                PlayerLogger.getLogManager().logEvent(new PlayerEvent.ItemPickup(
                        serverPlayer,
                        stack.getName().getString(),
                        stack.getCount()
                ));
            }
        }
    }
}