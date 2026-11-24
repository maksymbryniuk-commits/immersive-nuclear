package com.ienuclear.reactor.multiblock;

import blusunrize.immersiveengineering.api.multiblocks.ClientMultiblocks;
import blusunrize.immersiveengineering.api.multiblocks.MultiblockHandler;
import com.ienuclear.reactor.IENuclearMod;
import com.ienuclear.reactor.block.SteamTurbineBlock;
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
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class SteamTurbineMultiblock implements MultiblockHandler.IMultiblock {
    public static final SteamTurbineMultiblock INSTANCE = new SteamTurbineMultiblock();
    public static final ResourceLocation ID =
            ResourceLocation.fromNamespaceAndPath(IENuclearMod.MODID, "multiblocks/steam_turbine");

    private static final Vec3i SIZE = new Vec3i(3, 3, 5);
    private static final BlockPos TRIGGER_OFFSET = new BlockPos(1, 1, 4);

    public static void register() {
        MultiblockHandler.registerMultiblock(INSTANCE);
    }

    @Override
    public ResourceLocation getUniqueName() { return ID; }

    @Override
    public Component getDisplayName() { return Component.translatable("block.ienuclear.steam_turbine"); }

    @Override
    public boolean isBlockTrigger(BlockState state, Direction side, Level level) {
        return state.is(ModBlocks.STEAM_TURBINE.get());
    }

    @Override
    public boolean createStructure(Level level, BlockPos pos, Direction side, Player player) { return true; }

    @Override
    public List<StructureBlockInfo> getStructure(Level level) { return buildStructureList(); }

    @Override
    public float getManualScale() { return 11.0f; }

    @Override
    public Vec3i getSize(Level level) { return SIZE; }

    @Override
    public void disassemble(Level level, BlockPos origin, boolean mirrored, Direction clickDirectionAtCreation) {}

    @Override
    public BlockPos getTriggerOffset() { return TRIGGER_OFFSET; }

    @Override
    public void initializeClient(Consumer<ClientMultiblocks.MultiblockManualData> consumer) {
        consumer.accept(new ClientMultiblocks.MultiblockManualData() {
            @Override
            public NonNullList<ItemStack> getTotalMaterials() {
                return NonNullList.of(
                        ItemStack.EMPTY,
                        new ItemStack(ModBlocks.TURBINE_CASING.get(), 36),
                        new ItemStack(ModBlocks.REACTOR_GLASS.get(), 3),
                        new ItemStack(ModBlocks.TURBINE_ROTOR.get(), 3),
                        new ItemStack(ModBlocks.STEAM_TURBINE.get(), 1),
                        new ItemStack(ModBlocks.STEAM_INPUT_PORT.get(), 1),
                        new ItemStack(ModBlocks.ENERGY_OUTPUT_PORT.get(), 1)
                );
            }

            @Override
            public boolean canRenderFormedStructure() { return false; }

            @Override
            public void renderFormedStructure(PoseStack matrix, MultiBufferSource buffer) {}
        });
    }

    private static List<StructureBlockInfo> buildStructureList() {
        List<StructureBlockInfo> list = new ArrayList<>();
        Block casing = ModBlocks.TURBINE_CASING.get();
        Block glass = ModBlocks.REACTOR_GLASS.get();
        Block rotor = ModBlocks.TURBINE_ROTOR.get();
        Block controller = ModBlocks.STEAM_TURBINE.get();
        Block steamIn = ModBlocks.STEAM_INPUT_PORT.get();
        Block energyOut = ModBlocks.ENERGY_OUTPUT_PORT.get();

        for (int y = 0; y < 3; y++) {
            for (int z = 0; z < 5; z++) {
                for (int x = 0; x < 3; x++) {
                    BlockState state = casing.defaultBlockState();
                    if (y == 1 && x == 1 && z >= 1 && z <= 3) {
                        state = rotor.defaultBlockState();
                    } else if (y == 1 && x == 1 && z == 4) {
                        state = controller.defaultBlockState().setValue(SteamTurbineBlock.FACING, Direction.SOUTH);
                    } else if (y == 1 && x == 0 && z == 2) {
                        state = steamIn.defaultBlockState();
                    } else if (y == 1 && x == 2 && z == 2) {
                        state = energyOut.defaultBlockState();
                    } else if (y == 2 && x == 1 && z >= 1 && z <= 3) {
                        state = glass.defaultBlockState();
                    }
                    list.add(new StructureBlockInfo(new BlockPos(x, y, z), state, null));
                }
            }
        }
        return list;
    }
}
