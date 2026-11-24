package com.ienuclear.reactor.multiblock;

import blusunrize.immersiveengineering.api.multiblocks.ClientMultiblocks;
import blusunrize.immersiveengineering.api.multiblocks.MultiblockHandler;
import com.ienuclear.reactor.IENuclearMod;
import com.ienuclear.reactor.block.SpentFuelPoolBlock;
import com.ienuclear.reactor.registry.ModBlocks;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Vec3i;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Мультиблок охолоджувального басейну 3×3×3 (Spent Fuel Pool).
 */
public class SpentFuelPoolMultiblock implements MultiblockHandler.IMultiblock {
    public static final SpentFuelPoolMultiblock INSTANCE = new SpentFuelPoolMultiblock();
    public static final ResourceLocation ID =
            ResourceLocation.fromNamespaceAndPath(IENuclearMod.MODID, "multiblocks/spent_fuel_pool");

    private static final Vec3i SIZE = new Vec3i(3, 3, 3);
    private static final BlockPos TRIGGER_OFFSET = new BlockPos(1, 1, 2);

    public static void register() {
        MultiblockHandler.registerMultiblock(INSTANCE);
    }

    @Override public ResourceLocation getUniqueName() { return ID; }
    @Override public Component getDisplayName() { return Component.translatable("block.ienuclear.spent_fuel_pool"); }
    @Override public boolean isBlockTrigger(BlockState state, Direction side, Level level) { return state.is(ModBlocks.SPENT_FUEL_POOL.get()); }
    @Override public boolean createStructure(Level level, BlockPos pos, Direction side, Player player) { return true; }
    @Override public List<StructureBlockInfo> getStructure(Level level) { return buildStructureList(); }
    @Override public float getManualScale() { return 14.0f; }
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
                        new ItemStack(ModBlocks.REACTOR_CASING.get(), 22),
                        new ItemStack(ModBlocks.REACTOR_GLASS.get(), 2),
                        new ItemStack(ModBlocks.SPENT_FUEL_POOL.get(), 1),
                        new ItemStack(Items.WATER_BUCKET, 2)
                );
            }
            @Override public boolean canRenderFormedStructure() { return false; }
            @Override public void renderFormedStructure(PoseStack matrix, MultiBufferSource buffer) {}
        });
    }

    private static List<StructureBlockInfo> buildStructureList() {
        List<StructureBlockInfo> list = new ArrayList<>();
        Block casing = ModBlocks.REACTOR_CASING.get();
        Block glass = ModBlocks.REACTOR_GLASS.get();
        Block controller = ModBlocks.SPENT_FUEL_POOL.get();

        for (int y = 0; y < 3; y++) {
            for (int z = 0; z < 3; z++) {
                for (int x = 0; x < 3; x++) {
                    if (y == 0) {
                        list.add(new StructureBlockInfo(new BlockPos(x, y, z), casing.defaultBlockState(), null));
                    } else if (x == 1 && z == 1) {
                        list.add(new StructureBlockInfo(new BlockPos(x, y, z), Blocks.WATER.defaultBlockState(), null));
                    } else if (y == 1 && x == 1 && z == 2) {
                        list.add(new StructureBlockInfo(new BlockPos(x, y, z),
                                controller.defaultBlockState().setValue(SpentFuelPoolBlock.FACING, Direction.SOUTH), null));
                    } else if (y == 1 && z == 1 && (x == 0 || x == 2)) {
                        list.add(new StructureBlockInfo(new BlockPos(x, y, z), glass.defaultBlockState(), null));
                    } else {
                        list.add(new StructureBlockInfo(new BlockPos(x, y, z), casing.defaultBlockState(), null));
                    }
                }
            }
        }
        return list;
    }
}
