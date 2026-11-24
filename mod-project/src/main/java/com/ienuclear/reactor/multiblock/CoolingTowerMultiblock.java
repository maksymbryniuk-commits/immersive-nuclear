package com.ienuclear.reactor.multiblock;

import blusunrize.immersiveengineering.api.multiblocks.ClientMultiblocks;
import blusunrize.immersiveengineering.api.multiblocks.MultiblockHandler;
import com.ienuclear.reactor.IENuclearMod;
import com.ienuclear.reactor.block.CoolingTowerBlock;
import com.ienuclear.reactor.registry.ModBlocks;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Мультиблок бетонної градирні 7×10×7 (Cooling Tower).
 */
public class CoolingTowerMultiblock implements MultiblockHandler.IMultiblock {
    public static final CoolingTowerMultiblock INSTANCE = new CoolingTowerMultiblock();
    public static final ResourceLocation ID =
            ResourceLocation.fromNamespaceAndPath(IENuclearMod.MODID, "multiblocks/cooling_tower");

    private static final Vec3i SIZE = new Vec3i(7, 10, 7);
    private static final BlockPos TRIGGER_OFFSET = new BlockPos(3, 0, 6);

    private static Block ie(String name) {
        return BuiltInRegistries.BLOCK.get(ResourceLocation.fromNamespaceAndPath("immersiveengineering", name));
    }

    public static void register() {
        MultiblockHandler.registerMultiblock(INSTANCE);
    }

    @Override public ResourceLocation getUniqueName() { return ID; }
    @Override public Component getDisplayName() { return Component.translatable("block.ienuclear.cooling_tower"); }
    @Override public boolean isBlockTrigger(BlockState state, Direction side, Level level) { return state.is(ModBlocks.COOLING_TOWER.get()); }
    @Override public boolean createStructure(Level level, BlockPos pos, Direction side, Player player) { return true; }
    @Override public List<StructureBlockInfo> getStructure(Level level) { return buildStructureList(); }
    @Override public float getManualScale() { return 6.2f; }
    @Override public Vec3i getSize(Level level) { return SIZE; }
    @Override public void disassemble(Level level, BlockPos origin, boolean mirrored, Direction clickDirectionAtCreation) {}
    @Override public BlockPos getTriggerOffset() { return TRIGGER_OFFSET; }

    @Override
    public void initializeClient(Consumer<ClientMultiblocks.MultiblockManualData> consumer) {
        consumer.accept(new ClientMultiblocks.MultiblockManualData() {
            @Override
            public NonNullList<ItemStack> getTotalMaterials() {
                return NonNullList.of(
                        ItemStack.EMPTY,
                        new ItemStack(ie("concrete"), 148),
                        new ItemStack(ie("concrete_leaded"), 24),
                        new ItemStack(ModBlocks.COOLING_TOWER.get(), 1)
                );
            }
            @Override public boolean canRenderFormedStructure() { return false; }
            @Override public void renderFormedStructure(PoseStack matrix, MultiBufferSource buffer) {}
        });
    }

    private static List<StructureBlockInfo> buildStructureList() {
        List<StructureBlockInfo> list = new ArrayList<>();
        Block concrete = ie("concrete");
        Block leaded = ie("concrete_leaded");
        Block ctrl = ModBlocks.COOLING_TOWER.get();

        for (int y = 0; y < 10; y++) {
            for (int z = 0; z < 7; z++) {
                for (int x = 0; x < 7; x++) {
                    boolean edge = (x == 0 || x == 6 || z == 0 || z == 6);
                    if (!edge && y > 0) continue;
                    if (y == 0 && x == 3 && z == 6) {
                        list.add(new StructureBlockInfo(new BlockPos(x, y, z),
                                ctrl.defaultBlockState().setValue(CoolingTowerBlock.FACING, Direction.SOUTH), null));
                    } else if (y == 0 && !edge) {
                        list.add(new StructureBlockInfo(new BlockPos(x, y, z), leaded.defaultBlockState(), null));
                    } else {
                        list.add(new StructureBlockInfo(new BlockPos(x, y, z), concrete.defaultBlockState(), null));
                    }
                }
            }
        }
        return list;
    }
}
