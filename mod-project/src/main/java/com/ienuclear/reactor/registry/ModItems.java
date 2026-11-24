package com.ienuclear.reactor.registry;

import com.ienuclear.reactor.IENuclearMod;
import com.ienuclear.reactor.item.GeigerCounterItem;
import com.ienuclear.reactor.item.IodineTabletItem;
import net.minecraft.world.item.ArmorItem;
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

    // Ядерне паливо з міцністю (після вичерпання міцності стає порожньою капсулою)
    public static final DeferredItem<Item> NUCLEAR_FUEL_ROD = ITEMS.registerSimpleItem(
            "nuclear_fuel_rod", new Item.Properties().durability(2400));
    public static final DeferredItem<Item> ENRICHED_NUCLEAR_FUEL_ROD = ITEMS.registerSimpleItem(
            "enriched_nuclear_fuel_rod", new Item.Properties().durability(4800));
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

    // Модернізація v1.1: Переробка, MOX-паливо 3x, Дозиметр, Антирад, Коріум
    public static final DeferredItem<BlockItem> MOLTEN_CORIUM_ITEM =
            ITEMS.registerSimpleBlockItem("molten_corium", ModBlocks.MOLTEN_CORIUM);
    public static final DeferredItem<Item> PLUTONIUM_DUST = ITEMS.registerSimpleItem(
            "plutonium_dust", new Item.Properties());
    public static final DeferredItem<Item> MOX_FUEL_CAPSULE = ITEMS.registerSimpleItem(
            "mox_fuel_capsule", new Item.Properties().durability(3600));

    // IC2-подібні пластини та компоненти для активної сітки люку палива
    public static final DeferredItem<BlockItem> FUEL_ACCESS_HATCH_ITEM =
            ITEMS.registerSimpleBlockItem("fuel_access_hatch", ModBlocks.FUEL_ACCESS_HATCH);
    public static final DeferredItem<Item> HEAT_VENT = ITEMS.registerSimpleItem(
            "heat_vent", new Item.Properties().durability(2000));
    public static final DeferredItem<Item> REACTOR_PLATING = ITEMS.registerSimpleItem(
            "reactor_plating", new Item.Properties().durability(3000));
    public static final DeferredItem<Item> HEAT_EXCHANGER = ITEMS.registerSimpleItem(
            "heat_exchanger", new Item.Properties().durability(2500));
    public static final DeferredItem<Item> NEUTRON_REFLECTOR = ITEMS.registerSimpleItem(
            "neutron_reflector", new Item.Properties().durability(1800));
    public static final DeferredItem<GeigerCounterItem> GEIGER_COUNTER = ITEMS.register(
            "geiger_counter",
            () -> new GeigerCounterItem(new Item.Properties().stacksTo(1))
    );
    public static final DeferredItem<IodineTabletItem> IODINE_TABLET = ITEMS.register(
            "iodine_tablet",
            () -> new IodineTabletItem(new Item.Properties().stacksTo(16))
    );

    // v2.0: Блоки парового циклу, Примітивний 1x1 реактор, Відро гарячої пари
    public static final DeferredItem<BlockItem> WATER_INPUT_PORT_ITEM =
            ITEMS.registerSimpleBlockItem("water_input_port", ModBlocks.WATER_INPUT_PORT);
    public static final DeferredItem<BlockItem> STEAM_OUTPUT_PORT_ITEM =
            ITEMS.registerSimpleBlockItem("steam_output_port", ModBlocks.STEAM_OUTPUT_PORT);
    public static final DeferredItem<BlockItem> STEAM_TURBINE_ITEM =
            ITEMS.registerSimpleBlockItem("steam_turbine", ModBlocks.STEAM_TURBINE);
    public static final DeferredItem<BlockItem> TURBINE_CASING_ITEM =
            ITEMS.registerSimpleBlockItem("turbine_casing", ModBlocks.TURBINE_CASING);
    public static final DeferredItem<BlockItem> TURBINE_ROTOR_ITEM =
            ITEMS.registerSimpleBlockItem("turbine_rotor", ModBlocks.TURBINE_ROTOR);
    public static final DeferredItem<BlockItem> STEAM_INPUT_PORT_ITEM =
            ITEMS.registerSimpleBlockItem("steam_input_port", ModBlocks.STEAM_INPUT_PORT);
    public static final DeferredItem<BlockItem> PRIMITIVE_REACTOR_ITEM =
            ITEMS.registerSimpleBlockItem("primitive_reactor", ModBlocks.PRIMITIVE_REACTOR);
    public static final DeferredItem<BucketItem> HOT_STEAM_BUCKET = ITEMS.register(
            "hot_steam_bucket",
            () -> new BucketItem(ModFluids.HOT_STEAM_SOURCE.get(),
                    new Item.Properties().craftRemainder(Items.BUCKET).stacksTo(1))
    );

    // v3.0: Нові паливні елементи, матеріали, броня хімзахисту, відра, блоки
    public static final DeferredItem<Item> THORIUM_FUEL_CAPSULE = ITEMS.register(
            "thorium_fuel_capsule",
            () -> new Item(new Item.Properties().durability(5400).setNoRepair())
    );
    public static final DeferredItem<Item> DEPLETED_URANIUM_INGOT = ITEMS.register(
            "depleted_uranium_ingot",
            () -> new Item(new Item.Properties())
    );
    public static final DeferredItem<Item> TRITIUM_CELL = ITEMS.register(
            "tritium_cell",
            () -> new Item(new Item.Properties().stacksTo(16))
    );
    public static final DeferredItem<Item> AP_URANIUM_SLUG = ITEMS.register(
            "ap_uranium_slug",
            () -> new Item(new Item.Properties().stacksTo(64))
    );

    // Костюм хімзахисту (Hazmat Suit)
    public static final DeferredItem<ArmorItem> HAZMAT_HELMET = ITEMS.register(
            "hazmat_helmet",
            () -> new ArmorItem(ModArmorMaterials.HAZMAT, ArmorItem.Type.HELMET, new Item.Properties().stacksTo(1))
    );
    public static final DeferredItem<ArmorItem> HAZMAT_CHESTPLATE = ITEMS.register(
            "hazmat_chestplate",
            () -> new ArmorItem(ModArmorMaterials.HAZMAT, ArmorItem.Type.CHESTPLATE, new Item.Properties().stacksTo(1).durability(2400))
    );
    public static final DeferredItem<ArmorItem> HAZMAT_LEGGINGS = ITEMS.register(
            "hazmat_leggings",
            () -> new ArmorItem(ModArmorMaterials.HAZMAT, ArmorItem.Type.LEGGINGS, new Item.Properties().stacksTo(1))
    );
    public static final DeferredItem<ArmorItem> HAZMAT_BOOTS = ITEMS.register(
            "hazmat_boots",
            () -> new ArmorItem(ModArmorMaterials.HAZMAT, ArmorItem.Type.BOOTS, new Item.Properties().stacksTo(1))
    );

    // Нові відра рідин v3.0
    public static final DeferredItem<BucketItem> DEPLETED_UF6_BUCKET = ITEMS.register(
            "depleted_uf6_bucket",
            () -> new BucketItem(ModFluids.DEPLETED_UF6_SOURCE.get(), new Item.Properties().craftRemainder(Items.BUCKET).stacksTo(1))
    );
    public static final DeferredItem<BucketItem> LIQUID_SODIUM_BUCKET = ITEMS.register(
            "liquid_sodium_bucket",
            () -> new BucketItem(ModFluids.LIQUID_SODIUM_SOURCE.get(), new Item.Properties().craftRemainder(Items.BUCKET).stacksTo(1))
    );
    public static final DeferredItem<BucketItem> HOT_COOLANT_BUCKET = ITEMS.register(
            "hot_coolant_bucket",
            () -> new BucketItem(ModFluids.HOT_COOLANT_SOURCE.get(), new Item.Properties().craftRemainder(Items.BUCKET).stacksTo(1))
    );
    public static final DeferredItem<BucketItem> DISTILLED_WATER_BUCKET = ITEMS.register(
            "distilled_water_bucket",
            () -> new BucketItem(ModFluids.DISTILLED_WATER_SOURCE.get(), new Item.Properties().craftRemainder(Items.BUCKET).stacksTo(1))
    );
    public static final DeferredItem<BucketItem> EXHAUST_STEAM_BUCKET = ITEMS.register(
            "exhaust_steam_bucket",
            () -> new BucketItem(ModFluids.EXHAUST_STEAM_SOURCE.get(), new Item.Properties().craftRemainder(Items.BUCKET).stacksTo(1))
    );

    // Нові BlockItem v3.0
    public static final DeferredItem<BlockItem> COOLING_LOOP_ITEM =
            ITEMS.registerSimpleBlockItem("cooling_loop", ModBlocks.COOLING_LOOP);
    public static final DeferredItem<BlockItem> REMOTE_CONTROL_PORT_ITEM =
            ITEMS.registerSimpleBlockItem("remote_control_port", ModBlocks.REMOTE_CONTROL_PORT);
    public static final DeferredItem<BlockItem> CONTROL_CABLE_ITEM =
            ITEMS.registerSimpleBlockItem("control_cable", ModBlocks.CONTROL_CABLE);
    public static final DeferredItem<BlockItem> CONTROL_DESK_ITEM =
            ITEMS.registerSimpleBlockItem("control_desk", ModBlocks.CONTROL_DESK);
    public static final DeferredItem<BlockItem> GAS_CENTRIFUGE_ITEM =
            ITEMS.registerSimpleBlockItem("gas_centrifuge", ModBlocks.GAS_CENTRIFUGE);
    public static final DeferredItem<BlockItem> FUEL_ROD_ASSEMBLER_ITEM =
            ITEMS.registerSimpleBlockItem("fuel_rod_assembler", ModBlocks.FUEL_ROD_ASSEMBLER);
    public static final DeferredItem<BlockItem> SPENT_FUEL_POOL_ITEM =
            ITEMS.registerSimpleBlockItem("spent_fuel_pool", ModBlocks.SPENT_FUEL_POOL);
    public static final DeferredItem<BlockItem> COOLING_TOWER_ITEM =
            ITEMS.registerSimpleBlockItem("cooling_tower", ModBlocks.COOLING_TOWER);
    public static final DeferredItem<BlockItem> HEAT_EXCHANGER_CONTROLLER_ITEM =
            ITEMS.registerSimpleBlockItem("heat_exchanger_controller", ModBlocks.HEAT_EXCHANGER_CONTROLLER);
    public static final DeferredItem<BlockItem> ECCS_UNIT_ITEM =
            ITEMS.registerSimpleBlockItem("eccs_unit", ModBlocks.ECCS_UNIT);
    public static final DeferredItem<BlockItem> NUCLEAR_WASTE_CASK_ITEM =
            ITEMS.registerSimpleBlockItem("nuclear_waste_cask", ModBlocks.NUCLEAR_WASTE_CASK);
    public static final DeferredItem<BlockItem> RTG_GENERATOR_ITEM =
            ITEMS.registerSimpleBlockItem("rtg_generator", ModBlocks.RTG_GENERATOR);
    public static final DeferredItem<BlockItem> DEPLETED_URANIUM_BLOCK_ITEM =
            ITEMS.registerSimpleBlockItem("depleted_uranium_block", ModBlocks.DEPLETED_URANIUM_BLOCK);
}
