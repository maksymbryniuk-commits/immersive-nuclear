package com.ienuclear.reactor.registry;

import com.ienuclear.reactor.IENuclearMod;
import com.ienuclear.reactor.block.ControlCableBlock;
import com.ienuclear.reactor.block.ControlDeskBlock;
import com.ienuclear.reactor.block.CoolantInputPortBlock;
import com.ienuclear.reactor.block.CoolingTowerBlock;
import com.ienuclear.reactor.block.EccsUnitBlock;
import com.ienuclear.reactor.block.EnergyOutputPortBlock;
import com.ienuclear.reactor.block.FuelAccessHatchBlock;
import com.ienuclear.reactor.block.FuelRodAssemblerBlock;
import com.ienuclear.reactor.block.GasCentrifugeBlock;
import com.ienuclear.reactor.block.HeatExchangerBlock;
import com.ienuclear.reactor.block.MoltenCoriumBlock;
import com.ienuclear.reactor.block.NuclearWasteCaskBlock;
import com.ienuclear.reactor.block.PrimitiveReactorBlock;
import com.ienuclear.reactor.block.ReactorControllerBlock;
import com.ienuclear.reactor.block.RemoteControlPortBlock;
import com.ienuclear.reactor.block.RtgGeneratorBlock;
import com.ienuclear.reactor.block.SpentFuelPoolBlock;
import com.ienuclear.reactor.block.SteamInputPortBlock;
import com.ienuclear.reactor.block.SteamOutputPortBlock;
import com.ienuclear.reactor.block.SteamTurbineBlock;
import com.ienuclear.reactor.block.WaterInputPortBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.TransparentBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS =
            DeferredRegister.createBlocks(IENuclearMod.MODID);

    public static final DeferredBlock<Block> REACTOR_CASING = BLOCKS.register(
            "reactor_casing",
            () -> new Block(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .strength(6.0f, 30.0f)
                    .requiresCorrectToolForDrops()
                    .sound(SoundType.METAL))
    );

    public static final DeferredBlock<Block> REACTOR_CORE_ROD = BLOCKS.register(
            "reactor_core_rod",
            () -> new Block(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_CYAN)
                    .strength(5.0f, 18.0f)
                    .requiresCorrectToolForDrops()
                    .lightLevel(state -> 7)
                    .sound(SoundType.METAL))
    );

    public static final DeferredBlock<Block> REACTOR_GLASS = BLOCKS.register(
            "reactor_glass",
            () -> new TransparentBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.NONE)
                    .strength(3.0f, 15.0f)
                    .noOcclusion()
                    .sound(SoundType.GLASS))
    );

    public static final DeferredBlock<ReactorControllerBlock> REACTOR_CONTROLLER = BLOCKS.register(
            "reactor_controller",
            () -> new ReactorControllerBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .strength(6.0f, 30.0f)
                    .requiresCorrectToolForDrops()
                    .sound(SoundType.METAL))
    );

    public static final DeferredBlock<CoolantInputPortBlock> COOLANT_INPUT_PORT = BLOCKS.register(
            "coolant_input_port",
            () -> new CoolantInputPortBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_BLUE)
                    .strength(5.0f, 20.0f)
                    .requiresCorrectToolForDrops()
                    .sound(SoundType.METAL))
    );

    public static final DeferredBlock<EnergyOutputPortBlock> ENERGY_OUTPUT_PORT = BLOCKS.register(
            "energy_output_port",
            () -> new EnergyOutputPortBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_ORANGE)
                    .strength(5.0f, 20.0f)
                    .requiresCorrectToolForDrops()
                    .sound(SoundType.METAL))
    );

    public static final DeferredBlock<LiquidBlock> HEAVY_COOLANT_BLOCK = BLOCKS.register(
            "heavy_coolant_block",
            () -> new LiquidBlock(ModFluids.HEAVY_COOLANT_SOURCE.get(),
                    BlockBehaviour.Properties.ofFullCopy(Blocks.WATER).noLootTable())
    );

    public static final DeferredBlock<LiquidBlock> URANIUM_HEXAFLUORIDE_BLOCK = BLOCKS.register(
            "uranium_hexafluoride_block",
            () -> new LiquidBlock(ModFluids.URANIUM_HEXAFLUORIDE_SOURCE.get(),
                    BlockBehaviour.Properties.ofFullCopy(Blocks.WATER).noLootTable())
    );

    public static final DeferredBlock<LiquidBlock> ENRICHED_URANIUM_BLOCK = BLOCKS.register(
            "enriched_uranium_block",
            () -> new LiquidBlock(ModFluids.ENRICHED_URANIUM_SOURCE.get(),
                    BlockBehaviour.Properties.ofFullCopy(Blocks.WATER).noLootTable())
    );

    public static final DeferredBlock<MoltenCoriumBlock> MOLTEN_CORIUM = BLOCKS.register(
            "molten_corium",
            () -> new MoltenCoriumBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_ORANGE)
                    .strength(12.0f, 1200.0f)
                    .requiresCorrectToolForDrops()
                    .lightLevel(state -> 13)
                    .sound(SoundType.STONE))
    );

    // v2.0: Паровий цикл (Порт води, Порт пари, Парова турбіна) + Примітивний 1x1 реактор
    public static final DeferredBlock<WaterInputPortBlock> WATER_INPUT_PORT = BLOCKS.register(
            "water_input_port",
            () -> new WaterInputPortBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.WATER)
                    .strength(5.0f, 20.0f)
                    .requiresCorrectToolForDrops()
                    .sound(SoundType.METAL))
    );

    public static final DeferredBlock<SteamOutputPortBlock> STEAM_OUTPUT_PORT = BLOCKS.register(
            "steam_output_port",
            () -> new SteamOutputPortBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.SNOW)
                    .strength(5.0f, 20.0f)
                    .requiresCorrectToolForDrops()
                    .sound(SoundType.METAL))
    );

    public static final DeferredBlock<SteamTurbineBlock> STEAM_TURBINE = BLOCKS.register(
            "steam_turbine",
            () -> new SteamTurbineBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .strength(6.0f, 25.0f)
                    .requiresCorrectToolForDrops()
                    .sound(SoundType.METAL))
    );

    public static final DeferredBlock<PrimitiveReactorBlock> PRIMITIVE_REACTOR = BLOCKS.register(
            "primitive_reactor",
            () -> new PrimitiveReactorBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .strength(5.5f, 22.0f)
                    .requiresCorrectToolForDrops()
                    .sound(SoundType.METAL))
    );

    public static final DeferredBlock<LiquidBlock> HOT_STEAM_BLOCK = BLOCKS.register(
            "hot_steam_block",
            () -> new LiquidBlock(ModFluids.HOT_STEAM_SOURCE.get(),
                    BlockBehaviour.Properties.ofFullCopy(Blocks.WATER).noLootTable())
    );

    // v2.1: Мультиблокова Парова турбіна 3x3x5
    public static final DeferredBlock<Block> TURBINE_CASING = BLOCKS.register(
            "turbine_casing",
            () -> new Block(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .strength(5.5f, 25.0f)
                    .requiresCorrectToolForDrops()
                    .sound(SoundType.METAL))
    );

    public static final DeferredBlock<Block> TURBINE_ROTOR = BLOCKS.register(
            "turbine_rotor",
            () -> new Block(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .strength(5.0f, 20.0f)
                    .requiresCorrectToolForDrops()
                    .sound(SoundType.METAL))
    );

    public static final DeferredBlock<SteamInputPortBlock> STEAM_INPUT_PORT = BLOCKS.register(
            "steam_input_port",
            () -> new SteamInputPortBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.SNOW)
                    .strength(5.0f, 20.0f)
                    .requiresCorrectToolForDrops()
                    .sound(SoundType.METAL))
    );

    // v2.3: Верхній люк завантаження палива та IC2-пластин
    public static final DeferredBlock<FuelAccessHatchBlock> FUEL_ACCESS_HATCH = BLOCKS.register(
            "fuel_access_hatch",
            () -> new FuelAccessHatchBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .strength(5.5f, 25.0f)
                    .requiresCorrectToolForDrops()
                    .sound(SoundType.METAL))
    );

    // v3.0: Нові рідини
    public static final DeferredBlock<LiquidBlock> DEPLETED_UF6_BLOCK = BLOCKS.register(
            "depleted_uf6_block",
            () -> new LiquidBlock(ModFluids.DEPLETED_UF6_SOURCE.get(), BlockBehaviour.Properties.ofFullCopy(Blocks.WATER).noLootTable())
    );
    public static final DeferredBlock<LiquidBlock> LIQUID_SODIUM_BLOCK = BLOCKS.register(
            "liquid_sodium_block",
            () -> new LiquidBlock(ModFluids.LIQUID_SODIUM_SOURCE.get(), BlockBehaviour.Properties.ofFullCopy(Blocks.LAVA).noLootTable())
    );
    public static final DeferredBlock<LiquidBlock> HOT_COOLANT_BLOCK = BLOCKS.register(
            "hot_coolant_block",
            () -> new LiquidBlock(ModFluids.HOT_COOLANT_SOURCE.get(), BlockBehaviour.Properties.ofFullCopy(Blocks.WATER).noLootTable())
    );
    public static final DeferredBlock<LiquidBlock> DISTILLED_WATER_BLOCK = BLOCKS.register(
            "distilled_water_block",
            () -> new LiquidBlock(ModFluids.DISTILLED_WATER_SOURCE.get(), BlockBehaviour.Properties.ofFullCopy(Blocks.WATER).noLootTable())
    );
    public static final DeferredBlock<LiquidBlock> EXHAUST_STEAM_BLOCK = BLOCKS.register(
            "exhaust_steam_block",
            () -> new LiquidBlock(ModFluids.EXHAUST_STEAM_SOURCE.get(), BlockBehaviour.Properties.ofFullCopy(Blocks.WATER).noLootTable())
    );

    // v3.0: Охолоджувальний контур, дистанційне керування, паливний цикл, термодинаміка, безпека
    public static final DeferredBlock<Block> COOLING_LOOP = BLOCKS.register(
            "cooling_loop",
            () -> new Block(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_CYAN).strength(4.5f, 15.0f).requiresCorrectToolForDrops().sound(SoundType.METAL))
    );
    public static final DeferredBlock<RemoteControlPortBlock> REMOTE_CONTROL_PORT = BLOCKS.register(
            "remote_control_port",
            () -> new RemoteControlPortBlock(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(5.5f, 25.0f).requiresCorrectToolForDrops().sound(SoundType.METAL))
    );
    public static final DeferredBlock<ControlCableBlock> CONTROL_CABLE = BLOCKS.register(
            "control_cable",
            () -> new ControlCableBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BLACK).strength(1.5f, 5.0f).sound(SoundType.METAL).noOcclusion())
    );
    public static final DeferredBlock<ControlDeskBlock> CONTROL_DESK = BLOCKS.register(
            "control_desk",
            () -> new ControlDeskBlock(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(4.0f, 12.0f).requiresCorrectToolForDrops().sound(SoundType.METAL))
    );
    public static final DeferredBlock<GasCentrifugeBlock> GAS_CENTRIFUGE = BLOCKS.register(
            "gas_centrifuge",
            () -> new GasCentrifugeBlock(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(5.5f, 20.0f).requiresCorrectToolForDrops().sound(SoundType.METAL))
    );
    public static final DeferredBlock<FuelRodAssemblerBlock> FUEL_ROD_ASSEMBLER = BLOCKS.register(
            "fuel_rod_assembler",
            () -> new FuelRodAssemblerBlock(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(4.5f, 15.0f).requiresCorrectToolForDrops().sound(SoundType.METAL))
    );
    public static final DeferredBlock<SpentFuelPoolBlock> SPENT_FUEL_POOL = BLOCKS.register(
            "spent_fuel_pool",
            () -> new SpentFuelPoolBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_LIGHT_BLUE).strength(5.0f, 20.0f).requiresCorrectToolForDrops().sound(SoundType.STONE))
    );
    public static final DeferredBlock<CoolingTowerBlock> COOLING_TOWER = BLOCKS.register(
            "cooling_tower",
            () -> new CoolingTowerBlock(BlockBehaviour.Properties.of().mapColor(MapColor.STONE).strength(5.5f, 25.0f).requiresCorrectToolForDrops().sound(SoundType.STONE))
    );
    public static final DeferredBlock<HeatExchangerBlock> HEAT_EXCHANGER_CONTROLLER = BLOCKS.register(
            "heat_exchanger_controller",
            () -> new HeatExchangerBlock(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(5.5f, 25.0f).requiresCorrectToolForDrops().sound(SoundType.METAL))
    );
    public static final DeferredBlock<EccsUnitBlock> ECCS_UNIT = BLOCKS.register(
            "eccs_unit",
            () -> new EccsUnitBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_RED).strength(5.0f, 20.0f).requiresCorrectToolForDrops().sound(SoundType.METAL))
    );
    public static final DeferredBlock<NuclearWasteCaskBlock> NUCLEAR_WASTE_CASK = BLOCKS.register(
            "nuclear_waste_cask",
            () -> new NuclearWasteCaskBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_GRAY).strength(8.0f, 60.0f).requiresCorrectToolForDrops().sound(SoundType.METAL))
    );
    public static final DeferredBlock<RtgGeneratorBlock> RTG_GENERATOR = BLOCKS.register(
            "rtg_generator",
            () -> new RtgGeneratorBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_YELLOW).strength(5.0f, 20.0f).requiresCorrectToolForDrops().sound(SoundType.METAL))
    );
    public static final DeferredBlock<Block> DEPLETED_URANIUM_BLOCK = BLOCKS.register(
            "depleted_uranium_block",
            () -> new Block(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_GREEN).strength(10.0f, 80.0f).requiresCorrectToolForDrops().sound(SoundType.METAL))
    );
}
