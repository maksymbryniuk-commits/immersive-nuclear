package com.ienuclear.reactor.client;

import blusunrize.immersiveengineering.api.ManualHelper;
import blusunrize.lib.manual.ManualEntry;
import blusunrize.lib.manual.ManualInstance;
import blusunrize.lib.manual.Tree;
import com.ienuclear.reactor.IENuclearMod;
import com.ienuclear.reactor.multiblock.CoolingTowerMultiblock;
import com.ienuclear.reactor.multiblock.GasCentrifugeMultiblock;
import com.ienuclear.reactor.multiblock.HeatExchangerMultiblock;
import com.ienuclear.reactor.multiblock.NuclearReactor7x7Multiblock;
import com.ienuclear.reactor.multiblock.NuclearReactorMultiblock;
import com.ienuclear.reactor.multiblock.SpentFuelPoolMultiblock;
import com.ienuclear.reactor.multiblock.SteamTurbineMultiblock;
import net.minecraft.resources.ResourceLocation;

public class NuclearManualIntegration {
    private static boolean initialized = false;

    public static void init() {
        if (initialized) return;
        if (!ManualHelper.IE_MANUAL_INSTANCE.isInitialized()) return;
        initialized = true;

        ManualInstance manual = ManualHelper.getManual();
        if (manual == null) return;

        Tree.InnerNode<ResourceLocation, ManualEntry> root = manual.getRoot();
        Tree.InnerNode<ResourceLocation, ManualEntry> nuclearCategory =
                root.getOrCreateSubnode(ResourceLocation.fromNamespaceAndPath(IENuclearMod.MODID, "nuclear_category"), 100);

        ManualEntry.ManualEntryBuilder reactor3x3 = new ManualEntry.ManualEntryBuilder(manual);
        reactor3x3.readFromFile(ResourceLocation.fromNamespaceAndPath(IENuclearMod.MODID, "nuclear_reactor"));
        reactor3x3.addSpecialElement(new ManualEntry.SpecialElementData(
                "nuclear_reactor_mb",
                0,
                () -> ManualHelper.MAKE_MULTIBLOCK_ELEMENT.get().create(NuclearReactorMultiblock.INSTANCE)
        ));
        manual.addEntry(nuclearCategory, reactor3x3.create());

        ManualEntry.ManualEntryBuilder reactor7x7 = new ManualEntry.ManualEntryBuilder(manual);
        reactor7x7.readFromFile(ResourceLocation.fromNamespaceAndPath(IENuclearMod.MODID, "nuclear_reactor_7x7"));
        reactor7x7.addSpecialElement(new ManualEntry.SpecialElementData(
                "nuclear_reactor_7x7_mb",
                0,
                () -> ManualHelper.MAKE_MULTIBLOCK_ELEMENT.get().create(NuclearReactor7x7Multiblock.INSTANCE)
        ));
        manual.addEntry(nuclearCategory, reactor7x7.create());

        ManualEntry.ManualEntryBuilder turbineEntry = new ManualEntry.ManualEntryBuilder(manual);
        turbineEntry.readFromFile(ResourceLocation.fromNamespaceAndPath(IENuclearMod.MODID, "steam_turbine"));
        turbineEntry.addSpecialElement(new ManualEntry.SpecialElementData(
                "steam_turbine_mb",
                0,
                () -> ManualHelper.MAKE_MULTIBLOCK_ELEMENT.get().create(SteamTurbineMultiblock.INSTANCE)
        ));
        manual.addEntry(nuclearCategory, turbineEntry.create());

        ManualEntry.ManualEntryBuilder centrifugeEntry = new ManualEntry.ManualEntryBuilder(manual);
        centrifugeEntry.readFromFile(ResourceLocation.fromNamespaceAndPath(IENuclearMod.MODID, "gas_centrifuge"));
        centrifugeEntry.addSpecialElement(new ManualEntry.SpecialElementData(
                "gas_centrifuge_mb",
                0,
                () -> ManualHelper.MAKE_MULTIBLOCK_ELEMENT.get().create(GasCentrifugeMultiblock.INSTANCE)
        ));
        manual.addEntry(nuclearCategory, centrifugeEntry.create());

        ManualEntry.ManualEntryBuilder poolEntry = new ManualEntry.ManualEntryBuilder(manual);
        poolEntry.readFromFile(ResourceLocation.fromNamespaceAndPath(IENuclearMod.MODID, "spent_fuel_pool"));
        poolEntry.addSpecialElement(new ManualEntry.SpecialElementData(
                "spent_fuel_pool_mb",
                0,
                () -> ManualHelper.MAKE_MULTIBLOCK_ELEMENT.get().create(SpentFuelPoolMultiblock.INSTANCE)
        ));
        manual.addEntry(nuclearCategory, poolEntry.create());

        ManualEntry.ManualEntryBuilder hxEntry = new ManualEntry.ManualEntryBuilder(manual);
        hxEntry.readFromFile(ResourceLocation.fromNamespaceAndPath(IENuclearMod.MODID, "heat_exchanger"));
        hxEntry.addSpecialElement(new ManualEntry.SpecialElementData(
                "heat_exchanger_mb",
                0,
                () -> ManualHelper.MAKE_MULTIBLOCK_ELEMENT.get().create(HeatExchangerMultiblock.INSTANCE)
        ));
        manual.addEntry(nuclearCategory, hxEntry.create());

        ManualEntry.ManualEntryBuilder towerEntry = new ManualEntry.ManualEntryBuilder(manual);
        towerEntry.readFromFile(ResourceLocation.fromNamespaceAndPath(IENuclearMod.MODID, "cooling_tower"));
        towerEntry.addSpecialElement(new ManualEntry.SpecialElementData(
                "cooling_tower_mb",
                0,
                () -> ManualHelper.MAKE_MULTIBLOCK_ELEMENT.get().create(CoolingTowerMultiblock.INSTANCE)
        ));
        manual.addEntry(nuclearCategory, towerEntry.create());

        for (String page : new String[]{"primitive_reactor", "uranium_enrichment", "remote_control", "nuclear_safety_items"}) {
            ManualEntry.ManualEntryBuilder b = new ManualEntry.ManualEntryBuilder(manual);
            b.readFromFile(ResourceLocation.fromNamespaceAndPath(IENuclearMod.MODID, page));
            manual.addEntry(nuclearCategory, b.create());
        }
    }
}
