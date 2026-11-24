package com.ienuclear.reactor.item;

import com.ienuclear.reactor.blockentity.ReactorControllerBlockEntity;
import com.ienuclear.reactor.registry.ModBlocks;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * Промисловий дозиметр (Лічильник Гейгера):
 * Сканує радіус 12 блоків на наявність активних реакторів та розплавленого коріуму,
 * розраховує радіаційний фон у мЗв/год (mSv/h) та виводить діагностику в чат.
 */
public class GeigerCounterItem extends Item {
    public GeigerCounterItem(Properties properties) {
        super(properties);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, net.minecraft.world.entity.Entity entity, int slotId, boolean isSelected) {
        if (!level.isClientSide && entity instanceof Player player && level.getGameTime() % 20 == 0) {
            BlockPos center = player.blockPosition();
            for (BlockPos pos : BlockPos.betweenClosed(center.offset(-8, -4, -8), center.offset(8, 4, 8))) {
                if (level.getBlockState(pos).is(ModBlocks.MOLTEN_CORIUM.get())
                        || (level.getBlockEntity(pos) instanceof ReactorControllerBlockEntity r && r.isActive())) {
                    level.playSound(null, center, SoundEvents.UI_BUTTON_CLICK.value(), SoundSource.PLAYERS, 0.35f, 1.8f);
                    break;
                }
            }
        }
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide) {
            BlockPos center = player.blockPosition();
            double totalRadiation = 0.05; // Природний фон 0.05 мЗв/год
            ReactorControllerBlockEntity nearestReactor = null;
            double nearestDistSq = Double.MAX_VALUE;
            int coriumBlocks = 0;

            int radius = 12;
            for (BlockPos pos : BlockPos.betweenClosed(
                    center.offset(-radius, -6, -radius),
                    center.offset(radius, 6, radius))) {
                if (level.getBlockState(pos).is(ModBlocks.MOLTEN_CORIUM.get())) {
                    coriumBlocks++;
                    double dist = Math.max(1.0, Math.sqrt(pos.distSqr(center)));
                    totalRadiation += 45.0 / (dist * dist);
                } else {
                    BlockEntity be = level.getBlockEntity(pos);
                    if (be instanceof ReactorControllerBlockEntity reactor) {
                        double distSq = pos.distSqr(center);
                        if (distSq < nearestDistSq) {
                            nearestDistSq = distSq;
                            nearestReactor = reactor;
                        }
                        double dist = Math.max(1.0, Math.sqrt(distSq));
                        totalRadiation += reactor.getRadiationLevel() / dist;
                    }
                }
            }

            float pitch = (float) Math.min(2.0, 0.8 + totalRadiation * 0.05);
            level.playSound(null, center, SoundEvents.UI_BUTTON_CLICK.value(), SoundSource.PLAYERS, 0.8f, pitch);

            ChatFormatting color = totalRadiation >= 15.0 ? ChatFormatting.RED
                    : (totalRadiation >= 2.0 ? ChatFormatting.GOLD : ChatFormatting.GREEN);

            player.displayClientMessage(
                    Component.literal(String.format("[Дозиметр] Фон: %.2f мЗв/год", totalRadiation))
                            .withStyle(color),
                    true
            );

            if (nearestReactor != null) {
                player.sendSystemMessage(Component.literal(
                        String.format("§6[Гейгер]§r Реактор (Ядер: %d): §e%d°C§r | Стрижні: §b%d%%§r | Пара: §f+%d mB/t§r | Фон: §c%.2f мЗв/год",
                                nearestReactor.getCoreCount(),
                                nearestReactor.getTemperature(),
                                nearestReactor.getControlRodInsertion(),
                                nearestReactor.getCurrentSteamPerTick(),
                                totalRadiation)
                ));
            } else if (coriumBlocks > 0) {
                player.sendSystemMessage(Component.literal(
                        String.format("§c[Гейгер] УВАГА! Виявлено %d бл. розплавленого коріуму! Фон: %.2f мЗв/год",
                                coriumBlocks, totalRadiation)
                ));
            }

            player.getCooldowns().addCooldown(this, 15);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }
}
