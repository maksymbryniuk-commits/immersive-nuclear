import { useMemo, useState } from 'react';
import { BookOpen, ChevronLeft, ChevronRight, Layers, RotateCw } from 'lucide-react';
import { getTextureById } from '../modProject/pixelTextures';

interface BlockMeta {
  id: string;
  name: string;
  short: string;
  textureId: string;
  count: number;
  role: string;
  capability?: string;
}

const BLOCK_CATALOG: Record<string, BlockMeta> = {
  casing: {
    id: 'ienuclear:reactor_casing',
    name: 'Корпус реактора',
    short: 'RC',
    textureId: 'reactor_casing',
    count: 20,
    role: 'Свинцево-сталевий радіаційний захист зовнішніх стінок 3×3×3',
  },
  glass: {
    id: 'ienuclear:reactor_glass',
    name: 'Радіаційно-стійке скло',
    short: 'GL',
    textureId: 'reactor_glass',
    count: 2,
    role: 'Оглядовий блок на бічній та верхній гранях для візуального контролю активної зони',
  },
  core: {
    id: 'ienuclear:reactor_core_rod',
    name: 'Стрижень активної зони',
    short: 'CORE',
    textureId: 'reactor_core_rod',
    count: 1,
    role: 'Центральний блок активної зони (координата [1, 1, 1]), де відбувається реакція поділу',
  },
  controller: {
    id: 'ienuclear:reactor_controller',
    name: 'Контролер реактора',
    short: 'CTRL',
    textureId: 'reactor_controller_front_active',
    count: 1,
    role: 'Лицьова панель управління: сканує мультиблок 3×3×3, відкриває GUI, керує запуском і SCRAM',
  },
  coolant: {
    id: 'ienuclear:coolant_input_port',
    name: 'Порт входу охолоджувача',
    short: 'IN',
    textureId: 'coolant_input_port',
    count: 1,
    role: 'Приймає охолоджувальну рідину (16 000 mB) із труб Immersive Engineering Fluid Pipe або відром',
    capability: 'Capabilities.FluidHandler.BLOCK',
  },
  energy: {
    id: 'ienuclear:energy_output_port',
    name: 'Порт виходу енергії (FE)',
    short: 'FE',
    textureId: 'energy_output_port',
    count: 1,
    role: 'Акумулює до 1 000 000 FE та автоматично передає до 16 384 FE/t у HV/MV конектори IE',
    capability: 'Capabilities.EnergyStorage.BLOCK',
  },
};

type BlockKey = keyof typeof BLOCK_CATALOG;

// Exact 3x3x3 structure matching NuclearReactorMultiblock.java
const STRUCTURE_3D: BlockKey[][][] = [
  // Y = 0 (Bottom)
  [
    ['casing', 'casing', 'casing'],
    ['casing', 'casing', 'casing'],
    ['casing', 'casing', 'casing'],
  ],
  // Y = 1 (Middle)
  [
    ['casing', 'glass', 'casing'],
    ['coolant', 'core', 'energy'],
    ['casing', 'controller', 'casing'],
  ],
  // Y = 2 (Top)
  [
    ['casing', 'casing', 'casing'],
    ['casing', 'glass', 'casing'],
    ['casing', 'casing', 'casing'],
  ],
];

const LAYER_TITLES = [
  'Y = 0 · Нижній фундамент (3×3)',
  'Y = 1 · Активна зона та порти вводу/виводу',
  'Y = 2 · Верхній захист та оглядове скло',
];

