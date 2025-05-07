package com.ren.playerlogger.event;

import com.ren.playerlogger.PlayerLogger;
import com.ren.playerlogger.config.ConfigManager;
import com.ren.playerlogger.log.LogManager;
import com.ren.playerlogger.log.events.PlayerEvent;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.world.World;

// 实体交互监听器
public class EntityInteractionListener {
    private final LogManager logManager;
    private final ConfigManager configManager;

    public EntityInteractionListener(LogManager logManager, ConfigManager configManager) {
        this.logManager = logManager;
        this.configManager = configManager;
    }

    // 处理实体攻击
    public ActionResult onAttackEntity(ServerPlayerEntity player, World world, Hand hand, Entity entity, EntityHitResult hitResult) {
        if (configManager.isEnabled() && configManager.shouldLogEntityInteraction()) {
            try {
                // 获取实体信息
                String entityName = entity.getType().getName().getString();
                String position = entity.getBlockPos().toString();

                // 记录实体攻击事件
                logManager.logEvent(new PlayerEvent.EntityAttack(
                        player,
                        entityName,
                        position,
                        0.0f // 默认伤害值，实际伤害在伤害事件中计算
                ));
            } catch (Exception e) {
                PlayerLogger.LOGGER.error("记录实体攻击事件时出错", e);
            }
        }

        return ActionResult.PASS;
    }

    // 处理实体交互
    public ActionResult onInteractEntity(ServerPlayerEntity player, Entity entity, Hand hand) {
        if (configManager.isEnabled() && configManager.shouldLogEntityInteraction()) {
            try {
                // 获取实体信息
                String entityName = entity.getType().getName().getString();
                String position = entity.getBlockPos().toString();

                // 记录实体交互事件
                logManager.logEvent(new PlayerEvent.EntityInteract(
                        player,
                        entityName,
                        "交互",
                        position
                ));
            } catch (Exception e) {
                PlayerLogger.LOGGER.error("记录实体交互事件时出错", e);
            }
        }

        return ActionResult.PASS;
    }

    // 处理实体伤害
    public void onEntityDamage(Entity entity, DamageSource source, float amount) {
        if (configManager.isEnabled() && configManager.shouldLogEntityInteraction()) {
            try {
                // 只处理玩家造成的伤害
                if (source.getAttacker() instanceof ServerPlayerEntity) {
                    ServerPlayerEntity player = (ServerPlayerEntity) source.getAttacker();

                    // 获取实体信息
                    String entityName = entity.getType().getName().getString();
                    String position = entity.getBlockPos().toString();

                    // 记录实体伤害事件
                    logManager.logEvent(new PlayerEvent.EntityAttack(
                            player,
                            entityName,
                            position,
                            amount
                    ));
                }
            } catch (Exception e) {
                PlayerLogger.LOGGER.error("记录实体伤害事件时出错", e);
            }
        }
    }
}