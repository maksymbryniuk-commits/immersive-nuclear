package com.ienuclear.reactor.client;

import blusunrize.immersiveengineering.api.ManualHelper;
import blusunrize.lib.manual.ManualEntry;
import blusunrize.lib.manual.ManualInstance;
import blusunrize.lib.manual.Tree;
import com.ienuclear.reactor.IENuclearMod;
import com.ienuclear.reactor.multiblock.NuclearReactorMultiblock;
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

        ManualEntry.ManualEntryBuilder reactorEntry = new ManualEntry.ManualEntryBuilder(manual);
        reactorEntry.readFromFile(ResourceLocation.fromNamespaceAndPath(IENuclearMod.MODID, "nuclear_reactor"));
        reactorEntry.addSpecialElement(new ManualEntry.SpecialElementData(
                "nuclear_reactor_mb",
                0,
                () -> ManualHelper.MAKE_MULTIBLOCK_ELEMENT.get().create(NuclearReactorMultiblock.INSTANCE)
        ));
        manual.addEntry(nuclearCategory, reactorEntry.create());

        ManualEntry.ManualEntryBuilder turbineEntry = new ManualEntry.ManualEntryBuilder(manual);
        turbineEntry.readFromFile(ResourceLocation.fromNamespaceAndPath(IENuclearMod.MODID, "steam_turbine"));
        turbineEntry.addSpecialElement(new ManualEntry.SpecialElementData(
                "steam_turbine_mb",
                0,
                () -> ManualHelper.MAKE_MULTIBLOCK_ELEMENT.get().create(SteamTurbineMultiblock.INSTANCE)
        ));
        manual.addEntry(nuclearCategory, turbineEntry.create());

        ManualEntry.ManualEntryBuilder enrichmentEntry = new ManualEntry.ManualEntryBuilder(manual);
        enrichmentEntry.readFromFile(ResourceLocation.fromNamespaceAndPath(IENuclearMod.MODID, "uranium_enrichment"));
        manual.addEntry(nuclearCategory, enrichmentEntry.create());
    }
}
