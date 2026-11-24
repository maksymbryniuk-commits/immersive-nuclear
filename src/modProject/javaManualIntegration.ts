import type { ModFileEntry } from './gradleAndConfig';

export const JAVA_MANUAL_AND_MULTIBLOCK_FILES: ModFileEntry[] = [
  {
    path: 'src/main/java/com/ienuclear/reactor/multiblock/NuclearReactorMultiblock.java',
    language: 'java',
    category: 'java',
    description: 'Реєстрація структури 3×3×3 у MultiblockHandler Immersive Engineering для 3D-візуалізації в Engineerʼs Manual',
    content: `package com.ienuclear.reactor.multiblock;

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
                        new ItemStack(ModBlocks.REACTOR_CASING.get(), 20),
                        new ItemStack(ModBlocks.REACTOR_GLASS.get(), 2),
                        new ItemStack(ModBlocks.REACTOR_CORE_ROD.get(), 1),
                        new ItemStack(ModBlocks.REACTOR_CONTROLLER.get(), 1),
                        new ItemStack(ModBlocks.COOLANT_INPUT_PORT.get(), 1),
                        new ItemStack(ModBlocks.ENERGY_OUTPUT_PORT.get(), 1)
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
        Block energy = ModBlocks.ENERGY_OUTPUT_PORT.get();

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
                            state = energy.defaultBlockState();
                        } else if (x == 1 && z == 0) {
                            state = glass.defaultBlockState();
                        }
                    } else if (y == 2 && x == 1 && z == 1) {
                        state = glass.defaultBlockState();
                    }
                    list.add(new StructureBlockInfo(new BlockPos(x, y, z), state, null));
                }
            }
        }
        return list;
    }
}
`
  },
  {
    path: 'src/main/java/com/ienuclear/reactor/client/NuclearManualIntegration.java',
    language: 'java',
    category: 'java',
    description: 'Додавання розділу «Immersive Nuclear» та сторінки мультиблоку з 3D-візуалізацією у гайд Engineerʼs Manual',
    content: `package com.ienuclear.reactor.client;

import blusunrize.immersiveengineering.api.ManualHelper;
import blusunrize.lib.manual.ManualEntry;
import blusunrize.lib.manual.ManualInstance;
import blusunrize.lib.manual.Tree;
import com.ienuclear.reactor.IENuclearMod;
import com.ienuclear.reactor.multiblock.NuclearReactorMultiblock;
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

        ManualEntry.ManualEntryBuilder enrichmentEntry = new ManualEntry.ManualEntryBuilder(manual);
        enrichmentEntry.readFromFile(ResourceLocation.fromNamespaceAndPath(IENuclearMod.MODID, "uranium_enrichment"));
        manual.addEntry(nuclearCategory, enrichmentEntry.create());
    }
}
`
  },
  {
    path: 'src/main/resources/assets/ienuclear/manual/uk_ua/nuclear_reactor.txt',
    language: 'markdown',
    category: 'resources',
    description: 'Українська сторінка в Engineerʼs Manual з тегом 3D-візуалізації мультиблоку реактора',
    content: `Ядерний реактор (3×3×3)
Промисловий реактор поділу

<&nuclear_reactor_mb>

Мультиблоковий ядерний реактор перетворює енергію поділу урану в стабільний потік Forge Energy (FE), сумісний з високовольтними кабелями та конекторами Immersive Engineering.

Конструкція складається з куба 3×3×3:
- Центр: 1× Стрижень активної зони (Reactor Core Rod).
- Передня стінка: 1× Контролер реактора (доступ до GUI).
- Бічні порти: 1× Порт входу охолоджувача (для IE Fluid Pipes) та 1× Порт виходу FE (для HV конекторів).
- Стіни та перекриття: 20× Корпус реактора та 2× Радіаційно-стійке скло.

Управління та безпека:
Слідкуйте за рівнем охолоджувача (Heavy Coolant) та температурою в контролері. При нагріванні понад 1050°C спрацьовує автоматичне аварійне зупинення (SCRAM).
`
  },
  {
    path: 'src/main/resources/assets/ienuclear/manual/en_us/nuclear_reactor.txt',
    language: 'markdown',
    category: 'resources',
    description: 'English Engineerʼs Manual page with embedded 3D multiblock visualization element',
    content: `Nuclear Reactor (3x3x3)
Industrial Fission Power

<&nuclear_reactor_mb>

The Multiblock Nuclear Reactor converts nuclear fission into Forge Energy (FE), fully compatible with Immersive Engineering HV/MV connectors and fluid pipes.

Structure Layout (3x3x3):
- Center: 1x Reactor Core Assembly.
- Front: 1x Nuclear Reactor Controller (opens thermal & fuel GUI).
- Side Ports: 1x Coolant Input Port and 1x FE Energy Output Port.
- Shell: 20x Lead-Steel Reactor Casing and 2x Radiation-Shielded Glass.

Thermal Safety:
Always supply Heavy Borated Coolant via IE Fluid Pipes. If core temperature reaches 1050 C, automatic SCRAM protection shuts down the reaction.
`
  },
  {
    path: 'src/main/resources/assets/ienuclear/manual/uk_ua/uranium_enrichment.txt',
    language: 'markdown',
    category: 'resources',
    description: 'Сторінка Engineerʼs Manual про збагачення урану та приготування важкого охолоджувача',
    content: `Збагачення урану та охолоджувач
Паливний цикл Immersive Engineering

Для виробництва паливних стрижнів використовуйте промислові станки Immersive Engineering:

1. Дробарка (Crusher): подрібніть сирий уран у Урановий концентрат (Yellowcake).
2. Змішувач (Mixer): розчиніть Yellowcake у воді з редстоуном для отримання UF6.
3. Рафінер (Refinery): збагатіть UF6 етанолом у Збагачений розчин U-235.
4. Металопрес (Metal Press): відштампуйте сталеві пластини (mold_rod) у порожній стрижень та злитки урану в паливні таблетки.
5. Важкий охолоджувач: змішайте в IE Mixer воду, лазурит та селітру (Saltpeter).
`
  },
  {
    path: 'src/main/resources/assets/ienuclear/manual/en_us/uranium_enrichment.txt',
    language: 'markdown',
    category: 'resources',
    description: 'English Engineerʼs Manual page for uranium enrichment & heavy coolant',
    content: `Uranium Enrichment & Coolant
Immersive Engineering Fuel Cycle

Use Immersive Engineering multiblock machines to manufacture nuclear fuel:

1. Crusher: Grind raw uranium into Yellowcake Uranium Concentrate.
2. Mixer: Combine Yellowcake, Water, and Redstone into Uranium Hexafluoride (UF6).
3. Refinery: Enrich UF6 with Ethanol to produce Enriched Uranium Solution (U-235).
4. Metal Press: Stamp Steel Plates (Rod Mold) into Empty Fuel Rods and Uranium Ingots into Pellets.
5. Heavy Coolant: Mix Water, Lapis, and Saltpeter Dust in the IE Mixer for 2.5x cooling efficiency.
`
  },
  {
    path: 'src/main/resources/assets/ienuclear/manual/nuclear_reactor.json',
    language: 'json',
    category: 'resources',
    description: 'JSON-дескриптор сторінки Engineerʼs Manual для ядерного реактора',
    content: `{}\n`
  },
  {
    path: 'src/main/resources/assets/ienuclear/manual/uranium_enrichment.json',
    language: 'json',
    category: 'resources',
    description: 'JSON-дескриптор сторінки Engineerʼs Manual для збагачення урану',
    content: `{}\n`
  }
];
