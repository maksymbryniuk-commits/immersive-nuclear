package com.ienuclear.reactor.multiblock;

import blusunrize.immersiveengineering.api.multiblocks.ClientMultiblocks;
import blusunrize.immersiveengineering.api.multiblocks.MultiblockHandler;
import com.ienuclear.reactor.IENuclearMod;
import com.ienuclear.reactor.block.HeatExchangerBlock;
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
 * Мультиблок промислового теплообмінника 3×3×5.
 */
public class HeatExchangerMultiblock implements MultiblockHandler.IMultiblock {
    public static final HeatExchangerMultiblock INSTANCE = new HeatExchangerMultiblock();
    public static final ResourceLocation ID =
            ResourceLocation.fromNamespaceAndPath(IENuclearMod.MODID, "multiblocks/heat_exchanger");

    private static final Vec3i SIZE = new Vec3i(3, 3, 5);
    private static final BlockPos TRIGGER_OFFSET = new BlockPos(1, 0, 4);

    private static Block ie(String name) {
        return BuiltInRegistries.BLOCK.get(ResourceLocation.fromNamespaceAndPath("immersiveengineering", name));
    }

    public static void register() {
        MultiblockHandler.registerMultiblock(INSTANCE);
    }

    @Override public ResourceLocation getUniqueName() { return ID; }
    @Override public Component getDisplayName() { return Component.translatable("block.ienuclear.heat_exchanger_controller"); }
    @Override public boolean isBlockTrigger(BlockState state, Direction side, Level level) { return state.is(ModBlocks.HEAT_EXCHANGER_CONTROLLER.get()); }
    @Override public boolean createStructure(Level level, BlockPos pos, Direction side, Player player) { return true; }
    @Override public List<StructureBlockInfo> getStructure(Level level) { return buildStructureList(); }
    @Override public float getManualScale() { return 11.0f; }
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
                        new ItemStack(ie("steel_scaffolding_standard"), 14),
                        new ItemStack(ie("sheetmetal_steel"), 25),
                        new ItemStack(ie("fluid_pipe"), 5),
                        new ItemStack(ModBlocks.HEAT_EXCHANGER_CONTROLLER.get(), 1)
                );
            }
            @Override public boolean canRenderFormedStructure() { return false; }
            @Override public void renderFormedStructure(PoseStack matrix, MultiBufferSource buffer) {}
        });
    }

    private static List<StructureBlockInfo> buildStructureList() {
        List<StructureBlockInfo> list = new ArrayList<>();
        Block scaffold = ie("steel_scaffolding_standard");
        Block sheet = ie("sheetmetal_steel");
        Block pipe = ie("fluid_pipe");
        Block ctrl = ModBlocks.HEAT_EXCHANGER_CONTROLLER.get();

        for (int y = 0; y < 3; y++) {
            for (int z = 0; z < 5; z++) {
                for (int x = 0; x < 3; x++) {
                    BlockState state;
                    if (y == 0 && x == 1 && z == 4) {
                        state = ctrl.defaultBlockState().setValue(HeatExchangerBlock.FACING, Direction.SOUTH);
                    } else if (y == 0) {
                        state = scaffold.defaultBlockState();
                    } else if (y == 1 && x == 1) {
                        state = pipe.defaultBlockState();
                    } else {
                        state = sheet.defaultBlockState();
                    }
                    list.add(new StructureBlockInfo(new BlockPos(x, y, z), state, null));
                }
            }
        }
        return list;
    }
}
