import { useMemo, useState } from 'react';
import { BookOpen, Layers, RotateCw, ShieldAlert, Cpu, Flame, Wrench } from 'lucide-react';
import { getTextureById } from '../modProject/pixelTextures';
import { RAW_IE_TEXTURES } from '../modProject/rawIETextures';

type EncyclopediaTab = 'reactor_3x3' | 'reactor_7x7' | 'turbine_3x3x5' | 'primitive_1x1' | 'ic2_grid';

interface BlockMeta {
  id: string;
  name: string;
  short: string;
  color: string;
  textureId?: string;
  rawTex?: string;
  role: string;
}

const BLOCK_CATALOG: Record<string, BlockMeta> = {
  casing: {
    id: 'ienuclear:reactor_casing',
    name: 'Корпус реактора',
    short: 'RC',
    color: '#475569',
    textureId: 'reactor_casing',
    role: 'Свинцево-сталевий радіаційний захист стінок реактора',
  },
  glass: {
    id: 'ienuclear:reactor_glass',
    name: 'Броньоване скло',
    short: 'GL',
    color: '#38BDF8',
    textureId: 'reactor_glass',
    role: 'Оглядове скло Panzerglass для контролю активної зони або ротора',
  },
  core: {
    id: 'ienuclear:reactor_core_rod',
    name: 'Стрижень активної зони',
    short: 'CORE',
    color: '#10B981',
    textureId: 'reactor_core_rod',
    role: 'Внутрішній блок поділу урану (від 1 у 3×3×3 до 27+ у 7×7×7)',
  },
  controller: {
    id: 'ienuclear:reactor_controller',
    name: 'Контролер реактора (ПК)',
    short: 'CTRL',
    color: '#F59E0B',
    textureId: 'reactor_controller_front_active',
    role: 'Лицьовий CRT-термінал керування (БЕЗ слотів палива — лише телеметрія та рубильник)',
  },
  hatch: {
    id: 'ienuclear:fuel_access_hatch',
    name: 'Люк палива (IC2 6×3)',
    short: 'FUEL',
    color: '#EAB308',
    rawTex: RAW_IE_TEXTURES.heavy_engineering,
    role: 'Ставиться ЗВЕРХУ реактора! Містить 18-слотову IC2-сітку для паливних капсул і пластин',
  },
  coolant: {
    id: 'ienuclear:coolant_input_port',
    name: 'Порт охолоджувача',
    short: 'COOL',
    color: '#06B6D4',
    textureId: 'coolant_input_port',
    role: 'Вхід Heavy Coolant або води для стримування температури',
  },
  water: {
    id: 'ienuclear:water_input_port',
    name: 'Порт входу води',
    short: 'H2O',
    color: '#2563EB',
    rawTex: RAW_IE_TEXTURES.fluid_pump_side_in,
    role: 'Вхід води, яка кипить у реакторі та перетворюється на Гарячу пару (1:10)',
  },
  steamOut: {
    id: 'ienuclear:steam_output_port',
    name: 'Порт виходу пари',
    short: 'STM',
    color: '#E2E8F0',
    rawTex: RAW_IE_TEXTURES.fluid_pipe,
    role: 'Автоматично подає Гарячу пару трубами IE до Парової турбіни',
  },
  turbineCtrl: {
    id: 'ienuclear:steam_turbine',
    name: 'Контролер турбіни',
    short: 'TURB',
    color: '#D97706',
    rawTex: RAW_IE_TEXTURES.generator,
    role: 'Передня панель турбіни 3×3×5 (показує RPM, статус та FE/t)',
  },
  turbineCasing: {
    id: 'ienuclear:turbine_casing',
    name: 'Корпус турбіни',
    short: 'TC',
    color: '#78350F',
    rawTex: RAW_IE_TEXTURES.sheetmetal_steel,
    role: 'Зовнішня оболонка парової турбіни 3×3×5',
  },
  turbineRotor: {
    id: 'ienuclear:turbine_rotor',
    name: 'Ротор турбіни',
    short: 'ROT',
    color: '#94A3B8',
    rawTex: RAW_IE_TEXTURES.radiator,
    role: '3 блоки по центру осі всередині турбіни (розкручується парою до 3600 RPM)',
  },
  steamIn: {
    id: 'ienuclear:steam_input_port',
    name: 'Порт входу пари',
    short: 'SIN',
    color: '#BAE6FD',
    rawTex: RAW_IE_TEXTURES.fluid_pump_side_in,
    role: 'Приймає Гарячу пару від реактора',
  },
  energyOut: {
    id: 'ienuclear:energy_output_port',
    name: 'Порт виходу FE',
    short: 'FE',
    color: '#F97316',
    textureId: 'energy_output_port',
    role: 'Видає згенеровану енергію FE у HV/MV конектори IE',
  },
};

