package com.ienuclear.reactor.registry;

import com.ienuclear.reactor.IENuclearMod;
import com.ienuclear.reactor.blockentity.CoolantInputPortBlockEntity;
import com.ienuclear.reactor.blockentity.EnergyOutputPortBlockEntity;
import com.ienuclear.reactor.blockentity.PrimitiveReactorBlockEntity;
import com.ienuclear.reactor.blockentity.ReactorControllerBlockEntity;
import com.ienuclear.reactor.blockentity.SteamInputPortBlockEntity;
import com.ienuclear.reactor.blockentity.SteamOutputPortBlockEntity;
import com.ienuclear.reactor.blockentity.SteamTurbineBlockEntity;
import com.ienuclear.reactor.blockentity.WaterInputPortBlockEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, IENuclearMod.MODID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ReactorControllerBlockEntity>> REACTOR_CONTROLLER =
            BLOCK_ENTITIES.register("reactor_controller", () ->
                    BlockEntityType.Builder.of(
                            ReactorControllerBlockEntity::new,
                            ModBlocks.REACTOR_CONTROLLER.get()
                    ).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<CoolantInputPortBlockEntity>> COOLANT_INPUT_PORT =
            BLOCK_ENTITIES.register("coolant_input_port", () ->
                    BlockEntityType.Builder.of(
                            CoolantInputPortBlockEntity::new,
                            ModBlocks.COOLANT_INPUT_PORT.get()
                    ).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<EnergyOutputPortBlockEntity>> ENERGY_OUTPUT_PORT =
            BLOCK_ENTITIES.register("energy_output_port", () ->
                    BlockEntityType.Builder.of(
                            EnergyOutputPortBlockEntity::new,
                            ModBlocks.ENERGY_OUTPUT_PORT.get()
                    ).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<WaterInputPortBlockEntity>> WATER_INPUT_PORT =
            BLOCK_ENTITIES.register("water_input_port", () ->
                    BlockEntityType.Builder.of(
                            WaterInputPortBlockEntity::new,
                            ModBlocks.WATER_INPUT_PORT.get()
                    ).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<SteamOutputPortBlockEntity>> STEAM_OUTPUT_PORT =
            BLOCK_ENTITIES.register("steam_output_port", () ->
                    BlockEntityType.Builder.of(
                            SteamOutputPortBlockEntity::new,
                            ModBlocks.STEAM_OUTPUT_PORT.get()
                    ).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<SteamTurbineBlockEntity>> STEAM_TURBINE =
            BLOCK_ENTITIES.register("steam_turbine", () ->
                    BlockEntityType.Builder.of(
                            SteamTurbineBlockEntity::new,
                            ModBlocks.STEAM_TURBINE.get()
                    ).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<PrimitiveReactorBlockEntity>> PRIMITIVE_REACTOR =
            BLOCK_ENTITIES.register("primitive_reactor", () ->
                    BlockEntityType.Builder.of(
                            PrimitiveReactorBlockEntity::new,
                            ModBlocks.PRIMITIVE_REACTOR.get()
                    ).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<SteamInputPortBlockEntity>> STEAM_INPUT_PORT =
            BLOCK_ENTITIES.register("steam_input_port", () ->
                    BlockEntityType.Builder.of(
                            SteamInputPortBlockEntity::new,
                            ModBlocks.STEAM_INPUT_PORT.get()
                    ).build(null));
}
