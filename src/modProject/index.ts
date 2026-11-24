import JSZip from 'jszip';
import { GRADLE_AND_CONFIG_FILES, type ModFileEntry } from './gradleAndConfig';
import { JAVA_CORE_FILES } from './javaCore';
import { JAVA_MANUAL_AND_MULTIBLOCK_FILES } from './javaManualIntegration';
import { JAVA_REACTOR_FILES } from './javaReactorLogic';
import { getRenderedTextures } from './pixelTextures';
import { RESOURCE_AND_RECIPE_FILES } from './resourcesAndRecipes';

const BLOCK_IDS = [
  'reactor_casing',
  'reactor_core_rod',
  'reactor_glass',
  'coolant_input_port',
  'energy_output_port',
];

const ITEM_IDS = [
  'yellowcake_uranium',
  'enriched_uranium_pellet',
  'empty_fuel_rod',
  'nuclear_fuel_rod',
  'enriched_nuclear_fuel_rod',
  'depleted_fuel_rod',
  'coolant_bucket',
  'uranium_hexafluoride_bucket',
  'enriched_uranium_bucket',
];

const GENERATED_MODELS_AND_STATES: ModFileEntry[] = [
  // Blockstates
  ...BLOCK_IDS.map((id) => ({
    path: `src/main/resources/assets/ienuclear/blockstates/${id}.json`,
    language: 'json' as const,
    category: 'resources' as const,
    description: `Blockstate JSON для блоку ${id}`,
    content: JSON.stringify(
      {
        variants: {
          '': { model: `ienuclear:block/${id}` },
        },
      },
      null,
      2
    ),
  })),
  {
    path: 'src/main/resources/assets/ienuclear/blockstates/reactor_controller.json',
    language: 'json',
    category: 'resources',
    description: 'Blockstate JSON для контролера реактора з підтримкою орієнтації (facing) та стану active',
    content: JSON.stringify(
      {
        variants: {
          'facing=north,active=false': { model: 'ienuclear:block/reactor_controller' },
          'facing=south,active=false': { model: 'ienuclear:block/reactor_controller', y: 180 },
          'facing=west,active=false': { model: 'ienuclear:block/reactor_controller', y: 270 },
          'facing=east,active=false': { model: 'ienuclear:block/reactor_controller', y: 90 },
          'facing=north,active=true': { model: 'ienuclear:block/reactor_controller_active' },
          'facing=south,active=true': { model: 'ienuclear:block/reactor_controller_active', y: 180 },
          'facing=west,active=true': { model: 'ienuclear:block/reactor_controller_active', y: 270 },
          'facing=east,active=true': { model: 'ienuclear:block/reactor_controller_active', y: 90 },
        },
      },
      null,
      2
    ),
  },

  // Block Models (16x16 textures)
  ...BLOCK_IDS.map((id) => ({
    path: `src/main/resources/assets/ienuclear/models/block/${id}.json`,
    language: 'json' as const,
    category: 'resources' as const,
    description: `Модель блоку ${id} (16×16 текстура)`,
    content: JSON.stringify(
      id === 'reactor_glass'
        ? {
            parent: 'minecraft:block/cube_all',
            render_type: 'minecraft:translucent',
            textures: { all: `ienuclear:block/${id}` },
          }
        : {
            parent: 'minecraft:block/cube_all',
            textures: { all: `ienuclear:block/${id}` },
          },
      null,
      2
    ),
  })),
  {
    path: 'src/main/resources/assets/ienuclear/models/block/reactor_controller.json',
    language: 'json',
    category: 'resources',
    description: 'Модель блоку контролера (неактивний стан)',
    content: JSON.stringify(
      {
        parent: 'minecraft:block/orientable',
        textures: {
          top: 'ienuclear:block/reactor_casing',
          front: 'ienuclear:block/reactor_controller_front',
          side: 'ienuclear:block/reactor_casing',
        },
      },
      null,
      2
    ),
  },
  {
    path: 'src/main/resources/assets/ienuclear/models/block/reactor_controller_active.json',
    language: 'json',
    category: 'resources',
    description: 'Модель блоку контролера (активний стан)',
    content: JSON.stringify(
      {
        parent: 'minecraft:block/orientable',
        textures: {
          top: 'ienuclear:block/reactor_casing',
          front: 'ienuclear:block/reactor_controller_front_active',
          side: 'ienuclear:block/reactor_casing',
        },
      },
      null,
      2
    ),
  },

  // BlockItem Models
  ...['reactor_controller', ...BLOCK_IDS].map((id) => ({
    path: `src/main/resources/assets/ienuclear/models/item/${id}.json`,
    language: 'json' as const,
    category: 'resources' as const,
    description: `Item-модель для блоку ${id}`,
    content: JSON.stringify(
      {
        parent: `ienuclear:block/${id}`,
      },
      null,
      2
    ),
  })),

  // Item Models (16x16 textures)
  ...ITEM_IDS.map((id) => ({
    path: `src/main/resources/assets/ienuclear/models/item/${id}.json`,
    language: 'json' as const,
    category: 'resources' as const,
    description: `Item-модель для предмета ${id} (16×16 текстура)`,
    content: JSON.stringify(
      {
        parent: 'minecraft:item/generated',
        textures: {
          layer0: `ienuclear:item/${id}`,
        },
      },
      null,
      2
    ),
  })),

  // Loot tables
  ...['reactor_controller', ...BLOCK_IDS].map((id) => ({
    path: `src/main/resources/data/ienuclear/loot_table/blocks/${id}.json`,
    language: 'json' as const,
    category: 'resources' as const,
    description: `Loot table для випадіння блоку ${id} при руйнуванні`,
    content: JSON.stringify(
      {
        type: 'minecraft:block',
        pools: [
          {
            rolls: 1,
            bonus_rolls: 0,
            entries: [{ type: 'minecraft:item', name: `ienuclear:${id}` }],
            conditions: [{ condition: 'minecraft:survives_explosion' }],
          },
        ],
      },
      null,
      2
    ),
  })),
];

export const ALL_MOD_FILES: ModFileEntry[] = [
  ...GRADLE_AND_CONFIG_FILES,
  ...JAVA_CORE_FILES,
  ...JAVA_REACTOR_FILES,
  ...JAVA_MANUAL_AND_MULTIBLOCK_FILES,
  ...RESOURCE_AND_RECIPE_FILES,
  ...GENERATED_MODELS_AND_STATES,
];

export async function exportModZip(): Promise<void> {
  const zip = new JSZip();
  const root = zip.folder('ImmersiveNuclear-NeoForge-1.21.1');
  if (!root) return;

  for (const file of ALL_MOD_FILES) {
    root.file(file.path, file.content);
  }

  // Add all 16x16 binary PNG textures
  for (const tex of getRenderedTextures()) {
    root.file(tex.zipPath, tex.pngBytes);
  }

  const blob = await zip.generateAsync({ type: 'blob' });
  const url = URL.createObjectURL(blob);
  const link = document.createElement('a');
  link.href = url;
  link.download = 'ImmersiveNuclear-NeoForge-1.21.1.zip';
  document.body.appendChild(link);
  link.click();
  document.body.removeChild(link);
  URL.revokeObjectURL(url);
}

export type { ModFileEntry };
