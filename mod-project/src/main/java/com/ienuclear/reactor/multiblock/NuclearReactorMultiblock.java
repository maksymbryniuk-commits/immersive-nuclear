package com.ienuclear.reactor.multiblock;

import blusunrize.immersiveengineering.api.multiblocks.ClientMultiblocks;
import blusunrize.immersiveengineering.api.multiblocks.MultiblockHandler;
import com.ienuclear.reactor.IENuclearMod;
import com.ienuclear.reactor.block.ReactorControllerBlock;
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

public class NuclearReactorMultiblock implements MultiblockHandler.IMultiblock {
    public static final NuclearReactorMultiblock INSTANCE = new NuclearReactorMultiblock();
    public static final ResourceLocation ID =
            ResourceLocation.fromNamespaceAndPath(IENuclearMod.MODID, "multiblocks/nuclear_reactor");

    private static final Vec3i SIZE = new Vec3i(3, 3, 3);
    private static final BlockPos TRIGGER_OFFSET = new BlockPos(1, 1, 2);

    public static void register() {
        MultiblockHandler.registerMultiblock(INSTANCE);
    }

    @Override
    public ResourceLocation getUniqueName() {
        return ID;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.ienuclear.reactor_controller");
    }

    @Override
    public boolean isBlockTrigger(BlockState state, Direction side, Level level) {
        return state.is(ModBlocks.REACTOR_CONTROLLER.get());
    }

    @Override
    public boolean createStructure(Level level, BlockPos pos, Direction side, Player player) {
        // Контролер автоматично сканує та активує мультиблок 3x3x3
        return true;
    }

    @Override
    public List<StructureBlockInfo> getStructure(Level level) {
        return buildStructureList();
    }

    @Override
    public float getManualScale() {
        return 14.0f;
    }

    @Override
    public Vec3i getSize(Level level) {
        return SIZE;
    }

    @Override
    public void disassemble(Level level, BlockPos origin, boolean mirrored, Direction clickDirectionAtCreation) {
        // Блоки мультиблоку залишаються індивідуальними та розбираються стандартно
    }

    @Override
    public BlockPos getTriggerOffset() {
        return TRIGGER_OFFSET;
    }

    @Override
    public void initializeClient(Consumer<ClientMultiblocks.MultiblockManualData> consumer) {
        consumer.accept(new ClientMultiblocks.MultiblockManualData() {
            @Override
            public NonNullList<ItemStack> getTotalMaterials() {
                return NonNullList.of(
                        ItemStack.EMPTY,
                        new ItemStack(ModBlocks.REACTOR_CASING.get(), 19),
                        new ItemStack(ModBlocks.REACTOR_GLASS.get(), 1),
                        new ItemStack(ModBlocks.REACTOR_CORE_ROD.get(), 1),
                        new ItemStack(ModBlocks.REACTOR_CONTROLLER.get(), 1),
                        new ItemStack(ModBlocks.FUEL_ACCESS_HATCH.get(), 1),
                        new ItemStack(ModBlocks.COOLANT_INPUT_PORT.get(), 1),
                        new ItemStack(ModBlocks.WATER_INPUT_PORT.get(), 1),
                        new ItemStack(ModBlocks.STEAM_OUTPUT_PORT.get(), 1)
                );
            }

            @Override
            public boolean canRenderFormedStructure() {
                return false;
            }

            @Override
            public void renderFormedStructure(PoseStack matrix, MultiBufferSource buffer) {
                // Відображається пошарова 3D-схема з блоків мультиблоку
            }
        });
    }

    private static List<StructureBlockInfo> buildStructureList() {
        List<StructureBlockInfo> list = new ArrayList<>();
        Block casing = ModBlocks.REACTOR_CASING.get();
        Block glass = ModBlocks.REACTOR_GLASS.get();
        Block core = ModBlocks.REACTOR_CORE_ROD.get();
        Block controller = ModBlocks.REACTOR_CONTROLLER.get();
        Block coolant = ModBlocks.COOLANT_INPUT_PORT.get();
        Block water = ModBlocks.WATER_INPUT_PORT.get();
        Block steam = ModBlocks.STEAM_OUTPUT_PORT.get();
        Block hatch = ModBlocks.FUEL_ACCESS_HATCH.get();

        for (int y = 0; y < 3; y++) {
            for (int z = 0; z < 3; z++) {
                for (int x = 0; x < 3; x++) {
                    BlockState state = casing.defaultBlockState();
                    if (y == 1) {
                        if (x == 1 && z == 1) {
                            state = core.defaultBlockState();
                        } else if (x == 1 && z == 2) {
                            state = controller.defaultBlockState()
                                    .setValue(ReactorControllerBlock.FACING, Direction.SOUTH);
                        } else if (x == 0 && z == 1) {
                            state = coolant.defaultBlockState();
                        } else if (x == 2 && z == 1) {
                            state = steam.defaultBlockState();
                        } else if (x == 1 && z == 0) {
                            state = water.defaultBlockState();
                        }
                    } else if (y == 2 && x == 1 && z == 1) {
                        state = hatch.defaultBlockState();
                    }
                    list.add(new StructureBlockInfo(new BlockPos(x, y, z), state, null));
                }
            }
        }
        return list;
    }
}
