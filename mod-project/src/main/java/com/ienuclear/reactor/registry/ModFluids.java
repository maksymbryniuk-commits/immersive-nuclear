package com.ienuclear.reactor.registry;

import com.ienuclear.reactor.IENuclearMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.common.SoundActions;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public class ModFluids {
    public static final DeferredRegister<FluidType> FLUID_TYPES =
            DeferredRegister.create(NeoForgeRegistries.Keys.FLUID_TYPES, IENuclearMod.MODID);

    public static final DeferredRegister<Fluid> FLUIDS =
            DeferredRegister.create(Registries.FLUID, IENuclearMod.MODID);

    // 1. Важкий боро-водний охолоджувач (Heavy Coolant)
    public static final DeferredHolder<FluidType, FluidType> HEAVY_COOLANT_TYPE = FLUID_TYPES.register(
            "heavy_coolant",
            () -> new FluidType(FluidType.Properties.create()
                    .descriptionId("fluid.ienuclear.heavy_coolant")
                    .density(1150)
                    .viscosity(1200)
                    .temperature(280)
                    .sound(SoundActions.BUCKET_FILL, SoundEvents.BUCKET_FILL)
                    .sound(SoundActions.BUCKET_EMPTY, SoundEvents.BUCKET_EMPTY))
    );

    public static final DeferredHolder<Fluid, FlowingFluid> HEAVY_COOLANT_SOURCE = FLUIDS.register(
            "heavy_coolant",
            () -> new BaseFlowingFluid.Source(ModFluids.HEAVY_COOLANT_PROPERTIES)
    );

    public static final DeferredHolder<Fluid, FlowingFluid> HEAVY_COOLANT_FLOWING = FLUIDS.register(
            "heavy_coolant_flowing",
            () -> new BaseFlowingFluid.Flowing(ModFluids.HEAVY_COOLANT_PROPERTIES)
    );

    public static final BaseFlowingFluid.Properties HEAVY_COOLANT_PROPERTIES =
            new BaseFlowingFluid.Properties(HEAVY_COOLANT_TYPE, HEAVY_COOLANT_SOURCE, HEAVY_COOLANT_FLOWING)
                    .slopeFindDistance(4)
                    .levelDecreasePerBlock(1)
                    .block(ModBlocks.HEAVY_COOLANT_BLOCK)
                    .bucket(ModItems.COOLANT_BUCKET);

    // 2. Гексафторид урану (UF6) - проміжна рідина для IE Refinery
    public static final DeferredHolder<FluidType, FluidType> URANIUM_HEXAFLUORIDE_TYPE = FLUID_TYPES.register(
            "uranium_hexafluoride",
            () -> new FluidType(FluidType.Properties.create()
                    .descriptionId("fluid.ienuclear.uranium_hexafluoride")
                    .density(1800)
                    .viscosity(1500)
                    .temperature(330))
    );

    public static final DeferredHolder<Fluid, FlowingFluid> URANIUM_HEXAFLUORIDE_SOURCE = FLUIDS.register(
            "uranium_hexafluoride",
            () -> new BaseFlowingFluid.Source(ModFluids.UF6_PROPERTIES)
    );

    public static final DeferredHolder<Fluid, FlowingFluid> URANIUM_HEXAFLUORIDE_FLOWING = FLUIDS.register(
            "uranium_hexafluoride_flowing",
            () -> new BaseFlowingFluid.Flowing(ModFluids.UF6_PROPERTIES)
    );

    public static final BaseFlowingFluid.Properties UF6_PROPERTIES =
            new BaseFlowingFluid.Properties(URANIUM_HEXAFLUORIDE_TYPE, URANIUM_HEXAFLUORIDE_SOURCE, URANIUM_HEXAFLUORIDE_FLOWING)
                    .slopeFindDistance(3)
                    .levelDecreasePerBlock(2)
                    .block(ModBlocks.URANIUM_HEXAFLUORIDE_BLOCK)
                    .bucket(ModItems.URANIUM_HEXAFLUORIDE_BUCKET);

    // 3. Збагачений розчин урану (U-235) - вихід IE Refinery
    public static final DeferredHolder<FluidType, FluidType> ENRICHED_URANIUM_TYPE = FLUID_TYPES.register(
            "enriched_uranium_solution",
            () -> new FluidType(FluidType.Properties.create()
                    .descriptionId("fluid.ienuclear.enriched_uranium_solution")
                    .density(2100)
                    .viscosity(1400)
                    .lightLevel(6)
                    .temperature(340))
    );

    public static final DeferredHolder<Fluid, FlowingFluid> ENRICHED_URANIUM_SOURCE = FLUIDS.register(
            "enriched_uranium_solution",
            () -> new BaseFlowingFluid.Source(ModFluids.ENRICHED_PROPERTIES)
    );

    public static final DeferredHolder<Fluid, FlowingFluid> ENRICHED_URANIUM_FLOWING = FLUIDS.register(
            "enriched_uranium_solution_flowing",
            () -> new BaseFlowingFluid.Flowing(ModFluids.ENRICHED_PROPERTIES)
    );

    public static final BaseFlowingFluid.Properties ENRICHED_PROPERTIES =
            new BaseFlowingFluid.Properties(ENRICHED_URANIUM_TYPE, ENRICHED_URANIUM_SOURCE, ENRICHED_URANIUM_FLOWING)
                    .slopeFindDistance(3)
                    .levelDecreasePerBlock(2)
                    .block(ModBlocks.ENRICHED_URANIUM_BLOCK)
                    .bucket(ModItems.ENRICHED_URANIUM_BUCKET);

    // 4. Гаряча пара (Hot Steam) — вихід мультиблокового ядерного реактора для парової турбіни
    public static final DeferredHolder<FluidType, FluidType> HOT_STEAM_TYPE = FLUID_TYPES.register(
            "hot_steam",
            () -> new FluidType(FluidType.Properties.create()
                    .descriptionId("fluid.ienuclear.hot_steam")
                    .density(-500)
                    .viscosity(200)
                    .temperature(600)
                    .sound(SoundActions.BUCKET_FILL, SoundEvents.BUCKET_FILL)
                    .sound(SoundActions.BUCKET_EMPTY, SoundEvents.BUCKET_EMPTY))
    );

    public static final DeferredHolder<Fluid, FlowingFluid> HOT_STEAM_SOURCE = FLUIDS.register(
            "hot_steam",
            () -> new BaseFlowingFluid.Source(ModFluids.HOT_STEAM_PROPERTIES)
    );

    public static final DeferredHolder<Fluid, FlowingFluid> HOT_STEAM_FLOWING = FLUIDS.register(
            "hot_steam_flowing",
            () -> new BaseFlowingFluid.Flowing(ModFluids.HOT_STEAM_PROPERTIES)
    );

    public static final BaseFlowingFluid.Properties HOT_STEAM_PROPERTIES =
            new BaseFlowingFluid.Properties(HOT_STEAM_TYPE, HOT_STEAM_SOURCE, HOT_STEAM_FLOWING)
                    .slopeFindDistance(2)
                    .levelDecreasePerBlock(2)
                    .block(ModBlocks.HOT_STEAM_BLOCK)
                    .bucket(ModItems.HOT_STEAM_BUCKET);

    // 5. Збіднений UF6 (вихід нижнього порту Газової центрифуги)
    public static final DeferredHolder<FluidType, FluidType> DEPLETED_UF6_TYPE = FLUID_TYPES.register(
            "depleted_uf6",
            () -> new FluidType(FluidType.Properties.create().descriptionId("fluid.ienuclear.depleted_uf6").density(2200).viscosity(1800))
    );
    public static final DeferredHolder<Fluid, FlowingFluid> DEPLETED_UF6_SOURCE = FLUIDS.register(
            "depleted_uf6", () -> new BaseFlowingFluid.Source(ModFluids.DEPLETED_UF6_PROPERTIES));
    public static final DeferredHolder<Fluid, FlowingFluid> DEPLETED_UF6_FLOWING = FLUIDS.register(
            "depleted_uf6_flowing", () -> new BaseFlowingFluid.Flowing(ModFluids.DEPLETED_UF6_PROPERTIES));
    public static final BaseFlowingFluid.Properties DEPLETED_UF6_PROPERTIES =
            new BaseFlowingFluid.Properties(DEPLETED_UF6_TYPE, DEPLETED_UF6_SOURCE, DEPLETED_UF6_FLOWING)
                    .slopeFindDistance(2).levelDecreasePerBlock(2)
                    .block(ModBlocks.DEPLETED_UF6_BLOCK).bucket(ModItems.DEPLETED_UF6_BUCKET);

    // 6. Рідкий натрій (теплоносій 2x ефективності в теплообміннику)
    public static final DeferredHolder<FluidType, FluidType> LIQUID_SODIUM_TYPE = FLUID_TYPES.register(
            "liquid_sodium",
            () -> new FluidType(FluidType.Properties.create().descriptionId("fluid.ienuclear.liquid_sodium").density(950).viscosity(700).temperature(400))
    );
    public static final DeferredHolder<Fluid, FlowingFluid> LIQUID_SODIUM_SOURCE = FLUIDS.register(
            "liquid_sodium", () -> new BaseFlowingFluid.Source(ModFluids.LIQUID_SODIUM_PROPERTIES));
    public static final DeferredHolder<Fluid, FlowingFluid> LIQUID_SODIUM_FLOWING = FLUIDS.register(
            "liquid_sodium_flowing", () -> new BaseFlowingFluid.Flowing(ModFluids.LIQUID_SODIUM_PROPERTIES));
    public static final BaseFlowingFluid.Properties LIQUID_SODIUM_PROPERTIES =
            new BaseFlowingFluid.Properties(LIQUID_SODIUM_TYPE, LIQUID_SODIUM_SOURCE, LIQUID_SODIUM_FLOWING)
                    .slopeFindDistance(4).levelDecreasePerBlock(1)
                    .block(ModBlocks.LIQUID_SODIUM_BLOCK).bucket(ModItems.LIQUID_SODIUM_BUCKET);

    // 7. Гарячий теплоносій першого контуру
    public static final DeferredHolder<FluidType, FluidType> HOT_COOLANT_TYPE = FLUID_TYPES.register(
            "hot_coolant",
            () -> new FluidType(FluidType.Properties.create().descriptionId("fluid.ienuclear.hot_coolant").density(1050).viscosity(900).temperature(750))
    );
    public static final DeferredHolder<Fluid, FlowingFluid> HOT_COOLANT_SOURCE = FLUIDS.register(
            "hot_coolant", () -> new BaseFlowingFluid.Source(ModFluids.HOT_COOLANT_PROPERTIES));
    public static final DeferredHolder<Fluid, FlowingFluid> HOT_COOLANT_FLOWING = FLUIDS.register(
            "hot_coolant_flowing", () -> new BaseFlowingFluid.Flowing(ModFluids.HOT_COOLANT_PROPERTIES));
    public static final BaseFlowingFluid.Properties HOT_COOLANT_PROPERTIES =
            new BaseFlowingFluid.Properties(HOT_COOLANT_TYPE, HOT_COOLANT_SOURCE, HOT_COOLANT_FLOWING)
                    .slopeFindDistance(4).levelDecreasePerBlock(1)
                    .block(ModBlocks.HOT_COOLANT_BLOCK).bucket(ModItems.HOT_COOLANT_BUCKET);

    // 8. Дистильована вода (вихід Градирні)
    public static final DeferredHolder<FluidType, FluidType> DISTILLED_WATER_TYPE = FLUID_TYPES.register(
            "distilled_water",
            () -> new FluidType(FluidType.Properties.create().descriptionId("fluid.ienuclear.distilled_water").density(1000).viscosity(1000))
    );
    public static final DeferredHolder<Fluid, FlowingFluid> DISTILLED_WATER_SOURCE = FLUIDS.register(
            "distilled_water", () -> new BaseFlowingFluid.Source(ModFluids.DISTILLED_WATER_PROPERTIES));
    public static final DeferredHolder<Fluid, FlowingFluid> DISTILLED_WATER_FLOWING = FLUIDS.register(
            "distilled_water_flowing", () -> new BaseFlowingFluid.Flowing(ModFluids.DISTILLED_WATER_PROPERTIES));
    public static final BaseFlowingFluid.Properties DISTILLED_WATER_PROPERTIES =
            new BaseFlowingFluid.Properties(DISTILLED_WATER_TYPE, DISTILLED_WATER_SOURCE, DISTILLED_WATER_FLOWING)
                    .slopeFindDistance(4).levelDecreasePerBlock(1)
                    .block(ModBlocks.DISTILLED_WATER_BLOCK).bucket(ModItems.DISTILLED_WATER_BUCKET);

    // 9. Відпрацьована пара (вихід турбіни -> в Градирню)
    public static final DeferredHolder<FluidType, FluidType> EXHAUST_STEAM_TYPE = FLUID_TYPES.register(
            "exhaust_steam",
            () -> new FluidType(FluidType.Properties.create().descriptionId("fluid.ienuclear.exhaust_steam").density(-300).viscosity(300).temperature(370))
    );
    public static final DeferredHolder<Fluid, FlowingFluid> EXHAUST_STEAM_SOURCE = FLUIDS.register(
            "exhaust_steam", () -> new BaseFlowingFluid.Source(ModFluids.EXHAUST_STEAM_PROPERTIES));
    public static final DeferredHolder<Fluid, FlowingFluid> EXHAUST_STEAM_FLOWING = FLUIDS.register(
            "exhaust_steam_flowing", () -> new BaseFlowingFluid.Flowing(ModFluids.EXHAUST_STEAM_PROPERTIES));
    public static final BaseFlowingFluid.Properties EXHAUST_STEAM_PROPERTIES =
            new BaseFlowingFluid.Properties(EXHAUST_STEAM_TYPE, EXHAUST_STEAM_SOURCE, EXHAUST_STEAM_FLOWING)
                    .slopeFindDistance(2).levelDecreasePerBlock(2)
                    .block(ModBlocks.EXHAUST_STEAM_BLOCK).bucket(ModItems.EXHAUST_STEAM_BUCKET);
}
