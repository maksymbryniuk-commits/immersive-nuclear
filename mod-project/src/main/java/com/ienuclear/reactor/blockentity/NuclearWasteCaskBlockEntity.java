package com.ienuclear.reactor.blockentity;

import com.ienuclear.reactor.registry.ModBlockEntities;
import com.ienuclear.reactor.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

/**
 * Контейнер для ядерних відходів: якщо не закопаний (бачить небо) — фонить радіацією на весь радіус!
 */
public class NuclearWasteCaskBlockEntity extends BlockEntity {
    private int wasteCount = 0;

    public NuclearWasteCaskBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.NUCLEAR_WASTE_CASK.get(), pos, state);
    }

    public int getWasteCount() { return wasteCount; }

    public boolean addWaste(ItemStack stack) {
        if (wasteCount < 9) {
            wasteCount++;
            stack.shrink(1);
            setChanged();
            return true;
        }
        return false;
    }

    public static boolean hasFullHazmat(Player p) {
        return p.getItemBySlot(EquipmentSlot.HEAD).is(ModItems.HAZMAT_HELMET.get())
                && p.getItemBySlot(EquipmentSlot.CHEST).is(ModItems.HAZMAT_CHESTPLATE.get())
                && p.getItemBySlot(EquipmentSlot.LEGS).is(ModItems.HAZMAT_LEGGINGS.get())
                && p.getItemBySlot(EquipmentSlot.FEET).is(ModItems.HAZMAT_BOOTS.get());
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, NuclearWasteCaskBlockEntity be) {
        if (be.wasteCount <= 0 || level.getGameTime() % 20 != 0) return;
        boolean exposed = level.canSeeSky(pos.above());
        if (exposed) {
            if (level instanceof ServerLevel sl) {
                sl.sendParticles(ParticleTypes.HAPPY_VILLAGER, pos.getX() + 0.5, pos.getY() + 1.1, pos.getZ() + 0.5, 4, 0.4, 0.4, 0.4, 0.01);
            }
            AABB box = new AABB(pos).inflate(10.0);
            for (Player p : level.getEntitiesOfClass(Player.class, box)) {
                if (!p.isCreative() && !hasFullHazmat(p)) {
                    p.addEffect(new MobEffectInstance(MobEffects.POISON, 80, 0));
                    p.addEffect(new MobEffectInstance(MobEffects.HUNGER, 80, 0));
                }
            }
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider reg) {
        super.saveAdditional(tag, reg);
        tag.putInt("Waste", wasteCount);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider reg) {
        super.loadAdditional(tag, reg);
        wasteCount = tag.getInt("Waste");
    }
}
