import { useState } from 'react';
import { Check, Copy, FlaskConical } from 'lucide-react';
import { RESOURCE_AND_RECIPE_FILES } from '../modProject/resourcesAndRecipes';

interface RecipeStage {
  id: string;
  step: string;
  machine: string;
  recipeFile: string;
  inputs: string;
  outputs: string;
  energy: string;
  note: string;
}

const ENRICHMENT_STAGES: RecipeStage[] = [
  {
    id: 'crusher',
    step: '01',
    machine: 'Дробарка (IE Crusher)',
    recipeFile: 'ie_crusher_yellowcake.json',
    inputs: 'Сирий уран (#c:raw_materials/uranium)',
    outputs: '2× Урановий концентрат (yellowcake_uranium) + 35% Пил урану IE',
    energy: '4 800 FE',
    note: 'Первинне подрібнення руди або сирого урану з високим виходом концентрату.',
  },
  {
    id: 'mixer_uf6',
    step: '02',
    machine: 'Змішувач (IE Mixer)',
    recipeFile: 'ie_mixer_uranium_hexafluoride.json',
    inputs: '1 000 mB Води + 2× Yellowcake + 2× Редстоун',
    outputs: '1 000 mB Гексафторид урану (ienuclear:uranium_hexafluoride)',
    energy: '6 400 FE',
    note: 'Переведення сухого концентрату в рідку фазу для подальшого розділення ізотопів.',
  },
  {
    id: 'refinery',
    step: '03',
    machine: 'Нафтопереробний завод (IE Refinery)',
    recipeFile: 'ie_refinery_enrich_uranium.json',
    inputs: '10 mB UF6 + 5 mB Етанол (#c:ethanol)',
    outputs: '10 mB Збагачений розчин U-235 (ienuclear:enriched_uranium_solution)',
    energy: '160 FE/t',
    note: 'Фракційне збагачення ізотопу U-235 у промисловому ректифікаторі IE.',
  },
  {
    id: 'mixer_coolant',
    step: '04',
    machine: 'Змішувач (IE Mixer — Охолоджувач)',
    recipeFile: 'ie_mixer_heavy_coolant.json',
    inputs: '1 000 mB Води + 2× Лазурит + 2× Селітра IE (#c:dusts/saltpeter)',
    outputs: '1 000 mB Важкий охолоджувач (ienuclear:heavy_coolant)',
    energy: '3 200 FE',
    note: 'Спеціальна охолоджувальна рідина з у 2.5 рази вищою теплоємністю за воду.',
  },
  {
    id: 'press_rod',
    step: '05',
    machine: 'Металопрес (IE Metal Press — Капсула)',
    recipeFile: 'ie_metal_press_empty_rod.json',
    inputs: '2× Сталева пластина (#c:plates/steel) + Форма стрижня (mold_rod)',
    outputs: '1× Порожня паливна капсула (empty_fuel_rod)',
    energy: '3 600 FE',
    note: 'Штампування герметичної свинцево-сталевої капсули на пресі Immersive Engineering.',
  },
  {
    id: 'press_pellet',
    step: '06',
    machine: 'Металопрес (IE Metal Press — Таблетки)',
    recipeFile: 'ie_metal_press_uranium_pellet.json',
    inputs: '1× Злиток урану (#c:ingots/uranium) + Форма гільзи (mold_bullet_casing)',
    outputs: '2× Паливна таблетка (enriched_uranium_pellet)',
    energy: '4 200 FE',
    note: 'Пресування паливних таблеток для спорядження стандартних та збагачених капсул.',
  },
  {
    id: 'craft_standard',
    step: '07',
    machine: 'Верстак (Стандартна та збагачена капсула)',
    recipeFile: 'crafting_enriched_fuel_rod.json',
    inputs: 'Порожня капсула + 4× Таблетки + Відро U-235 + Свинцеві пластини',
    outputs: 'Збагачена паливна капсула (2x FE/t, 4 800 тактів)',
    energy: 'Крафт',
    note: 'Фінальна збірка паливної капсули високої енергоємності (до 6 144 FE/t).',
  },
];

