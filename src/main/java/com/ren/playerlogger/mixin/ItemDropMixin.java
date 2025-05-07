package com.ren.playerlogger.mixin;

import com.ren.playerlogger.PlayerLogger;
import com.ren.playerlogger.log.events.PlayerEvent;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
public class ItemDropMixin {

    @Inject(method = "dropItem(Lnet/minecraft/item/ItemStack;ZZ)Lnet/minecraft/entity/ItemEntity;", at = @At("HEAD"))
    private void onItemDrop(ItemStack stack, boolean throwRandomly, boolean retainOwnership, CallbackInfoReturnable<ItemEntity> cir) {
        try {
            if (!stack.isEmpty() &&
                    (Object)this instanceof ServerPlayerEntity &&
                    PlayerLogger.getConfigManager() != null &&
                    PlayerLogger.getLogManager() != null &&
                    PlayerLogger.getConfigManager().shouldLogItemDrop()) {

                ServerPlayerEntity player = (ServerPlayerEntity)(Object)this;
                String itemName = stack.getName().getString();

                PlayerLogger.getLogManager().logEvent(new PlayerEvent.ItemDrop(
                        player,
                        itemName,
                        stack.getCount()
                ));
            }
        } catch (Exception e) {
            PlayerLogger.LOGGER.error("记录物品丢弃事件时出错: " + e.getMessage());
            e.printStackTrace();
        }
    }
}