export function MultiblockBlueprintView() {
  const [selectedLayer, setSelectedLayer] = useState<number>(1);
  const [showAllLayers, setShowAllLayers] = useState<boolean>(true);
  const [exploded, setExploded] = useState<boolean>(false);
  const [yawStep, setYawStep] = useState<number>(0); // 0..3 -> 0, 90, 180, 270 deg
  const [manualPage, setManualPage] = useState<1 | 2>(1);
  const [selectedBlockKey, setSelectedBlockKey] = useState<BlockKey>('controller');

  const selectedBlock = BLOCK_CATALOG[selectedBlockKey];

  const textures = useMemo(() => {
    const map: Record<string, string> = {};
    for (const [k, meta] of Object.entries(BLOCK_CATALOG)) {
      map[k] = getTextureById(meta.textureId)?.dataUrl ?? '';
    }
    return map;
  }, []);

  // Build isometric blocks sorted painter's algorithm back-to-front for current yawStep
  const isoBlocks = useMemo(() => {
    const blocks: {
      x: number;
      y: number;
      z: number;
      rx: number;
      rz: number;
      key: BlockKey;
      depth: number;
    }[] = [];

    for (let y = 0; y < 3; y++) {
      if (!showAllLayers && y !== selectedLayer) continue;
      for (let z = 0; z < 3; z++) {
        for (let x = 0; x < 3; x++) {
          const key = STRUCTURE_3D[y][z][x];
          // Rotate (x, z) around center (1, 1)
          const cx = x - 1;
          const cz = z - 1;
          let rx = cx;
          let rz = cz;
          if (yawStep === 1) {
            rx = -cz;
            rz = cx;
          } else if (yawStep === 2) {
            rx = -cx;
            rz = -cz;
          } else if (yawStep === 3) {
            rx = cz;
            rz = -cx;
          }
          const depth = rx + rz + y * 0.01;
          blocks.push({ x, y, z, rx: rx + 1, rz: rz + 1, key, depth });
        }
      }
    }

    return blocks.sort((a, b) => a.depth - b.depth || a.y - b.y);
  }, [showAllLayers, selectedLayer, yawStep]);

  const tileW = 32;
  const tileH = 16;
  const cubeH = 32;
  const spread = exploded ? 14 : 0;

  return (
    <div className="space-y-8">
      <div className="pb-6 border-b border-slate-800">
        <p className="text-xs font-mono text-slate-400 mb-2">
          Engineerʼs Manual (Immersive Engineering) · NuclearReactorMultiblock.java · 3×3×3
        </p>
        <h1 className="text-2xl sm:text-3xl font-semibold tracking-tight text-slate-100 max-w-2xl">
          Гайд Immersive Engineering та 3D-візуалізація мультиблоку
        </h1>
      </div>

      {/* Engineer's Manual Book Spread */}
      <div className="bg-[#121722] border border-slate-800 rounded-xl p-6">
        <div className="flex flex-wrap items-center justify-between gap-4 pb-4 mb-6 border-b border-slate-800">
          <div className="flex items-center gap-2.5 text-sm font-semibold text-amber-400">
            <BookOpen className="w-4 h-4" />
            <span>Engineerʼs Manual — Розділ «Immersive Nuclear»</span>
          </div>

          <div className="flex items-center gap-2">
            <button
              onClick={() => setManualPage(1)}
              disabled={manualPage === 1}
              className="p-1.5 rounded-lg border border-slate-700 text-slate-300 hover:text-white disabled:opacity-40"
              aria-label="Попередня сторінка"
            >
              <ChevronLeft className="w-4 h-4" />
            </button>
            <span className="text-xs font-mono text-slate-400 px-2 tabular-nums">
              Стор. {manualPage} / 2
            </span>
            <button
              onClick={() => setManualPage(2)}
              disabled={manualPage === 2}
              className="p-1.5 rounded-lg border border-slate-700 text-slate-300 hover:text-white disabled:opacity-40"
              aria-label="Наступна сторінка"
            >
              <ChevronRight className="w-4 h-4" />
            </button>
          </div>
        </div>

        {manualPage === 1 ? (
          <div className="grid grid-cols-1 lg:grid-cols-12 gap-8 items-start">
            {/* Left Book Page: Live Interactive 3D Texture-Mapped Multiblock */}
            <div className="lg:col-span-6 bg-[#1C1915] border border-[#3B3328] rounded-xl p-5 space-y-4">
              <div className="flex flex-wrap items-center justify-between gap-2">
                <span className="text-xs font-mono text-amber-300">
                  &lt;&amp;nuclear_reactor_mb&gt; · 3D-проекція
                </span>
                <div className="flex items-center gap-2">
                  <button
                    onClick={() => setYawStep((s) => (s + 1) % 4)}
                    className="inline-flex items-center gap-1.5 px-2.5 py-1 text-xs font-mono bg-[#2A241C] border border-[#473D2F] rounded text-amber-200 hover:border-amber-500/60 transition-colors whitespace-nowrap"
                  >
                    <RotateCw className="w-3.5 h-3.5" />
                    Повернути ({yawStep * 90}°)
                  </button>
                  <button
                    onClick={() => setExploded((e) => !e)}
                    className={`px-2.5 py-1 text-xs font-mono rounded border transition-colors whitespace-nowrap ${
                      exploded
                        ? 'bg-amber-500/20 border-amber-500 text-amber-200'
                        : 'bg-[#2A241C] border-[#473D2F] text-slate-300'
                    }`}
                  >
                    {exploded ? 'Зібрати' : 'Рознести'}
                  </button>
                </div>
              </div>

              {/* Interactive Isometric 3D Canvas */}
              <div className="bg-[#12100D] border border-[#2E271E] rounded-lg h-[310px] flex items-center justify-center overflow-hidden relative">
                <div
                  className="relative"
                  style={{
                    width: 280,
                    height: 260,
                  }}
                >
                  {isoBlocks.map((b) => {
                    const isoX = (b.rx - b.rz) * (tileW + spread * 0.45);
                    const isoY =
                      (b.rx + b.rz) * (tileH + spread * 0.22) -
                      b.y * (cubeH + spread * 0.9);
                    const left = 140 + isoX - tileW;
                    const top = 165 + isoY - cubeH;
                    const texUrl = textures[b.key];
                    const isSelected = b.key === selectedBlockKey;
                    const isGlass = b.key === 'glass';

                    return (
                      <button
                        key={`${b.x}-${b.y}-${b.z}`}
                        onClick={() => setSelectedBlockKey(b.key)}
                        title={`${BLOCK_CATALOG[b.key].name} [${b.x}, ${b.y}, ${b.z}]`}
                        className="absolute transition-all duration-200 group focus:outline-none"
                        style={{
                          left: `${left}px`,
                          top: `${top}px`,
                          width: `${tileW * 2}px`,
                          height: `${cubeH + tileH * 2}px`,
                          opacity: isGlass ? 0.78 : 1,
                        }}
                      >
                        {/* Top Face */}
                        <div
                          className="absolute overflow-hidden"
                          style={{
                            width: `${tileW}px`,
                            height: `${tileW}px`,
                            left: `${tileW}px`,
                            top: `${tileH}px`,
                            transformOrigin: '0 0',
                            transform: 'rotate(210deg) skewX(-30deg) scaleY(0.864)',
                            backgroundImage: `url(${texUrl})`,
                            backgroundSize: '100% 100%',
                            imageRendering: 'pixelated',
                            filter: isSelected ? 'brightness(1.35)' : 'brightness(1.08)',
                            boxShadow: isSelected ? 'inset 0 0 0 2px #F59E0B' : 'inset 0 0 0 1px rgba(0,0,0,0.25)',
                          }}
                        />
                        {/* Left Face */}
                        <div
                          className="absolute overflow-hidden"
                          style={{
                            width: `${tileW}px`,
                            height: `${cubeH}px`,
                            left: `0px`,
                            top: `${tileH}px`,
                            transformOrigin: '0 0',
                            transform: 'skewY(30deg) scaleY(1)',
                            backgroundImage: `url(${texUrl})`,
                            backgroundSize: '100% 100%',
                            imageRendering: 'pixelated',
                            filter: isSelected ? 'brightness(1.25)' : 'brightness(0.85)',
                            boxShadow: isSelected ? 'inset 0 0 0 2px #F59E0B' : 'inset 0 0 0 1px rgba(0,0,0,0.35)',
                          }}
                        />
                        {/* Right Face */}
                        <div
                          className="absolute overflow-hidden"
                          style={{
                            width: `${tileW}px`,
                            height: `${cubeH}px`,
                            left: `${tileW}px`,
                            top: `${tileH * 2}px`,
                            transformOrigin: '0 0',
                            transform: 'skewY(-30deg) scaleY(1)',
                            backgroundImage: `url(${texUrl})`,
                            backgroundSize: '100% 100%',
                            imageRendering: 'pixelated',
                            filter: isSelected ? 'brightness(1.15)' : 'brightness(0.68)',
                            boxShadow: isSelected ? 'inset 0 0 0 2px #F59E0B' : 'inset 0 0 0 1px rgba(0,0,0,0.4)',
                          }}
                        />
                      </button>
                    );
                  })}
                </div>
              </div>

              {/* Layer Filter Controls */}
              <div className="flex flex-wrap items-center justify-between gap-2">
                <button
                  onClick={() => setShowAllLayers((v) => !v)}
                  className={`inline-flex items-center gap-1.5 px-3 py-1.5 text-xs font-mono rounded border transition-colors whitespace-nowrap ${
                    showAllLayers
                      ? 'bg-amber-500 text-slate-950 border-amber-400 font-semibold'
                      : 'bg-[#2A241C] text-slate-300 border-[#473D2F]'
                  }`}
                >
                  <Layers className="w-3.5 h-3.5" />
                  Усі шари
                </button>

                <div className="flex items-center gap-1">
                  {[0, 1, 2].map((lvl) => (
                    <button
                      key={lvl}
                      onClick={() => {
                        setShowAllLayers(false);
                        setSelectedLayer(lvl);
                      }}
                      className={`px-3 py-1.5 text-xs font-mono rounded border transition-colors whitespace-nowrap ${
                        !showAllLayers && selectedLayer === lvl
                          ? 'bg-amber-500 text-slate-950 border-amber-400 font-semibold'
                          : 'bg-[#2A241C] text-slate-300 border-[#473D2F] hover:text-white'
                      }`}
                    >
                      Y = {lvl}
                    </button>
                  ))}
                </div>
              </div>
            </div>

            {/* Right Book Page: Ukrainian Manual Text + Bill of Materials */}
            <div className="lg:col-span-6 space-y-5">
              <div>
                <h2 className="text-xl font-semibold text-amber-300 mb-1">
                  Ядерний реактор (3×3×3)
                </h2>
                <p className="text-xs font-mono text-slate-400">
                  Сторінка довідника: assets/ienuclear/manual/uk_ua/nuclear_reactor.txt
                </p>
              </div>

              <p className="text-sm text-slate-300 leading-relaxed">
                Мультиблоковий ядерний реактор перетворює енергію поділу урану в стабільний потік
                Forge Energy (FE), повністю сумісний із високовольтними конекторами (HV/MV) та
                рідинними трубами Immersive Engineering.
              </p>

              {/* Materials List with 16x16 icons */}
              <div className="bg-[#0B0E14] border border-slate-800 rounded-xl p-4 space-y-3">
                <div className="text-xs font-mono text-slate-400">
                  Необхідні блоки (getTotalMaterials):
                </div>
                <div className="grid grid-cols-1 sm:grid-cols-2 gap-2.5">
                  {(Object.entries(BLOCK_CATALOG) as [BlockKey, BlockMeta][]).map(([key, meta]) => {
                    const isSelected = selectedBlockKey === key;
                    return (
                      <button
                        key={key}
                        onClick={() => setSelectedBlockKey(key)}
                        className={`flex items-center gap-3 p-2.5 rounded-lg border text-left transition-colors ${
                          isSelected
                            ? 'bg-amber-500/10 border-amber-500/60'
                            : 'bg-[#121722] border-slate-800 hover:border-slate-700'
                        }`}
                      >
                        <img
                          src={textures[key]}
                          alt={meta.name}
                          className="w-8 h-8 shrink-0 rounded-sm bg-slate-950"
                          style={{ imageRendering: 'pixelated' }}
                        />
                        <div className="min-w-0 flex-1">
                          <div className="text-xs font-medium text-slate-100 truncate">
                            {meta.count}× {meta.name}
                          </div>
                          <div className="text-[10px] font-mono text-slate-400 truncate">
                            {meta.id}
                          </div>
                        </div>
                      </button>
                    );
                  })}
                </div>
              </div>

              {/* Selected Block Info */}
              <div className="bg-[#0B0E14] border border-slate-800 rounded-lg p-4 space-y-1.5">
                <div className="flex items-center justify-between gap-2">
                  <span className="text-sm font-semibold text-slate-100">{selectedBlock.name}</span>
                  <span className="text-xs font-mono text-amber-400">{selectedBlock.id}</span>
                </div>
                <p className="text-xs text-slate-300 leading-relaxed">{selectedBlock.role}</p>
                {selectedBlock.capability && (
                  <p className="text-xs font-mono text-cyan-400">
                    Capability: {selectedBlock.capability}
                  </p>
                )}
              </div>
            </div>
          </div>
        ) : (
          /* Page 2 of Engineer's Manual */
          <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
            <div className="space-y-4">
              <h2 className="text-xl font-semibold text-amber-300">
                Збагачення урану та охолоджувач
              </h2>
              <p className="text-xs font-mono text-slate-400">
                Сторінка довідника: assets/ienuclear/manual/uk_ua/uranium_enrichment.txt
              </p>
              <div className="space-y-3 text-sm text-slate-300 leading-relaxed">
                <p>
                  <strong className="text-slate-100">1. Дробарка (Crusher):</strong> подрібніть сирий
                  уран у <span className="font-mono text-amber-300">yellowcake_uranium</span>.
                </p>
                <p>
                  <strong className="text-slate-100">2. Змішувач (Mixer):</strong> розчиніть
                  Yellowcake у воді з редстоуном для отримання гексафториду урану (UF6).
                </p>
                <p>
                  <strong className="text-slate-100">3. Рафінер (Refinery):</strong> збагатіть UF6
                  етанолом у збагачений розчин U-235.
                </p>
                <p>
                  <strong className="text-slate-100">4. Металопрес (Metal Press):</strong>{' '}
                  відштампуйте свинцево-сталеві пластини (mold_rod) у порожню герметичну капсулу та злитки урану в
                  паливні таблетки.
                </p>
                <p>
                  <strong className="text-slate-100">5. Важкий охолоджувач:</strong> змішайте в IE
                  Mixer воду, лазурит та селітру (Saltpeter) — ефективність охолодження у 2.5 рази
                  вища за воду. УВАГА: без охолоджувача реактор входить у тепловий розгін і при 1400°C вибухає (Meltdown)!
                </p>
              </div>
            </div>

            {/* 2D Layer Slice Reference */}
            <div className="bg-[#0B0E14] border border-slate-800 rounded-xl p-5 space-y-4">
              <div className="flex items-center justify-between gap-2">
                <span className="text-xs font-mono text-slate-400">
                  {LAYER_TITLES[selectedLayer]}
                </span>
                <div className="flex gap-1">
                  {[0, 1, 2].map((lvl) => (
                    <button
                      key={lvl}
                      onClick={() => setSelectedLayer(lvl)}
                      className={`px-2.5 py-1 text-xs font-mono rounded ${
                        selectedLayer === lvl
                          ? 'bg-amber-500 text-slate-950 font-semibold'
                          : 'bg-[#121722] text-slate-400'
                      }`}
                    >
                      Y={lvl}
                    </button>
                  ))}
                </div>
              </div>

              <div className="grid grid-cols-3 gap-2 max-w-[230px] mx-auto">
                {STRUCTURE_3D[selectedLayer].flat().map((key, idx) => (
                  <button
                    key={`${selectedLayer}-${idx}`}
                    onClick={() => setSelectedBlockKey(key)}
                    className={`aspect-square p-2 rounded-lg border flex flex-col items-center justify-center gap-1 ${
                      selectedBlockKey === key
                        ? 'border-amber-400 bg-amber-500/15'
                        : 'border-slate-700 bg-[#121722]'
                    }`}
                  >
                    <img
                      src={textures[key]}
                      alt={BLOCK_CATALOG[key].name}
                      className="w-8 h-8"
                      style={{ imageRendering: 'pixelated' }}
                    />
                    <span className="text-[10px] font-mono text-slate-300">
                      {BLOCK_CATALOG[key].short}
                    </span>
                  </button>
                ))}
              </div>
            </div>
          </div>
        )}
      </div>
    </div>
  );
}