type BlockKey = keyof typeof BLOCK_CATALOG;

// 3x3x3 Structure (with Top Fuel Access Hatch)
const STRUCTURE_3X3: (BlockKey | null)[][][] = [
  [
    ['casing', 'casing', 'casing'],
    ['casing', 'casing', 'casing'],
    ['casing', 'casing', 'casing'],
  ],
  [
    ['casing', 'water', 'casing'],
    ['coolant', 'core', 'steamOut'],
    ['casing', 'controller', 'casing'],
  ],
  [
    ['casing', 'casing', 'casing'],
    ['casing', 'hatch', 'casing'],
    ['casing', 'glass', 'casing'],
  ],
];

// 3x3x5 Turbine (width 3, height 3, depth 5)
const STRUCTURE_TURBINE: (BlockKey | null)[][][] = [
  Array.from({ length: 5 }, () => ['turbineCasing', 'turbineCasing', 'turbineCasing']),
  [
    ['turbineCasing', 'turbineCasing', 'turbineCasing'],
    ['turbineCasing', 'turbineRotor', 'turbineCasing'],
    ['steamIn', 'turbineRotor', 'energyOut'],
    ['turbineCasing', 'turbineRotor', 'turbineCasing'],
    ['turbineCasing', 'turbineCtrl', 'turbineCasing'],
  ],
  [
    ['turbineCasing', 'turbineCasing', 'turbineCasing'],
    ['turbineCasing', 'glass', 'turbineCasing'],
    ['turbineCasing', 'glass', 'turbineCasing'],
    ['turbineCasing', 'glass', 'turbineCasing'],
    ['turbineCasing', 'turbineCasing', 'turbineCasing'],
  ],
];

// 7x7x7 Mega Reactor (7x7x7)
const STRUCTURE_7X7: (BlockKey | null)[][][] = Array.from({ length: 7 }, (_, y) =>
  Array.from({ length: 7 }, (_, z) =>
    Array.from({ length: 7 }, (_, x) => {
      const onShell = x === 0 || x === 6 || y === 0 || y === 6 || z === 0 || z === 6;
      if (onShell) {
        if (y === 3 && x === 3 && z === 6) return 'controller';
        if (y === 6 && x === 3 && z === 3) return 'hatch';
        if (x === 0 && y === 3 && (z === 2 || z === 4)) return 'coolant';
        if (x === 6 && y === 3 && (z === 2 || z === 4)) return 'steamOut';
        if (z === 0 && y === 3 && (x === 2 || x === 4)) return 'water';
        if (z === 6 && y >= 2 && y <= 4 && (x === 2 || x === 4)) return 'glass';
        return 'casing';
      }
      if (x >= 2 && x <= 4 && y >= 2 && y <= 4 && z >= 2 && z <= 4) return 'core';
      return null;
    })
  )
);

function getBlockImage(key: BlockKey): string {
  const m = BLOCK_CATALOG[key];
  if (m.textureId) return getTextureById(m.textureId)?.dataUrl ?? '';
  return m.rawTex ?? '';
}

