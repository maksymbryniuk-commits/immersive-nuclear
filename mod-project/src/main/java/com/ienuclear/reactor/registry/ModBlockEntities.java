package com.ienuclear.reactor.registry;

import com.ienuclear.reactor.IENuclearMod;
import com.ienuclear.reactor.blockentity.ControlDeskBlockEntity;
import com.ienuclear.reactor.blockentity.CoolantInputPortBlockEntity;
import com.ienuclear.reactor.blockentity.CoolingTowerBlockEntity;
import com.ienuclear.reactor.blockentity.EccsUnitBlockEntity;
import com.ienuclear.reactor.blockentity.EnergyOutputPortBlockEntity;
import com.ienuclear.reactor.blockentity.FuelAccessHatchBlockEntity;
import com.ienuclear.reactor.blockentity.FuelRodAssemblerBlockEntity;
import com.ienuclear.reactor.blockentity.GasCentrifugeBlockEntity;
import com.ienuclear.reactor.blockentity.HeatExchangerBlockEntity;
import com.ienuclear.reactor.blockentity.NuclearWasteCaskBlockEntity;
import com.ienuclear.reactor.blockentity.PrimitiveReactorBlockEntity;
import com.ienuclear.reactor.blockentity.ReactorControllerBlockEntity;
import com.ienuclear.reactor.blockentity.RemoteControlPortBlockEntity;
import com.ienuclear.reactor.blockentity.RtgGeneratorBlockEntity;
import com.ienuclear.reactor.blockentity.SpentFuelPoolBlockEntity;
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

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<FuelAccessHatchBlockEntity>> FUEL_ACCESS_HATCH =
            BLOCK_ENTITIES.register("fuel_access_hatch", () ->
                    BlockEntityType.Builder.of(
                            FuelAccessHatchBlockEntity::new,
                            ModBlocks.FUEL_ACCESS_HATCH.get()
                    ).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<RemoteControlPortBlockEntity>> REMOTE_CONTROL_PORT =
            BLOCK_ENTITIES.register("remote_control_port", () ->
                    BlockEntityType.Builder.of(RemoteControlPortBlockEntity::new, ModBlocks.REMOTE_CONTROL_PORT.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ControlDeskBlockEntity>> CONTROL_DESK =
            BLOCK_ENTITIES.register("control_desk", () ->
                    BlockEntityType.Builder.of(ControlDeskBlockEntity::new, ModBlocks.CONTROL_DESK.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<GasCentrifugeBlockEntity>> GAS_CENTRIFUGE =
            BLOCK_ENTITIES.register("gas_centrifuge", () ->
                    BlockEntityType.Builder.of(GasCentrifugeBlockEntity::new, ModBlocks.GAS_CENTRIFUGE.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<FuelRodAssemblerBlockEntity>> FUEL_ROD_ASSEMBLER =
            BLOCK_ENTITIES.register("fuel_rod_assembler", () ->
                    BlockEntityType.Builder.of(FuelRodAssemblerBlockEntity::new, ModBlocks.FUEL_ROD_ASSEMBLER.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<SpentFuelPoolBlockEntity>> SPENT_FUEL_POOL =
            BLOCK_ENTITIES.register("spent_fuel_pool", () ->
                    BlockEntityType.Builder.of(SpentFuelPoolBlockEntity::new, ModBlocks.SPENT_FUEL_POOL.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<CoolingTowerBlockEntity>> COOLING_TOWER =
            BLOCK_ENTITIES.register("cooling_tower", () ->
                    BlockEntityType.Builder.of(CoolingTowerBlockEntity::new, ModBlocks.COOLING_TOWER.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<HeatExchangerBlockEntity>> HEAT_EXCHANGER_CONTROLLER =
            BLOCK_ENTITIES.register("heat_exchanger_controller", () ->
                    BlockEntityType.Builder.of(HeatExchangerBlockEntity::new, ModBlocks.HEAT_EXCHANGER_CONTROLLER.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<EccsUnitBlockEntity>> ECCS_UNIT =
            BLOCK_ENTITIES.register("eccs_unit", () ->
                    BlockEntityType.Builder.of(EccsUnitBlockEntity::new, ModBlocks.ECCS_UNIT.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<NuclearWasteCaskBlockEntity>> NUCLEAR_WASTE_CASK =
            BLOCK_ENTITIES.register("nuclear_waste_cask", () ->
                    BlockEntityType.Builder.of(NuclearWasteCaskBlockEntity::new, ModBlocks.NUCLEAR_WASTE_CASK.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<RtgGeneratorBlockEntity>> RTG_GENERATOR =
            BLOCK_ENTITIES.register("rtg_generator", () ->
                    BlockEntityType.Builder.of(RtgGeneratorBlockEntity::new, ModBlocks.RTG_GENERATOR.get()).build(null));
}
