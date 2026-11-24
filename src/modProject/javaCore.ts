import type { ModFileEntry } from './gradleAndConfig';

export const JAVA_CORE_FILES: ModFileEntry[] = [
  {
    path: 'src/main/java/com/ienuclear/reactor/IENuclearMod.java',
    language: 'java',
    category: 'java',
    description: 'Головний клас моду @Mod("ienuclear"): реєстрація блоків, рідин, меню, Capabilities (FE/Fluid) та мережі',
    content: `package com.ienuclear.reactor;

import com.ienuclear.reactor.multiblock.NuclearReactorMultiblock;
import com.ienuclear.reactor.network.ToggleReactorPayload;
import com.ienuclear.reactor.registry.ModBlockEntities;
import com.ienuclear.reactor.registry.ModBlocks;
import com.ienuclear.reactor.registry.ModFluids;
import com.ienuclear.reactor.registry.ModItems;
import com.ienuclear.reactor.registry.ModMenus;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

@Mod(IENuclearMod.MODID)
public class IENuclearMod {
    public static final String MODID = "ienuclear";

    public static final DeferredRegister<CreativeModeTab> CREATIVE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> NUCLEAR_TAB = CREATIVE_TABS.register(
            "nuclear_tab",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.ienuclear"))
                    .icon(() -> new ItemStack(ModItems.REACTOR_CONTROLLER_ITEM.get()))
                    .displayItems((params, output) -> {
                        output.accept(ModItems.REACTOR_CONTROLLER_ITEM.get());
                        output.accept(ModItems.REACTOR_CASING_ITEM.get());
                        output.accept(ModItems.REACTOR_CORE_ROD_ITEM.get());
                        output.accept(ModItems.REACTOR_GLASS_ITEM.get());
                        output.accept(ModItems.COOLANT_INPUT_PORT_ITEM.get());
                        output.accept(ModItems.ENERGY_OUTPUT_PORT_ITEM.get());
                        output.accept(ModItems.YELLOWCAKE_URANIUM.get());
                        output.accept(ModItems.ENRICHED_URANIUM_PELLET.get());
                        output.accept(ModItems.EMPTY_FUEL_ROD.get());
                        output.accept(ModItems.NUCLEAR_FUEL_ROD.get());
                        output.accept(ModItems.ENRICHED_NUCLEAR_FUEL_ROD.get());
                        output.accept(ModItems.DEPLETED_FUEL_ROD.get());
                        output.accept(ModItems.COOLANT_BUCKET.get());
                        output.accept(ModItems.URANIUM_HEXAFLUORIDE_BUCKET.get());
                        output.accept(ModItems.ENRICHED_URANIUM_BUCKET.get());
                    })
                    .build()
    );

    public IENuclearMod(IEventBus modEventBus) {
        ModBlocks.BLOCKS.register(modEventBus);
        ModItems.ITEMS.register(modEventBus);
        ModFluids.FLUID_TYPES.register(modEventBus);
        ModFluids.FLUIDS.register(modEventBus);
        ModBlockEntities.BLOCK_ENTITIES.register(modEventBus);
        ModMenus.MENUS.register(modEventBus);
        CREATIVE_TABS.register(modEventBus);

        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::registerCapabilities);
        modEventBus.addListener(this::registerPayloads);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(NuclearReactorMultiblock::register);
    }

    private void registerCapabilities(RegisterCapabilitiesEvent event) {
        // Порт охолоджувача: дозволяє IE Fluid Pipes та іншим трубам подавати охолоджувач
        event.registerBlockEntity(
                Capabilities.FluidHandler.BLOCK,
                ModBlockEntities.COOLANT_INPUT_PORT.get(),
                (blockEntity, side) -> blockEntity.getFluidTank()
        );

        // Порт виводу енергії: віддає FE через IE HV/MV Wire Connectors та кабелі
        event.registerBlockEntity(
                Capabilities.EnergyStorage.BLOCK,
                ModBlockEntities.ENERGY_OUTPUT_PORT.get(),
                (blockEntity, side) -> blockEntity.getEnergyStorage()
        );
    }

    private void registerPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");
        registrar.playToServer(
                ToggleReactorPayload.TYPE,
                ToggleReactorPayload.STREAM_CODEC,
                ToggleReactorPayload::handleServer
        );
    }
}
`
  },
  {
    path: 'src/main/java/com/ienuclear/reactor/registry/ModBlocks.java',
    language: 'java',
    category: 'java',
    description: 'Реєстрація блоків мультиблоку ядерного реактора та блоку охолоджувальної рідини',
    content: `package com.ienuclear.reactor.registry;

import com.ienuclear.reactor.IENuclearMod;
import com.ienuclear.reactor.block.CoolantInputPortBlock;
import com.ienuclear.reactor.block.EnergyOutputPortBlock;
import com.ienuclear.reactor.block.ReactorControllerBlock;
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
}
`
  },
  {
    path: 'src/main/java/com/ienuclear/reactor/registry/ModItems.java',
    language: 'java',
    category: 'java',
    description: 'Реєстрація паливних стрижнів, проміжних уранових матеріалів, відер з рідинами та BlockItems',
    content: `package com.ienuclear.reactor.registry;

import com.ienuclear.reactor.IENuclearMod;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems {
    public static final DeferredRegister.Items ITEMS =
            DeferredRegister.createItems(IENuclearMod.MODID);

    // BlockItems мультиблоку
    public static final DeferredItem<BlockItem> REACTOR_CASING_ITEM =
            ITEMS.registerSimpleBlockItem("reactor_casing", ModBlocks.REACTOR_CASING);
    public static final DeferredItem<BlockItem> REACTOR_CORE_ROD_ITEM =
            ITEMS.registerSimpleBlockItem("reactor_core_rod", ModBlocks.REACTOR_CORE_ROD);
    public static final DeferredItem<BlockItem> REACTOR_GLASS_ITEM =
            ITEMS.registerSimpleBlockItem("reactor_glass", ModBlocks.REACTOR_GLASS);
    public static final DeferredItem<BlockItem> REACTOR_CONTROLLER_ITEM =
            ITEMS.registerSimpleBlockItem("reactor_controller", ModBlocks.REACTOR_CONTROLLER);
    public static final DeferredItem<BlockItem> COOLANT_INPUT_PORT_ITEM =
            ITEMS.registerSimpleBlockItem("coolant_input_port", ModBlocks.COOLANT_INPUT_PORT);
    public static final DeferredItem<BlockItem> ENERGY_OUTPUT_PORT_ITEM =
            ITEMS.registerSimpleBlockItem("energy_output_port", ModBlocks.ENERGY_OUTPUT_PORT);

    // Проміжні матеріали збагачення урану
    public static final DeferredItem<Item> YELLOWCAKE_URANIUM = ITEMS.registerSimpleItem(
            "yellowcake_uranium", new Item.Properties());
    public static final DeferredItem<Item> ENRICHED_URANIUM_PELLET = ITEMS.registerSimpleItem(
            "enriched_uranium_pellet", new Item.Properties());
    public static final DeferredItem<Item> EMPTY_FUEL_ROD = ITEMS.registerSimpleItem(
            "empty_fuel_rod", new Item.Properties().stacksTo(16));

    // Ядерне паливо (стандартне та збагачене) і відпрацьований стрижень
    public static final DeferredItem<Item> NUCLEAR_FUEL_ROD = ITEMS.registerSimpleItem(
            "nuclear_fuel_rod", new Item.Properties().stacksTo(16));
    public static final DeferredItem<Item> ENRICHED_NUCLEAR_FUEL_ROD = ITEMS.registerSimpleItem(
            "enriched_nuclear_fuel_rod", new Item.Properties().stacksTo(16));
    public static final DeferredItem<Item> DEPLETED_FUEL_ROD = ITEMS.registerSimpleItem(
            "depleted_fuel_rod", new Item.Properties().stacksTo(16));

    // Відра для рідин
    public static final DeferredItem<BucketItem> COOLANT_BUCKET = ITEMS.register(
            "coolant_bucket",
            () -> new BucketItem(ModFluids.HEAVY_COOLANT_SOURCE.get(),
                    new Item.Properties().craftRemainder(Items.BUCKET).stacksTo(1))
    );

    public static final DeferredItem<BucketItem> URANIUM_HEXAFLUORIDE_BUCKET = ITEMS.register(
            "uranium_hexafluoride_bucket",
            () -> new BucketItem(ModFluids.URANIUM_HEXAFLUORIDE_SOURCE.get(),
                    new Item.Properties().craftRemainder(Items.BUCKET).stacksTo(1))
    );

    public static final DeferredItem<BucketItem> ENRICHED_URANIUM_BUCKET = ITEMS.register(
            "enriched_uranium_bucket",
            () -> new BucketItem(ModFluids.ENRICHED_URANIUM_SOURCE.get(),
                    new Item.Properties().craftRemainder(Items.BUCKET).stacksTo(1))
    );
}
`
  },
  {
    path: 'src/main/java/com/ienuclear/reactor/registry/ModFluids.java',
    language: 'java',
    category: 'java',
    description: 'Реєстрація охолоджувальної рідини heavy_coolant та уранових розчинів для збагачення в IE Mixer/Refinery',
    content: `package com.ienuclear.reactor.registry;

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
}
`
  },
  {
    path: 'src/main/java/com/ienuclear/reactor/registry/ModBlockEntities.java',
    language: 'java',
    category: 'java',
    description: 'Реєстрація BlockEntity для контролера, порту охолоджувача та порту FE енергії',
    content: `package com.ienuclear.reactor.registry;

import com.ienuclear.reactor.IENuclearMod;
import com.ienuclear.reactor.blockentity.CoolantInputPortBlockEntity;
import com.ienuclear.reactor.blockentity.EnergyOutputPortBlockEntity;
import com.ienuclear.reactor.blockentity.ReactorControllerBlockEntity;
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
}
`
  },
  {
    path: 'src/main/java/com/ienuclear/reactor/registry/ModMenus.java',
    language: 'java',
    category: 'java',
    description: 'Реєстрація MenuType через IMenuTypeExtension для передачі BlockPos на клієнт',
    content: `package com.ienuclear.reactor.registry;

import com.ienuclear.reactor.IENuclearMod;
import com.ienuclear.reactor.menu.ReactorControllerMenu;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModMenus {
    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(Registries.MENU, IENuclearMod.MODID);

    public static final DeferredHolder<MenuType<?>, MenuType<ReactorControllerMenu>> REACTOR_CONTROLLER_MENU =
            MENUS.register("reactor_controller_menu", () ->
                    IMenuTypeExtension.create(ReactorControllerMenu::new));
}
`
  },
  {
    path: 'src/main/java/com/ienuclear/reactor/network/ToggleReactorPayload.java',
    language: 'java',
    category: 'java',
    description: 'Мережевий пакет (C2S) для перемикання запуску/зупинки реакції з інтерфейсу',
    content: `package com.ienuclear.reactor.network;

import com.ienuclear.reactor.IENuclearMod;
import com.ienuclear.reactor.blockentity.ReactorControllerBlockEntity;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ToggleReactorPayload(BlockPos pos) implements CustomPacketPayload {
    public static final Type<ToggleReactorPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(IENuclearMod.MODID, "toggle_reactor"));

    public static final StreamCodec<ByteBuf, ToggleReactorPayload> STREAM_CODEC =
            StreamCodec.composite(
                    BlockPos.STREAM_CODEC,
                    ToggleReactorPayload::pos,
                    ToggleReactorPayload::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handleServer(ToggleReactorPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                if (player.distanceToSqr(payload.pos().getCenter()) > 64.0) {
                    return;
                }
                BlockEntity be = player.level().getBlockEntity(payload.pos());
                if (be instanceof ReactorControllerBlockEntity controller) {
                    controller.toggleActive();
                }
            }
        });
    }
}
`
  }
];