function IsometricViewer({
  structure,
  title,
}: {
  structure: (BlockKey | null)[][][];
  title: string;
}) {
  const height = structure.length;
  const depth = structure[0].length;
  const width = structure[0][0].length;

  const [selectedLayer, setSelectedLayer] = useState<number>(Math.floor(height / 2));
  const [showAll, setShowAll] = useState<boolean>(true);
  const [yawStep, setYawStep] = useState<number>(0);
  const [selectedKey, setSelectedKey] = useState<BlockKey | null>(null);

  const isoBlocks = useMemo(() => {
    const blocks: { x: number; y: number; z: number; key: BlockKey; rx: number; rz: number }[] = [];
    for (let y = 0; y < height; y++) {
      if (!showAll && y !== selectedLayer) continue;
      for (let z = 0; z < depth; z++) {
        for (let x = 0; x < width; x++) {
          const key = structure[y][z][x];
          if (!key) continue;
          let rx = x;
          let rz = z;
          if (yawStep === 1) {
            rx = z;
            rz = width - 1 - x;
          } else if (yawStep === 2) {
            rx = width - 1 - x;
            rz = depth - 1 - z;
          } else if (yawStep === 3) {
            rx = depth - 1 - z;
            rz = x;
          }
          blocks.push({ x, y, z, key, rx, rz });
        }
      }
    }
    blocks.sort((a, b) => a.rx + a.rz - (b.rx + b.rz) || a.y - b.y);
    return blocks;
  }, [structure, height, depth, width, showAll, selectedLayer, yawStep]);

  const counts = useMemo(() => {
    const map: Partial<Record<BlockKey, number>> = {};
    for (const layer of structure) {
      for (const row of layer) {
        for (const k of row) {
          if (k) map[k] = (map[k] ?? 0) + 1;
        }
      }
    }
    return Object.entries(map) as [BlockKey, number][];
  }, [structure]);

  const activeKey = selectedKey ?? counts[0][0];
  const scale = width >= 7 ? 15 : width >= 5 || depth >= 5 ? 21 : 30;
  const tileW = scale;
  const tileH = Math.round(scale * 0.5);
  const blockH = Math.round(scale * 0.88);
  const centerX = 200;
  const baseY = width >= 7 ? 245 : 230;

  return (
    <div className="grid grid-cols-1 lg:grid-cols-12 gap-6">
      {/* 3D Isometric Preview */}
      <div className="lg:col-span-6 bg-[#17130E] border border-[#3E3426] rounded-xl p-4 space-y-4">
        <div className="flex items-center justify-between gap-2">
          <span className="text-xs font-mono text-amber-300">{title}</span>
          <button
            onClick={() => setYawStep((s) => (s + 1) % 4)}
            className="inline-flex items-center gap-1.5 px-2.5 py-1 text-xs font-mono bg-[#2A241C] border border-[#473D2F] rounded text-slate-200 hover:border-amber-500/60"
          >
            <RotateCw className="w-3.5 h-3.5" />
            {yawStep * 90}°
          </button>
        </div>

        <div className="bg-[#120F0B] border border-[#322B20] rounded-lg p-2 flex items-center justify-center overflow-hidden">
          <svg viewBox="0 0 400 300" className="w-full h-72 select-none">
            {isoBlocks.map((b) => {
              const cx = centerX + (b.rx - b.rz) * (tileW * 0.88);
              const cy = baseY + (b.rx + b.rz) * (tileH * 0.65) - b.y * blockH;
              const meta = BLOCK_CATALOG[b.key];
              const isGlass = b.key === 'glass';
              const isSelected = activeKey === b.key;

              const topPts = `${cx},${cy - tileH} ${cx + tileW},${cy} ${cx},${cy + tileH} ${cx - tileW},${cy}`;
              const leftPts = `${cx - tileW},${cy} ${cx},${cy + tileH} ${cx},${cy + tileH + blockH} ${cx - tileW},${cy + blockH}`;
              const rightPts = `${cx + tileW},${cy} ${cx},${cy + tileH} ${cx},${cy + tileH + blockH} ${cx + tileW},${cy + blockH}`;

              return (
                <g
                  key={`${b.x}-${b.y}-${b.z}`}
                  onClick={() => setSelectedKey(b.key)}
                  className="cursor-pointer"
                  opacity={isGlass ? 0.55 : 1}
                >
                  <polygon
                    points={topPts}
                    fill={meta.color}
                    stroke={isSelected ? '#FBBF24' : '#0B0E14'}
                    strokeWidth={isSelected ? 1.5 : 0.8}
                  />
                  <polygon
                    points={leftPts}
                    fill={meta.color}
                    fillOpacity={0.78}
                    stroke={isSelected ? '#FBBF24' : '#0B0E14'}
                    strokeWidth={isSelected ? 1.5 : 0.8}
                  />
                  <polygon
                    points={rightPts}
                    fill={meta.color}
                    fillOpacity={0.55}
                    stroke={isSelected ? '#FBBF24' : '#0B0E14'}
                    strokeWidth={isSelected ? 1.5 : 0.8}
                  />
                </g>
              );
            })}
          </svg>
        </div>

        {/* Layer Slicer */}
        <div className="flex flex-wrap items-center justify-between gap-2">
          <button
            onClick={() => setShowAll((v) => !v)}
            className={`inline-flex items-center gap-1.5 px-3 py-1 text-xs font-mono rounded border ${
              showAll
                ? 'bg-amber-500 text-slate-950 border-amber-400 font-semibold'
                : 'bg-[#2A241C] text-slate-300 border-[#473D2F]'
            }`}
          >
            <Layers className="w-3.5 h-3.5" />
            Усі шари
          </button>
          <div className="flex flex-wrap gap-1">
            {Array.from({ length: height }, (_, lvl) => (
              <button
                key={lvl}
                onClick={() => {
                  setShowAll(false);
                  setSelectedLayer(lvl);
                }}
                className={`px-2 py-1 text-xs font-mono rounded border ${
                  !showAll && selectedLayer === lvl
                    ? 'bg-amber-500 text-slate-950 border-amber-400 font-semibold'
                    : 'bg-[#2A241C] text-slate-300 border-[#473D2F]'
                }`}
              >
                Y={lvl}
              </button>
            ))}
          </div>
        </div>
      </div>

      {/* Bill of Materials */}
      <div className="lg:col-span-6 space-y-4">
        <div className="bg-[#0B0E14] border border-slate-800 rounded-xl p-4 space-y-3">
          <div className="text-xs font-mono text-slate-400">Необхідні матеріали:</div>
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-2">
            {counts.map(([k, count]) => {
              const m = BLOCK_CATALOG[k];
              return (
                <button
                  key={k}
                  onClick={() => setSelectedKey(k)}
                  className={`flex items-center gap-3 p-2.5 rounded-lg border text-left transition-colors ${
                    activeKey === k
                      ? 'bg-amber-500/10 border-amber-500/60'
                      : 'bg-[#121722] border-slate-800 hover:border-slate-700'
                  }`}
                >
                  <div
                    className="w-7 h-7 rounded shrink-0 flex items-center justify-center border border-slate-700 overflow-hidden bg-slate-950"
                    style={{ backgroundColor: getBlockImage(k) ? undefined : m.color }}
                  >
                    {getBlockImage(k) && (
                      <img
                        src={getBlockImage(k)}
                        alt={m.name}
                        className="w-full h-full"
                        style={{ imageRendering: 'pixelated' }}
                      />
                    )}
                  </div>
                  <div className="min-w-0 flex-1">
                    <div className="text-xs font-medium text-slate-100 truncate">
                      {count}× {m.name}
                    </div>
                    <div className="text-[10px] font-mono text-slate-400 truncate">{m.id}</div>
                  </div>
                </button>
              );
            })}
          </div>
        </div>

        <div className="bg-[#0B0E14] border border-slate-800 rounded-xl p-4 space-y-1.5">
          <div className="flex items-center justify-between gap-2">
            <span className="text-sm font-semibold text-amber-300">
              {BLOCK_CATALOG[activeKey].name}
            </span>
            <span className="text-xs font-mono text-slate-400">{BLOCK_CATALOG[activeKey].id}</span>
          </div>
          <p className="text-xs text-slate-300 leading-relaxed">{BLOCK_CATALOG[activeKey].role}</p>
        </div>
      </div>
    </div>
  );
}

