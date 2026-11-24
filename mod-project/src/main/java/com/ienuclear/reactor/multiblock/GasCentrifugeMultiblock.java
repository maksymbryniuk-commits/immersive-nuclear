package com.ienuclear.reactor.multiblock;

import blusunrize.immersiveengineering.api.multiblocks.ClientMultiblocks;
import blusunrize.immersiveengineering.api.multiblocks.MultiblockHandler;
import com.ienuclear.reactor.IENuclearMod;
import com.ienuclear.reactor.block.GasCentrifugeBlock;
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
 * Мультиблок газової центрифуги 3×4×2 (ширина 3 × висота 4 × глибина 2).
 */
public class GasCentrifugeMultiblock implements MultiblockHandler.IMultiblock {
    public static final GasCentrifugeMultiblock INSTANCE = new GasCentrifugeMultiblock();
    public static final ResourceLocation ID =
            ResourceLocation.fromNamespaceAndPath(IENuclearMod.MODID, "multiblocks/gas_centrifuge");

    private static final Vec3i SIZE = new Vec3i(3, 4, 2);
    private static final BlockPos TRIGGER_OFFSET = new BlockPos(1, 0, 1);

    private static Block ie(String name) {
        return BuiltInRegistries.BLOCK.get(ResourceLocation.fromNamespaceAndPath("immersiveengineering", name));
    }

    public static void register() {
        MultiblockHandler.registerMultiblock(INSTANCE);
    }

    @Override public ResourceLocation getUniqueName() { return ID; }
    @Override public Component getDisplayName() { return Component.translatable("block.ienuclear.gas_centrifuge"); }
    @Override public boolean isBlockTrigger(BlockState state, Direction side, Level level) { return state.is(ModBlocks.GAS_CENTRIFUGE.get()); }
    @Override public boolean createStructure(Level level, BlockPos pos, Direction side, Player player) { return true; }
    @Override public List<StructureBlockInfo> getStructure(Level level) { return buildStructureList(); }
    @Override public float getManualScale() { return 12.0f; }
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
                        new ItemStack(ie("steel_scaffolding_standard"), 5),
                        new ItemStack(ie("light_engineering"), 4),
                        new ItemStack(ie("heavy_engineering"), 2),
                        new ItemStack(ie("sheetmetal_steel"), 10),
                        new ItemStack(ie("radiator"), 2),
                        new ItemStack(ModBlocks.GAS_CENTRIFUGE.get(), 1)
                );
            }
            @Override public boolean canRenderFormedStructure() { return false; }
            @Override public void renderFormedStructure(PoseStack matrix, MultiBufferSource buffer) {}
        });
    }

    private static List<StructureBlockInfo> buildStructureList() {
        List<StructureBlockInfo> list = new ArrayList<>();
        Block scaffold = ie("steel_scaffolding_standard");
        Block light = ie("light_engineering");
        Block heavy = ie("heavy_engineering");
        Block sheet = ie("sheetmetal_steel");
        Block radiator = ie("radiator");
        Block ctrl = ModBlocks.GAS_CENTRIFUGE.get();

        for (int y = 0; y < 4; y++) {
            for (int z = 0; z < 2; z++) {
                for (int x = 0; x < 3; x++) {
                    BlockState state;
                    if (y == 0) {
                        state = (x == 1 && z == 1)
                                ? ctrl.defaultBlockState().setValue(GasCentrifugeBlock.FACING, Direction.SOUTH)
                                : scaffold.defaultBlockState();
                    } else if (y == 1) {
                        if (z == 1 && x == 1) state = heavy.defaultBlockState();
                        else if (z == 1) state = light.defaultBlockState();
                        else state = sheet.defaultBlockState();
                    } else if (y == 2) {
                        state = (z == 1 && x == 1) ? heavy.defaultBlockState() : sheet.defaultBlockState();
                    } else {
                        if (z == 0 && (x == 0 || x == 2)) state = radiator.defaultBlockState();
                        else if (x == 1) state = light.defaultBlockState();
                        else state = sheet.defaultBlockState();
                    }
                    list.add(new StructureBlockInfo(new BlockPos(x, y, z), state, null));
                }
            }
        }
        return list;
    }
}
