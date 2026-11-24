package com.ienuclear.reactor.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Розплавлений коріум — утворюється в кратері після ядерного вибуху (Meltdown).
 * Випромінює смертельну радіацію та обпікає при контакті.
 */
public class MoltenCoriumBlock extends Block {
    public MoltenCoriumBlock(Properties properties) {
        super(properties);
    }

    @Override
    public void stepOn(Level level, BlockPos pos, BlockState state, Entity entity) {
        if (!level.isClientSide && entity instanceof LivingEntity living) {
            if (living instanceof net.minecraft.world.entity.player.Player p
                    && com.ienuclear.reactor.blockentity.NuclearWasteCaskBlockEntity.hasFullHazmat(p)) {
                super.stepOn(level, pos, state, entity);
                return;
            }
            living.hurt(level.damageSources().hotFloor(), 3.0f);
            living.addEffect(new MobEffectInstance(MobEffects.POISON, 120, 1));
            living.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 160, 1));
            living.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 120, 0));
        }
        super.stepOn(level, pos, state, entity);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (random.nextInt(3) == 0) {
            double x = pos.getX() + random.nextDouble();
            double y = pos.getY() + 1.05;
            double z = pos.getZ() + random.nextDouble();
            level.addParticle(ParticleTypes.LARGE_SMOKE, x, y, z, 0.0, 0.05, 0.0);
        }
        if (random.nextInt(5) == 0) {
            double x = pos.getX() + random.nextDouble();
            double y = pos.getY() + 1.02;
            double z = pos.getZ() + random.nextDouble();
            level.addParticle(ParticleTypes.LAVA, x, y, z, 0.0, 0.04, 0.0);
        }
    }
}