export function IERecipesView() {
  const [selectedStageId, setSelectedStageId] = useState<string>('crusher');
  const [copied, setCopied] = useState(false);
  const [imgError, setImgError] = useState(false);

  const selectedStage = ENRICHMENT_STAGES.find((s) => s.id === selectedStageId) ?? ENRICHMENT_STAGES[0];
  const recipeEntry = RESOURCE_AND_RECIPE_FILES.find((f) =>
    f.path.endsWith(`/${selectedStage.recipeFile}`)
  );

  const handleCopy = () => {
    if (!recipeEntry) return;
    navigator.clipboard.writeText(recipeEntry.content);
    setCopied(true);
    setTimeout(() => setCopied(false), 1800);
  };

  return (
    <div className="space-y-8">
      <div className="pb-6 border-b border-slate-800">
        <p className="text-xs font-mono text-slate-400 mb-2">
          Виробничий цикл · Crusher → Mixer → Refinery → Metal Press
        </p>
        <h1 className="text-2xl sm:text-3xl font-semibold tracking-tight text-slate-100 max-w-2xl">
          Збагачення урану, паливні стрижні та охолоджувач на станках Immersive Engineering
        </h1>
      </div>

      {/* Top Feature Split: Studio Render + Coolant Highlight */}
      <div className="grid grid-cols-1 lg:grid-cols-12 gap-8 items-stretch">
        <div className="lg:col-span-5 bg-[#121722] border border-slate-800 rounded-xl overflow-hidden flex flex-col">
          <div className="relative flex-1 min-h-[240px] bg-[#0B0E14]">
            {!imgError ? (
              <img
                src="/src/assets/images/uranium_enrichment_rods_1795384363495.jpg"
                alt="Ядерні паливні стрижні зі сталевою оболонкою, урановими таблетками та баком охолоджувача"
                referrerPolicy="no-referrer"
                onError={() => setImgError(true)}
                className="w-full h-full object-cover"
              />
            ) : (
              <div className="w-full h-full flex flex-col items-center justify-center p-8 text-center">
                <FlaskConical className="w-10 h-10 text-emerald-400 mb-3" />
                <p className="text-sm text-slate-300">Паливні стрижні U-235 та охолоджувач</p>
              </div>
            )}
          </div>
          <div className="p-5 border-t border-slate-800 space-y-2">
            <h2 className="text-sm font-semibold text-slate-100">
              Важкий боро-водний охолоджувач (ienuclear:heavy_coolant)
            </h2>
            <p className="text-xs text-slate-400 leading-relaxed">
              Зареєстрований як повноцінна рідина NeoForge (FluidType + Flowing + Bucket + Block).
              Готується в <span className="text-slate-200">IE Mixer</span> з води, лазуриту та селітри;
              подається в реактор через <span className="text-slate-200">IE Fluid Pipes</span>.
            </p>
          </div>
        </div>

        {/* Right: Selected Recipe JSON Inspector */}
        <div className="lg:col-span-7 bg-[#121722] border border-slate-800 rounded-xl p-6 flex flex-col justify-between space-y-4">
          <div className="flex flex-wrap items-start justify-between gap-4">
            <div>
              <div className="text-xs font-mono text-amber-400 mb-1">
                Етап {selectedStage.step} · {selectedStage.machine}
              </div>
              <h2 className="text-base font-semibold text-slate-100">{selectedStage.outputs}</h2>
              <p className="text-xs text-slate-400 mt-1">{selectedStage.note}</p>
            </div>
            <button
              onClick={handleCopy}
              className="inline-flex items-center gap-2 px-3 py-1.5 text-xs font-medium bg-[#0B0E14] border border-slate-700 rounded-lg text-slate-200 hover:border-slate-500 transition-colors whitespace-nowrap shrink-0"
            >
              {copied ? <Check className="w-3.5 h-3.5 text-emerald-400" /> : <Copy className="w-3.5 h-3.5" />}
              {copied ? 'Скопійовано' : 'Копіювати JSON'}
            </button>
          </div>

          <div className="grid grid-cols-1 sm:grid-cols-2 gap-3 text-xs bg-[#0B0E14] border border-slate-800 rounded-lg p-3.5">
            <div>
              <span className="text-slate-400 block mb-0.5">Вхідні ресурси:</span>
              <span className="font-mono text-slate-200">{selectedStage.inputs}</span>
            </div>
            <div>
              <span className="text-slate-400 block mb-0.5">Витрата енергії IE:</span>
              <span className="font-mono text-amber-400">{selectedStage.energy}</span>
            </div>
          </div>

          <div className="bg-[#0B0E14] border border-slate-800 rounded-lg overflow-hidden">
            <div className="px-4 py-2 border-b border-slate-800 text-xs font-mono text-slate-400">
              {recipeEntry?.path ?? selectedStage.recipeFile}
            </div>
            <pre className="p-4 text-xs font-mono text-slate-200 overflow-x-auto leading-relaxed">
              <code>{recipeEntry?.content ?? ''}</code>
            </pre>
          </div>
        </div>
      </div>

      {/* Interactive Recipe Pipeline Table */}
      <div className="bg-[#121722] border border-slate-800 rounded-xl overflow-hidden">
        <div className="px-6 py-4 border-b border-slate-800">
          <h2 className="text-base font-semibold text-slate-100">
            Усі технологічні рецепти (клікніть рядок для перегляду JSON)
          </h2>
        </div>
        <div className="divide-y divide-slate-800/80">
          {ENRICHMENT_STAGES.map((stage) => {
            const isActive = stage.id === selectedStageId;
            return (
              <button
                key={stage.id}
                onClick={() => setSelectedStageId(stage.id)}
                className={`w-full text-left px-6 py-4 grid grid-cols-1 md:grid-cols-12 gap-3 items-center transition-colors ${
                  isActive ? 'bg-amber-500/10' : 'hover:bg-slate-800/30'
                }`}
              >
                <div className="md:col-span-3 flex items-center gap-3">
                  <span className="font-mono text-xs text-slate-500">{stage.step}.</span>
                  <span
                    className={`text-sm font-medium ${
                      isActive ? 'text-amber-300' : 'text-slate-200'
                    }`}
                  >
                    {stage.machine}
                  </span>
                </div>
                <div className="md:col-span-4 text-xs font-mono text-slate-400">{stage.inputs}</div>
                <div className="md:col-span-4 text-xs font-mono text-emerald-300">
                  {stage.outputs}
                </div>
                <div className="md:col-span-1 text-right text-xs font-mono text-amber-400 tabular-nums">
                  {stage.energy}
                </div>
              </button>
            );
          })}
        </div>
      </div>
    </div>
  );
}