export function MultiblockBlueprintView() {
  const [tab, setTab] = useState<EncyclopediaTab>('reactor_3x3');

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col lg:flex-row lg:items-end justify-between gap-4 pb-6 border-b border-slate-800">
        <div>
          <p className="text-xs font-mono text-amber-400 mb-2">
            Engineer&apos;s Manual · Immersive Nuclear v2.3
          </p>
          <h1 className="text-2xl sm:text-3xl font-semibold tracking-tight text-slate-100">
            Енциклопедія реакторів, турбіни та IC2-пластин
          </h1>
        </div>
      </div>

      {/* 5 Separate Encyclopedia Tabs */}
      <div className="flex flex-wrap gap-2">
        {(
          [
            { id: 'reactor_3x3', label: 'Реактор 3×3×3', icon: Cpu },
            { id: 'reactor_7x7', label: 'Мегареактор 7×7×7', icon: Flame },
            { id: 'turbine_3x3x5', label: 'Парова турбіна 3×3×5', icon: RotateCw },
            { id: 'primitive_1x1', label: 'Одноблочний 1×1 (+Крафт)', icon: Wrench },
            { id: 'ic2_grid', label: 'IC2 Сітка та пластини', icon: ShieldAlert },
          ] as const
        ).map((item) => {
          const Icon = item.icon;
          const active = tab === item.id;
          return (
            <button
              key={item.id}
              onClick={() => setTab(item.id)}
              className={`inline-flex items-center gap-2 px-4 py-2 text-xs font-medium rounded-lg border transition-colors ${
                active
                  ? 'bg-amber-500 text-slate-950 border-amber-400 font-semibold'
                  : 'bg-[#121722] text-slate-300 border-slate-800 hover:border-slate-700'
              }`}
            >
              <Icon className="w-3.5 h-3.5" />
              {item.label}
            </button>
          );
        })}
      </div>

      {/* Content */}
      <div className="bg-[#1B1712] border-2 border-[#473D2F] rounded-xl p-6 shadow-xl">
        {tab === 'reactor_3x3' && (
          <div className="space-y-4">
            <div className="flex items-center gap-2 text-amber-300 font-semibold">
              <BookOpen className="w-4 h-4" />
              Ядерний реактор 3×3×3 (Паровий котел із верхнім люком IC2)
            </div>
            <p className="text-xs text-slate-300 leading-relaxed">
              Паливо більше не завантажується через Контролер! Поставте{' '}
              <strong className="text-amber-300">Люк завантаження палива (Fuel Access Hatch)</strong>{' '}
              на верхню кришку реактора — у ньому відкривається 6×3 сітка в стилі IndustrialCraft 2.
            </p>
            <IsometricViewer structure={STRUCTURE_3X3} title="Структура 3×3×3 (Y=2: Люк палива зверху)" />
          </div>
        )}

        {tab === 'reactor_7x7' && (
          <div className="space-y-4">
            <div className="flex items-center gap-2 text-amber-300 font-semibold">
              <Flame className="w-4 h-4" />
              Мегареактор 7×7×7 (27+ ядер активної зони)
            </div>
            <p className="text-xs text-slate-300 leading-relaxed">
              Динамічний сканер підтримує будь-який розмір від 3×3×3 до 7×7×7. У версії 7×7×7 всередині
              розміщується куб із 27 блоків активної зони, 2+ Порти охолоджувача, 2+ Порти води та 2+
              Порти виходу пари для одночасного живлення кількох Парових турбін 3×3×5.
            </p>
            <IsometricViewer structure={STRUCTURE_7X7} title="Структура 7×7×7 (27 ядер + 2× порти)" />
          </div>
        )}

        {tab === 'turbine_3x3x5' && (
          <div className="space-y-4">
            <div className="flex items-center gap-2 text-amber-300 font-semibold">
              <RotateCw className="w-4 h-4" />
              Мультиблокова Парова турбіна 3×3×5 (3600 RPM · до 36 000 FE/t)
            </div>
            <p className="text-xs text-slate-300 leading-relaxed">
              3 в ширину × 3 у висоту × 5 у довжину. У центрі вздовж осі встановлюються 3×{' '}
              <strong className="text-slate-100">Ротори турбіни</strong>. Приймає гарячу пару через{' '}
              <strong className="text-cyan-300">Порт входу пари</strong> та видає FE через{' '}
              <strong className="text-amber-300">Порт виходу енергії</strong>.
            </p>
            <IsometricViewer structure={STRUCTURE_TURBINE} title="Структура турбіни 3×3×5" />
          </div>
        )}

        {tab === 'primitive_1x1' && (
          <div className="grid grid-cols-1 lg:grid-cols-2 gap-8 items-start">
            <div className="space-y-4">
              <h2 className="text-lg font-semibold text-amber-300">
                Одноблочний примітивний реактор (1×1)
              </h2>
              <p className="text-sm text-slate-300 leading-relaxed">
                Автономний стартовий реактор, якому не потрібні мультиблок, вода або парова турбіна.
              </p>
              <ul className="space-y-2 text-xs text-slate-300 list-disc pl-4">
                <li>
                  <strong className="text-cyan-300">Охолоджувач:</strong> заливається прямо в блок
                  (ПКМ відром Heavy Coolant або води, бак 8 000 mB).
                </li>
                <li>
                  <strong className="text-amber-300">Вихід енергії FE:</strong>{' '}
                  <u>ВИКЛЮЧНО ЗВЕРХУ блоку!</u> Поставте HV/MV конектор або конденсатор IE на верхню грань.
                </li>
                <li>
                  <strong className="text-emerald-300">Потужність:</strong> 512 / 1024 / 1536 FE/t.
                  Коли паливо вигорає — капсула стає Порожньою капсулою.
                </li>
              </ul>
            </div>

            {/* 3x3 Crafting Grid for Primitive Reactor */}
            <div className="bg-[#0B0E14] border border-slate-800 rounded-xl p-5 space-y-4">
              <div className="text-xs font-mono text-amber-400">
                Крафт одноблочного реактора (ienuclear:primitive_reactor):
              </div>
              <div className="flex items-center justify-center gap-6">
                <div className="grid grid-cols-3 gap-1.5 bg-[#1C212B] p-2.5 rounded border border-slate-700">
                  {[
                    'Свинцева пластина',
                    'MV Конектор IE',
                    'Свинцева пластина',
                    'Сталева пластина',
                    'Стрижень ядра',
                    'Сталева пластина',
                    'Свинцева пластина',
                    'Відро (Bucket)',
                    'Свинцева пластина',
                  ].map((item, idx) => (
                    <div
                      key={idx}
                      className="w-20 h-16 bg-[#0F131A] border border-slate-700 rounded flex items-center justify-center p-1 text-center text-[10px] font-mono text-slate-200"
                    >
                      {item}
                    </div>
                  ))}
                </div>
                <span className="text-xl text-amber-400 font-bold">→</span>
                <div className="w-24 h-20 bg-[#121722] border-2 border-amber-500/60 rounded-lg flex flex-col items-center justify-center p-2 text-center">
                  <span className="text-xs font-semibold text-amber-300">1×</span>
                  <span className="text-[11px] font-mono text-slate-100 mt-1">
                    Примітивний реактор
                  </span>
                </div>
              </div>
            </div>
          </div>
        )}

        {tab === 'ic2_grid' && (
          <div className="space-y-5">
            <div>
              <h2 className="text-lg font-semibold text-amber-300">
                IC2 Сітка завантаження (6×3), міцність капсул і пластини
              </h2>
              <p className="text-xs text-slate-300 mt-1 leading-relaxed">
                Усі паливні капсули тепер мають <strong className="text-emerald-300">шкалу міцності</strong>.
                Коли міцність капсули закінчується, вона автоматично перетворюється на{' '}
                <strong className="text-slate-100">Порожню паливну капсулу</strong> (`empty_fuel_rod`),
                яку можна знову заправити!
              </p>
            </div>

            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              {[
                {
                  name: 'Тепловідвід (heat_vent)',
                  craft: 'Залізні ґрати + Мідні пластини + Радіатор IE',
                  effect: 'Активно відводить -5 тепла/тік (-9 поруч із теплообмінником).',
                },
                {
                  name: 'Реакторна пластина (reactor_plating)',
                  craft: 'Свинцеві + Сталеві пластини в шаховому порядку',
                  effect: 'Пасивно поглинає тепло та знижує нагрів сусідніх капсул на -2.',
                },
                {
                  name: 'Компонентний теплообмінник (heat_exchanger)',
                  craft: 'Мідні пластини + Сталеві пластини + Електрум',
                  effect: 'Підсилює сусідні Тепловідводи з -5 до -9 тепла/тік.',
                },
                {
                  name: 'Нейтронний відбивач (neutron_reflector)',
                  craft: 'Мідні пластини + Коксовий пил + Електрум',
                  effect: 'Додає +35% потужності кожній суміжній паливній капсулі!',
                },
              ].map((c) => (
                <div
                  key={c.name}
                  className="bg-[#0B0E14] border border-slate-800 rounded-xl p-4 space-y-1.5"
                >
                  <div className="text-sm font-semibold text-amber-300">{c.name}</div>
                  <div className="text-xs text-slate-300">{c.effect}</div>
                  <div className="text-[11px] font-mono text-cyan-400">Крафт: {c.craft}</div>
                </div>
              ))}
            </div>
          </div>
        )}
      </div>
    </div>
  );
}
