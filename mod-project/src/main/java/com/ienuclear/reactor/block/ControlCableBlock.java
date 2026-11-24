package com.ienuclear.reactor.block;

import com.ienuclear.reactor.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Кабель передачі даних для дистанційного керування реактором (з'єднує Remote Control Port і Control Desk).
 * Автоматично підключається по 6 напрямках до інших кабелів, порту керування та пульта.
 */
public class ControlCableBlock extends Block {
    public static final BooleanProperty NORTH = BlockStateProperties.NORTH;
    public static final BooleanProperty SOUTH = BlockStateProperties.SOUTH;
    public static final BooleanProperty EAST = BlockStateProperties.EAST;
    public static final BooleanProperty WEST = BlockStateProperties.WEST;
    public static final BooleanProperty UP = BlockStateProperties.UP;
    public static final BooleanProperty DOWN = BlockStateProperties.DOWN;

    private static final VoxelShape CORE = Block.box(5.0, 5.0, 5.0, 11.0, 11.0, 11.0);
    private static final VoxelShape ARM_N = Block.box(5.5, 5.5, 0.0, 10.5, 10.5, 5.0);
    private static final VoxelShape ARM_S = Block.box(5.5, 5.5, 11.0, 10.5, 10.5, 16.0);
    private static final VoxelShape ARM_W = Block.box(0.0, 5.5, 5.5, 5.0, 10.5, 10.5);
    private static final VoxelShape ARM_E = Block.box(11.0, 5.5, 5.5, 16.0, 10.5, 10.5);
    private static final VoxelShape ARM_D = Block.box(5.5, 0.0, 5.5, 10.5, 5.0, 10.5);
    private static final VoxelShape ARM_U = Block.box(5.5, 11.0, 5.5, 10.5, 16.0, 10.5);

    public ControlCableBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any()
                .setValue(NORTH, false).setValue(SOUTH, false)
                .setValue(EAST, false).setValue(WEST, false)
                .setValue(UP, false).setValue(DOWN, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(NORTH, SOUTH, EAST, WEST, UP, DOWN);
    }

    private boolean canConnectTo(LevelAccessor level, BlockPos neighborPos) {
        BlockState bs = level.getBlockState(neighborPos);
        return bs.is(ModBlocks.CONTROL_CABLE.get())
                || bs.is(ModBlocks.REMOTE_CONTROL_PORT.get())
                || bs.is(ModBlocks.CONTROL_DESK.get());
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        Level lvl = ctx.getLevel();
        BlockPos p = ctx.getClickedPos();
        return defaultBlockState()
                .setValue(NORTH, canConnectTo(lvl, p.north()))
                .setValue(SOUTH, canConnectTo(lvl, p.south()))
                .setValue(EAST, canConnectTo(lvl, p.east()))
                .setValue(WEST, canConnectTo(lvl, p.west()))
                .setValue(UP, canConnectTo(lvl, p.above()))
                .setValue(DOWN, canConnectTo(lvl, p.below()));
    }

    @Override
    protected BlockState updateShape(BlockState state, Direction dir, BlockState neighborState,
                                     LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        BooleanProperty prop = switch (dir) {
            case NORTH -> NORTH;
            case SOUTH -> SOUTH;
            case EAST -> EAST;
            case WEST -> WEST;
            case UP -> UP;
            case DOWN -> DOWN;
        };
        return state.setValue(prop, canConnectTo(level, neighborPos));
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        VoxelShape shape = CORE;
        if (state.getValue(NORTH)) shape = Shapes.or(shape, ARM_N);
        if (state.getValue(SOUTH)) shape = Shapes.or(shape, ARM_S);
        if (state.getValue(EAST)) shape = Shapes.or(shape, ARM_E);
        if (state.getValue(WEST)) shape = Shapes.or(shape, ARM_W);
        if (state.getValue(UP)) shape = Shapes.or(shape, ARM_U);
        if (state.getValue(DOWN)) shape = Shapes.or(shape, ARM_D);
        return shape;
    }
}
