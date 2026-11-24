import { useEffect, useState } from 'react';
import {
  AlertTriangle,
  Droplets,
  Flame,
  Gauge,
  Layers,
  Radiation,
  RotateCcw,
  ShieldAlert,
  Zap,
} from 'lucide-react';
import { getTextureById } from '../modProject/pixelTextures';
import { RAW_IE_TEXTURES } from '../modProject/rawIETextures';

type FuelType = 'none' | 'standard' | 'enriched';
type CoolantType = 'heavy_coolant' | 'water';

interface TelemetryPoint {
  t: number;
  temp: number;
  fePerTick: number;
  coolant: number;
}

const AMBIENT_TEMP = 20;
const OPTIMAL_TEMP = 750;
const WARNING_TEMP = 1000;
const SCRAM_TEMP = 1150;
const MELTDOWN_TEMP = 1400;
const MAX_TEMP = 1600;

const MAX_COOLANT = 16000;
const MAX_ENERGY = 1_000_000;

export function ReactorSimulatorView() {
  const [multiblockValid, setMultiblockValid] = useState(true);
  const [active, setActive] = useState(false);
  const [temperature, setTemperature] = useState(20);
  const [coolantMb, setCoolantMb] = useState(12000);
  const [coolantType, setCoolantType] = useState<CoolantType>('heavy_coolant');
  const [energyFe, setEnergyFe] = useState(185000);
  const [fePerTick, setFePerTick] = useState(0);
  const [fuelSlot, setFuelSlot] = useState<FuelType>('enriched');
  const [fuelTicks, setFuelTicks] = useState(4800);
  const [maxFuelTicks, setMaxFuelTicks] = useState(4800);
  const [depletedCount, setDepletedCount] = useState(0);
  const [pumpConnected, setPumpConnected] = useState(true);
  const [hvLoadEnabled, setHvLoadEnabled] = useState(true);
  const [scramTripped, setScramTripped] = useState(false);
  const [isMeltdown, setIsMeltdown] = useState(false);
  const [meltdownShock, setMeltdownShock] = useState(false);

  const [history, setHistory] = useState<TelemetryPoint[]>(() =>
    Array.from({ length: 45 }, (_, i) => ({
      t: i,
      temp: 20,
      fePerTick: 0,
      coolant: 12000,
    }))
  );

  // Realistic Simulation loop matching ReactorControllerBlockEntity.serverTick
  useEffect(() => {
    if (isMeltdown) return; // Reactor is blown up, physics halted until rebuilt

    const interval = setInterval(() => {
      // 1. Influx of coolant via IE Fluid Pump
      setCoolantMb((prevCoolant) => {
        if (!pumpConnected) return prevCoolant;
        return Math.min(MAX_COOLANT, prevCoolant + 120);
      });

      // 2. Main thermal & nuclear reactor physics
      setTemperature((prevTemp) => {
        let nextTemp = prevTemp;
        let nextFeRate = 0;

        if (active && multiblockValid && fuelSlot !== 'none' && fuelTicks > 0) {
          const multiplier = fuelSlot === 'enriched' ? 2 : 1;
          const hasCoolant = coolantMb > 0;

          if (hasCoolant) {
            // Coolant is flowing: controlled reaction
            const isHeavy = coolantType === 'heavy_coolant';
            const coolantDrain = isHeavy ? 16 : 40;
            const coolingPower = isHeavy ? 18 : 7;
            const normalHeat = multiplier === 2 ? 14 : 9;

            setCoolantMb((c) => Math.max(0, c - coolantDrain));

            // Stabilizes around optimal zone (650-800°C)
            if (nextTemp < OPTIMAL_TEMP) {
              nextTemp = Math.min(OPTIMAL_TEMP, nextTemp + normalHeat);
            } else {
              nextTemp = Math.max(AMBIENT_TEMP + 60, nextTemp + normalHeat - coolingPower);
            }

            // Normal SCRAM trip if coolant is present but threshold exceeded
            if (nextTemp >= SCRAM_TEMP) {
              setActive(false);
              setScramTripped(true);
              nextFeRate = 0;
            } else {
              const baseFe = 2048 * multiplier;
              const tempEff = Math.min(1.6, 0.5 + nextTemp / 650.0);
              nextFeRate = Math.round(baseFe * tempEff);
            }
          } else {
            // === РЕАЛІСТИЧНИЙ ТЕПЛОВИЙ РОЗГІН: БЕЗ ОХОЛОДЖУВАЧА РЕАКТОР СТРІМКО НАБИРАЄ ТЕМПЕРАТУРУ! ===
            const runawayGain = multiplier === 2 ? 45 : 30; // +30°C to +45°C per tick step!
            nextTemp = Math.min(MAX_TEMP, nextTemp + runawayGain);

            // In runaway mode, energy generation spikes dangerously before the core melts
            const baseFe = 2048 * multiplier;
            nextFeRate = Math.round(baseFe * 1.8);

            // CATASTROPHIC NUCLEAR MELTDOWN AT 1400°C!
            if (nextTemp >= MELTDOWN_TEMP) {
              setIsMeltdown(true);
              setActive(false);
              setMeltdownShock(true);
              setTimeout(() => setMeltdownShock(false), 2000);
              return MAX_TEMP;
            }
          }

          // Consume fuel capsule ticks
          setFuelTicks((prevTicks) => {
            const nextTicks = prevTicks - 16;
            if (nextTicks <= 0) {
              setDepletedCount((d) => d + 1);
              setFuelSlot('none');
              setActive(false);
              return 0;
            }
            return nextTicks;
          });

          // Energy buffer insertion / extraction
          setEnergyFe((prevFe) => {
            const inserted = Math.min(MAX_ENERGY, prevFe + nextFeRate * 4);
            if (hvLoadEnabled) {
              return Math.max(0, inserted - 3500);
            }
            return inserted;
          });
        } else {
          // Reactor Idle: natural passive cooling down to ambient
          nextFeRate = 0;
          if (nextTemp > AMBIENT_TEMP) {
            nextTemp = Math.max(AMBIENT_TEMP, nextTemp - 8);
          }
          if (hvLoadEnabled) {
            setEnergyFe((prevFe) => Math.max(0, prevFe - 1800));
          }
        }

        setFePerTick(nextFeRate);
        setHistory((prev) => [
          ...prev.slice(1),
          {
            t: prev[prev.length - 1].t + 1,
            temp: nextTemp,
            fePerTick: nextFeRate,
            coolant: coolantMb,
          },
        ]);

        return nextTemp;
      });
    }, 250);

    return () => clearInterval(interval);
  }, [
    active,
    multiblockValid,
    fuelSlot,
    fuelTicks,
    coolantMb,
    coolantType,
    pumpConnected,
    hvLoadEnabled,
    isMeltdown,
  ]);

  const handleToggleKnifeSwitch = () => {
    if (isMeltdown) return;
    if (!multiblockValid) return;
    if (!active && fuelSlot === 'none') return;
    setScramTripped(false);
    setActive((v) => !v);
  };

  const loadFuelCapsule = (type: Exclude<FuelType, 'none'>) => {
    if (isMeltdown) return;
    setFuelSlot(type);
    const ticks = type === 'enriched' ? 4800 : 2400;
    setFuelTicks(ticks);
    setMaxFuelTicks(ticks);
    setScramTripped(false);
  };

  const rebuildAndResetReactor = () => {
    setIsMeltdown(false);
    setMeltdownShock(false);
    setActive(false);
    setTemperature(AMBIENT_TEMP);
    setCoolantMb(12000);
    setEnergyFe(185000);
    setFePerTick(0);
    setFuelSlot('enriched');
    setFuelTicks(4800);
    setMaxFuelTicks(4800);
    setDepletedCount(0);
    setScramTripped(false);
    setMultiblockValid(true);
    setHistory(
      Array.from({ length: 45 }, (_, i) => ({
        t: i,
        temp: 20,
        fePerTick: 0,
        coolant: 12000,
      }))
    );
  };

  const tempPercent = Math.min(100, Math.max(0, Math.round((temperature / MELTDOWN_TEMP) * 100)));
  const coolantPercent = Math.min(100, Math.round((coolantMb / MAX_COOLANT) * 100));
  const energyPercent = Math.min(100, Math.round((energyFe / MAX_ENERGY) * 100));
  const fuelProgress =
    maxFuelTicks > 0 && fuelSlot !== 'none'
      ? Math.round(((maxFuelTicks - fuelTicks) / maxFuelTicks) * 100)
      : 0;

  // Temperature warning badge & styling
  const tempStatus =
    temperature >= 1150
      ? {
          label: 'АВАРІЙНИЙ РОЗГІН! ЗАГРОЗА ВИБУХУ',
          color: 'text-rose-400',
          border: 'border-rose-500',
          bg: 'bg-rose-500/20',
          fill: '#EF4444',
        }
      : temperature >= 900
        ? {
            label: 'Перегрів активної зони',
            color: 'text-orange-400',
            border: 'border-orange-500',
            bg: 'bg-orange-500/20',
            fill: '#F97316',
          }
        : temperature >= 550
          ? {
              label: 'Оптимальний робочий режим',
              color: 'text-amber-300',
              border: 'border-amber-500',
              bg: 'bg-amber-500/15',
              fill: '#F59E0B',
            }
          : {
              label: 'Номінальна температура',
              color: 'text-emerald-400',
              border: 'border-emerald-500',
              bg: 'bg-emerald-500/15',
              fill: '#10B981',
            };

  // Telemetry chart paths
  const chartWidth = 620;
  const chartHeight = 150;
  const tempPath = history
    .map((pt, idx) => {
      const x = (idx / (history.length - 1)) * chartWidth;
      const y = chartHeight - (pt.temp / MELTDOWN_TEMP) * (chartHeight - 16) - 8;
      return `${idx === 0 ? 'M' : 'L'} ${x.toFixed(1)} ${y.toFixed(1)}`;
    })
    .join(' ');

  const fePath = history
    .map((pt, idx) => {
      const x = (idx / (history.length - 1)) * chartWidth;
      const y = chartHeight - (pt.fePerTick / 7000) * (chartHeight - 16) - 8;
      return `${idx === 0 ? 'M' : 'L'} ${x.toFixed(1)} ${y.toFixed(1)}`;
    })
    .join(' ');

  const emptyRodTex = getTextureById('empty_fuel_rod')?.dataUrl;
  const standardCapsuleTex = getTextureById('nuclear_fuel_rod')?.dataUrl;
  const enrichedCapsuleTex = getTextureById('enriched_nuclear_fuel_rod')?.dataUrl;
  const depletedCapsuleTex = getTextureById('depleted_fuel_rod')?.dataUrl;
  const coolantBucketTex = getTextureById('coolant_bucket')?.dataUrl;
  const leadSheetTex = RAW_IE_TEXTURES.sheetmetal_lead;
  const steelSheetTex = RAW_IE_TEXTURES.sheetmetal_steel;
  const breakerSwitchTex = RAW_IE_TEXTURES.breaker_switch;

  return (
    <div className={`space-y-8 ${meltdownShock ? 'animate-bounce' : ''}`}>
      {/* Meltdown Disaster Modal Banner if exploded */}
      {isMeltdown && (
        <div className="relative overflow-hidden rounded-xl border-2 border-red-600 bg-[#160B0B] p-6 shadow-2xl">
          <div className="absolute -right-8 -top-8 h-40 w-40 rounded-full bg-red-600/20 blur-3xl" />
          <div className="flex flex-col md:flex-row items-center justify-between gap-6 relative z-10">
            <div className="flex items-center gap-4">
              <div className="flex h-14 w-14 shrink-0 items-center justify-center rounded-lg bg-red-600/30 border border-red-500 text-red-400">
                <Radiation className="h-8 w-8 animate-spin" style={{ animationDuration: '4s' }} />
              </div>
              <div>
                <div className="flex items-center gap-2">
                  <span className="font-mono text-xs font-bold uppercase tracking-wider text-red-400">
                    Аварійна тривога 4-го рівня
                  </span>
                  <span className="rounded bg-red-950 px-2 py-0.5 font-mono text-[11px] text-red-300">
                    1400°C MELTDOWN
                  </span>
                </div>
                <h2 className="text-xl font-bold text-white mt-1">
                  КАТАСТРОФІЧНИЙ ТЕПЛОВИЙ РОЗГІН ТА ВИБУХ РЕАКТОРА!
                </h2>
                <p className="text-sm text-red-200/80 mt-1 max-w-xl">
                  Через відсутність охолоджувальної рідини температура реактора сягнула 1400°C.
                  Мультиблок 3×3×3 знищено потужним ядерним вибухом силою 14.0F, утворився радіоактивний кратер!
                </p>
              </div>
            </div>

            <button
              onClick={rebuildAndResetReactor}
              className="inline-flex items-center gap-2 rounded-lg bg-gradient-to-r from-red-600 to-amber-600 px-5 py-3 font-mono text-sm font-bold text-white shadow-lg hover:from-red-500 hover:to-amber-500 transition-all shrink-0"
            >
              <RotateCcw className="h-4 w-4" />
              Ліквідувати наслідки та відновити реактор
            </button>
          </div>
        </div>
      )}

      {/* Top Header */}
      <div className="flex flex-col lg:flex-row lg:items-end justify-between gap-6 pb-6 border-b border-slate-800">
        <div>
          <div className="flex items-center gap-2 text-xs font-mono text-amber-500/90 mb-2">
            <span>NeoForge 1.21.1</span>
            <span>•</span>
            <span>Immersive Engineering Industrial Interface</span>
          </div>
          <h1 className="text-2xl sm:text-3xl font-semibold tracking-tight text-slate-100 max-w-2xl">
            Пульт керування та симулятор промислового ядерного реактора
          </h1>
        </div>
        <div className="flex flex-wrap items-center gap-3 shrink-0">
          <button
            onClick={rebuildAndResetReactor}
            className="inline-flex items-center gap-2 px-4 py-2 text-sm font-medium text-slate-300 bg-[#121722] border border-slate-800 rounded-lg hover:text-white hover:border-slate-700 transition-colors whitespace-nowrap"
          >
            <RotateCcw className="w-4 h-4" />
            Скинути стан
          </button>
        </div>
      </div>

      {/* Main Console: IE In-Game Screen Replica (Left) + Engineering Control Bench (Right) */}
      <div className="grid grid-cols-1 lg:grid-cols-12 gap-8 items-start">
        {/* Left: Authentic Immersive Engineering Heavy Steel & Brass Machine Interface */}
        <div className="lg:col-span-7 bg-[#151921] border-2 border-[#2F3642] rounded-xl p-5 shadow-2xl relative">
          {/* Machine Header with Brass Plate & Rivets */}
          <div className="flex items-center justify-between mb-4 pb-3 border-b border-[#303847]">
            <div className="flex items-center gap-3">
              <div
                className="w-5 h-5 rounded border border-[#6B5733] shadow"
                style={{
                  backgroundImage: `url(${breakerSwitchTex})`,
                  backgroundSize: 'cover',
                  imageRendering: 'pixelated',
                }}
              />
              <span className="font-mono text-xs sm:text-sm font-bold tracking-wide text-[#E4B059] uppercase drop-shadow">
                IMMERSIVE NUCLEAR • REACTOR CONTROLLER
              </span>
            </div>
            <div className="flex items-center gap-2">
              <span
                className={`font-mono text-xs font-semibold px-2 py-0.5 rounded border ${
                  isMeltdown
                    ? 'border-red-600 bg-red-950/60 text-red-400 animate-pulse'
                    : !multiblockValid
                      ? 'border-rose-500/60 bg-rose-950/40 text-rose-300'
                      : active
                        ? 'border-emerald-500/60 bg-emerald-950/40 text-emerald-300'
                        : 'border-slate-700 bg-slate-800 text-slate-400'
                }`}
              >
                {isMeltdown
                  ? 'MELTDOWN ВИБУХ'
                  : !multiblockValid
                    ? 'МУЛЬТИБЛОК ПОШКОДЖЕНО'
                    : active
                      ? 'РЕАКЦІЯ АКТИВНА'
                      : 'РЕАКТОР ГОТОВИЙ'}
              </span>
            </div>
          </div>

          {/* Riveted Industrial Bezel */}
          <div className="bg-[#1C212B] border-4 border-[#2D333F] rounded-lg p-5 shadow-inner relative">
            {/* Corner Decorative Brass Screws */}
            <div className="absolute top-1.5 left-1.5 w-2 h-2 rounded-full bg-[#B8860B] border border-[#7D5B08] shadow-sm" />
            <div className="absolute top-1.5 right-1.5 w-2 h-2 rounded-full bg-[#B8860B] border border-[#7D5B08] shadow-sm" />
            <div className="absolute bottom-1.5 left-1.5 w-2 h-2 rounded-full bg-[#B8860B] border border-[#7D5B08] shadow-sm" />
            <div className="absolute bottom-1.5 right-1.5 w-2 h-2 rounded-full bg-[#B8860B] border border-[#7D5B08] shadow-sm" />

            {/* In-Game Gauges & Center Control Core */}
            <div className="grid grid-cols-12 gap-3 sm:gap-4 items-stretch">
              {/* 1. Pyrometer / Temperature Column (IE Style with Meltdown Marker) */}
              <div className="col-span-3 sm:col-span-2 flex flex-col items-center">
                <span className="text-[11px] font-mono font-semibold text-slate-300 mb-1.5">
                  ТЕМП.
                </span>
                <div
                  className="w-8 h-48 bg-[#0D1017] border-2 border-[#384152] rounded-sm relative overflow-hidden flex items-end p-0.5 shadow-inner"
                  title={`Температура активної зони: ${temperature}°C / 1400°C (Meltdown)`}
                >
                  {/* Meltdown Danger Line (1400°C) */}
                  <div
                    className="absolute left-0 right-0 border-t-2 border-red-500 z-10"
                    style={{ bottom: '87.5%' }}
                    title="Поріг ядерного вибуху Meltdown (1400°C)"
                  />
                  {/* SCRAM Line (1150°C) */}
                  <div
                    className="absolute left-0 right-0 border-t border-dashed border-amber-400/80 z-10"
                    style={{ bottom: '71.8%' }}
                    title="Поріг аварійного SCRAM (1150°C)"
                  />
                  {/* Optimal Zone Range (550-900°C) */}
                  <div
                    className="absolute left-0 right-0 bg-emerald-500/10 border-y border-emerald-500/20"
                    style={{ bottom: '34.3%', height: '22%' }}
                    title="Оптимальний робочий діапазон"
                  />

                  {/* Temperature Liquid Column */}
                  <div
                    className="w-full transition-all duration-200"
                    style={{
                      height: `${tempPercent}%`,
                      backgroundColor: tempStatus.fill,
                    }}
                  />
                </div>
                <span className="mt-2 font-mono text-xs font-bold text-slate-100 tabular-nums">
                  {temperature}°C
                </span>
              </div>

              {/* 2. Borosilicate Sight Glass: Heavy Coolant / Water Tank */}
              <div className="col-span-3 sm:col-span-2 flex flex-col items-center">
                <span className="text-[11px] font-mono font-semibold text-slate-300 mb-1.5">
                  ОХОЛОДЖ.
                </span>
                <div
                  className="w-8 h-48 bg-[#0B1015] border-2 border-[#384152] rounded-sm relative overflow-hidden flex items-end p-0.5 shadow-inner"
                  title={`Рівень охолоджувача: ${coolantMb} / ${MAX_COOLANT} mB (${coolantType === 'heavy_coolant' ? 'Heavy Coolant' : 'Water'})`}
                >
                  {/* Glass Graduation Tick Marks */}
                  <div className="absolute inset-y-0 left-0 flex flex-col justify-between py-2 text-[9px] text-slate-500 font-mono pointer-events-none select-none pl-0.5">
                    <span>16k</span>
                    <span>12k</span>
                    <span>8k</span>
                    <span>4k</span>
                    <span>0k</span>
                  </div>

                  {/* Coolant Liquid Column */}
                  <div
                    className={`w-full transition-all duration-200 ${
                      coolantType === 'heavy_coolant'
                        ? 'bg-gradient-to-t from-cyan-600 via-cyan-400 to-sky-300 shadow-[0_0_8px_rgba(34,211,238,0.5)]'
                        : 'bg-gradient-to-t from-blue-700 via-blue-500 to-blue-300'
                    }`}
                    style={{ height: `${coolantPercent}%` }}
                  />
                </div>
                <span className="mt-2 font-mono text-[11px] text-cyan-300 tabular-nums">
                  {Math.round(coolantMb / 1000)}k mB
                </span>
              </div>

              {/* 3. Fuel Capsules Core Chamber & Immersive Engineering Knife Switch */}
              <div className="col-span-6 sm:col-span-6 flex flex-col justify-between px-1 sm:px-3">
                <div className="flex items-center justify-between gap-3">
                  {/* Capsule Slots Column */}
                  <div className="flex flex-col items-center gap-1.5">
                    <span className="text-[10px] font-mono font-semibold uppercase text-slate-400">
                      Капсула
                    </span>

                    {/* Fuel Input Capsule Slot */}
                    <div
                      className={`w-14 h-14 rounded border-2 flex flex-col items-center justify-center relative shadow-inner ${
                        fuelSlot !== 'none'
                          ? 'bg-[#151D29] border-[#4B5E78]'
                          : 'bg-[#12161E] border-[#2E3747]'
                      }`}
                      title="Вхідний гніздовий слот паливної капсули"
                    >
                      {fuelSlot === 'enriched' && (
                        <img
                          src={enrichedCapsuleTex}
                          alt="Високозбагачена капсула U-235"
                          className="w-10 h-10"
                          style={{ imageRendering: 'pixelated' }}
                        />
                      )}
                      {fuelSlot === 'standard' && (
                        <img
                          src={standardCapsuleTex}
                          alt="Паливна капсула U-238"
                          className="w-10 h-10"
                          style={{ imageRendering: 'pixelated' }}
                        />
                      )}
                      {fuelSlot === 'none' && (
                        <img
                          src={emptyRodTex}
                          alt="Порожнє гніздо"
                          className="w-8 h-8 opacity-25"
                          style={{ imageRendering: 'pixelated' }}
                        />
                      )}
                    </div>

                    {/* Burn Progress Arrow / Gauge */}
                    <div className="w-4 h-6 bg-[#0E1118] border border-[#374152] rounded overflow-hidden flex flex-col justify-start">
                      <div
                        className="w-full bg-emerald-500 transition-all duration-200"
                        style={{ height: `${fuelProgress}%` }}
                      />
                    </div>

                    {/* Depleted Capsule Output Slot */}
                    <div
                      className="w-14 h-14 rounded border-2 bg-[#12161E] border-[#2E3747] flex flex-col items-center justify-center relative shadow-inner"
                      title="Слот відпрацьованої капсули (шлак)"
                    >
                      {depletedCount > 0 ? (
                        <>
                          <img
                            src={depletedCapsuleTex}
                            alt="Відпрацьована капсула"
                            className="w-10 h-10"
                            style={{ imageRendering: 'pixelated' }}
                          />
                          <span className="absolute bottom-0.5 right-1 font-mono text-[11px] font-bold text-white tabular-nums drop-shadow">
                            {depletedCount}
                          </span>
                        </>
                      ) : (
                        <span className="text-[10px] font-mono text-slate-500">Шлак</span>
                      )}
                    </div>
                  </div>

                  {/* Telemetry Display + IE Knife Breaker Switch */}
                  <div className="flex-1 flex flex-col justify-between h-48 py-1">
                    <div className="space-y-2 bg-[#141822] border border-[#2E3646] rounded p-2.5 shadow-inner">
                      <div>
                        <div className="text-[10px] font-mono text-slate-400 uppercase">
                          Генерація FE
                        </div>
                        <div className="text-base sm:text-lg font-mono font-bold text-[#FBBF24] tabular-nums">
                          +{fePerTick.toLocaleString('uk-UA')} FE/t
                        </div>
                      </div>
                      <div>
                        <div className="text-[10px] font-mono text-slate-400 uppercase">
                          Ресурс капсули
                        </div>
                        <div className="text-xs font-mono text-slate-200 tabular-nums">
                          {fuelSlot !== 'none'
                            ? `${100 - fuelProgress}% (${Math.round(fuelTicks / 20)}с)`
                            : 'Немає палива'}
                        </div>
                      </div>
                    </div>

                    {/* Immersive Engineering Heavy Knife Switch */}
                    <button
                      onClick={handleToggleKnifeSwitch}
                      disabled={isMeltdown || !multiblockValid || (!active && fuelSlot === 'none')}
                      className={`w-full py-2.5 px-3 rounded font-mono text-xs font-bold border-2 flex items-center justify-center gap-2 transition-all shadow-md ${
                        active
                          ? 'bg-[#8B1E1E] hover:bg-[#A82525] border-[#FF4D4D] text-white'
                          : 'bg-[#2A3428] hover:bg-[#394B36] border-[#578550] text-[#A6E39C] disabled:bg-[#181D26] disabled:border-slate-800 disabled:text-slate-600'
                      }`}
                    >
                      <div
                        className="w-4 h-4"
                        style={{
                          backgroundImage: `url(${breakerSwitchTex})`,
                          backgroundSize: 'contain',
                          imageRendering: 'pixelated',
                        }}
                      />
                      <span>
                        {active ? 'РОЗІРВАТИ ЛАНЦЮГ (СТОП)' : 'ЗАМКНУТИ НОЖОВИЙ ВИМИКАЧ (ПУСК)'}
                      </span>
                    </button>
                  </div>
                </div>
              </div>

              {/* 4. FE Energy Capacitor Storage Gauge (IE Capacitor Style with Lightning Bolt) */}
              <div className="col-span-12 sm:col-span-2 flex flex-col items-center mt-3 sm:mt-0">
                <span className="text-[11px] font-mono font-semibold text-slate-300 mb-1.5 flex items-center gap-1">
                  <Zap className="w-3 h-3 text-amber-400" />
                  FE
                </span>
                <div
                  className="w-full sm:w-8 h-10 sm:h-48 bg-[#0D1017] border-2 border-[#384152] rounded-sm relative overflow-hidden flex sm:flex-col justify-end p-0.5 shadow-inner"
                  title={`Буфер накопичувача: ${energyFe.toLocaleString('uk-UA')} / ${MAX_ENERGY.toLocaleString('uk-UA')} FE`}
                >
                  {/* Energy Segmented Bar with IE Gradient */}
                  <div
                    className="h-full sm:w-full bg-gradient-to-t from-[#B45309] via-[#F59E0B] to-[#FDE68A] transition-all duration-200 shadow-[0_0_8px_rgba(245,158,11,0.4)]"
                    style={{
                      height: `${energyPercent}%`,
                      width: '100%',
                    }}
                  />
                  {/* Overlay Lightning Bolt */}
                  <div className="absolute inset-0 flex items-center justify-center pointer-events-none opacity-40">
                    <Zap className="w-4 h-4 text-amber-900 fill-amber-900" />
                  </div>
                </div>
                <span className="mt-2 font-mono text-[11px] text-amber-300 tabular-nums">
                  {Math.round(energyFe / 1000)}k FE
                </span>
              </div>
            </div>

            {/* In-Game Status Summary Footer */}
            <div className="mt-5 pt-3 border-t border-[#313B4D] flex flex-wrap items-center justify-between gap-3 text-xs font-mono text-slate-400">
              <span className="flex items-center gap-1.5">
                <Gauge className="w-3.5 h-3.5 text-amber-400" />
                Буфер: {energyFe.toLocaleString('uk-UA')} / 1 000 000 FE
              </span>
              <span className={`flex items-center gap-1.5 ${tempStatus.color}`}>
                <AlertTriangle className="w-3.5 h-3.5" />
                {scramTripped ? 'Аварійний SCRAM (1150°C)' : tempStatus.label}
              </span>
            </div>
          </div>
        </div>

        {/* Right: Immersive Engineering External Connections & Meltdown Tester Bench */}
        <div className="lg:col-span-5 space-y-6">
          {/* Engineering Controls Box */}
          <div className="bg-[#121722] border border-slate-800 rounded-xl p-5 space-y-5">
            <h2 className="text-base font-semibold text-slate-100 flex items-center gap-2">
              <Layers className="w-4 h-4 text-amber-400" />
              Зовнішні підключення та тести аварій
            </h2>

            {/* Fuel Capsule Insertion Controls */}
            <div>
              <div className="text-xs font-mono text-slate-400 mb-2">
                Завантажити паливну капсулу в активну зону:
              </div>
              <div className="grid grid-cols-1 sm:grid-cols-2 gap-2">
                <button
                  onClick={() => loadFuelCapsule('standard')}
                  disabled={isMeltdown}
                  className={`p-2.5 rounded-lg border text-left flex items-center gap-2.5 transition-colors ${
                    fuelSlot === 'standard'
                      ? 'bg-amber-500/15 border-amber-500/60 text-amber-200'
                      : 'bg-[#0B0E14] border-slate-800 text-slate-300 hover:border-slate-700'
                  }`}
                >
                  <img
                    src={standardCapsuleTex}
                    alt="Капсула U-238"
                    className="w-7 h-7 shrink-0"
                    style={{ imageRendering: 'pixelated' }}
                  />
                  <div>
                    <div className="text-xs font-medium">Капсула U-238 (1x)</div>
                    <div className="text-[10px] font-mono text-slate-400">2 400 тактів • 2048 FE/t</div>
                  </div>
                </button>

                <button
                  onClick={() => loadFuelCapsule('enriched')}
                  disabled={isMeltdown}
                  className={`p-2.5 rounded-lg border text-left flex items-center gap-2.5 transition-colors ${
                    fuelSlot === 'enriched'
                      ? 'bg-emerald-500/15 border-emerald-500/60 text-emerald-200'
                      : 'bg-[#0B0E14] border-slate-800 text-slate-300 hover:border-slate-700'
                  }`}
                >
                  <img
                    src={enrichedCapsuleTex}
                    alt="Капсула U-235"
                    className="w-7 h-7 shrink-0"
                    style={{ imageRendering: 'pixelated' }}
                  />
                  <div>
                    <div className="text-xs font-medium">Капсула U-235 (2x)</div>
                    <div className="text-[10px] font-mono text-emerald-400">4 800 тактів • 4096 FE/t</div>
                  </div>
                </button>
              </div>
            </div>

            {/* Coolant Feed & Refill using Vanilla Bucket Style */}
            <div className="pt-3 border-t border-slate-800/80 space-y-3">
              <div className="text-xs font-mono text-slate-400">
                Тип охолоджувача та доливання рідини:
              </div>
              <div className="grid grid-cols-2 gap-2">
                <button
                  onClick={() => setCoolantType('heavy_coolant')}
                  disabled={isMeltdown}
                  className={`py-2 px-3 text-xs font-medium rounded-lg border transition-colors ${
                    coolantType === 'heavy_coolant'
                      ? 'bg-cyan-500/15 border-cyan-500/60 text-cyan-300'
                      : 'bg-[#0B0E14] border-slate-800 text-slate-400'
                  }`}
                >
                  Heavy Coolant (IE)
                </button>
                <button
                  onClick={() => setCoolantType('water')}
                  disabled={isMeltdown}
                  className={`py-2 px-3 text-xs font-medium rounded-lg border transition-colors ${
                    coolantType === 'water'
                      ? 'bg-blue-500/15 border-blue-500/60 text-blue-300'
                      : 'bg-[#0B0E14] border-slate-800 text-slate-400'
                  }`}
                >
                  Звичайна вода
                </button>
              </div>

              {/* Bucket Refill with Classic Minecraft Bucket Icon */}
              <button
                onClick={() => setCoolantMb((c) => Math.min(MAX_COOLANT, c + 4000))}
                disabled={isMeltdown}
                className="w-full inline-flex items-center justify-center gap-2 py-2.5 px-4 text-xs font-medium bg-[#0B0E14] border border-cyan-800/60 rounded-lg text-cyan-300 hover:bg-cyan-950/30 transition-colors"
              >
                <img
                  src={coolantBucketTex}
                  alt="Minecraft відро"
                  className="w-5 h-5 shrink-0"
                  style={{ imageRendering: 'pixelated' }}
                />
                Залити +4 000 mB (класичне відро Minecraft)
              </button>
            </div>

            {/* Thermal Runaway & Circuit Disconnect Toggles */}
            <div className="pt-3 border-t border-slate-800/80 space-y-2">
              <div className="text-xs font-mono text-slate-400 mb-1">
                Тестування теплового розгону (Runaway) та навантаження:
              </div>
              <div className="grid grid-cols-1 sm:grid-cols-2 gap-2">
                {/* Pump Toggle */}
                <button
                  onClick={() => setPumpConnected((v) => !v)}
                  disabled={isMeltdown}
                  className={`p-2.5 rounded-lg border text-left transition-colors ${
                    pumpConnected
                      ? 'bg-cyan-500/10 border-cyan-500/40 text-cyan-200'
                      : 'bg-rose-950/30 border-rose-500/60 text-rose-300'
                  }`}
                >
                  <div className="flex items-center gap-1.5 text-xs font-medium">
                    <Droplets className="w-3.5 h-3.5" />
                    Помпа IE (Fluid Pipe)
                  </div>
                  <div className="text-[11px] font-mono mt-0.5">
                    {pumpConnected ? 'Подача: УВІМК' : 'ВИМК! (ТЕПЛОВИЙ РОЗГІН)'}
                  </div>
                </button>

                {/* HV Load Wire Toggle */}
                <button
                  onClick={() => setHvLoadEnabled((v) => !v)}
                  disabled={isMeltdown}
                  className={`p-2.5 rounded-lg border text-left transition-colors ${
                    hvLoadEnabled
                      ? 'bg-amber-500/10 border-amber-500/40 text-amber-200'
                      : 'bg-[#0B0E14] border-slate-800 text-slate-400'
                  }`}
                >
                  <div className="flex items-center gap-1.5 text-xs font-medium">
                    <Zap className="w-3.5 h-3.5" />
                    HV Дроти IE
                  </div>
                  <div className="text-[11px] font-mono mt-0.5">
                    {hvLoadEnabled ? 'Споживач: 3500 FE/t' : 'Холостий хід'}
                  </div>
                </button>
              </div>

              {/* Multiblock Break Toggle */}
              <button
                onClick={() => setMultiblockValid((v) => !v)}
                disabled={isMeltdown}
                className={`w-full p-2.5 rounded-lg border text-left transition-colors ${
                  multiblockValid
                    ? 'bg-[#0B0E14] border-slate-800 text-slate-400'
                    : 'bg-rose-950/40 border-rose-500/60 text-rose-300'
                }`}
              >
                <div className="flex items-center gap-1.5 text-xs font-medium">
                  <ShieldAlert className="w-3.5 h-3.5" />
                  Мультиблок 3×3×3 (цілісність)
                </div>
                <div className="text-[11px] font-mono mt-0.5">
                  {multiblockValid
                    ? 'Всі 27 блоків зібрано належним чином'
                    : 'Зламано блок корпусу (зупинка реактора)'}
                </div>
              </button>
            </div>
          </div>

          {/* Live Dual-Axis Telemetry SVG Chart */}
          <div className="bg-[#121722] border border-slate-800 rounded-xl p-5">
            <div className="flex flex-wrap items-center justify-between gap-3 mb-3">
              <h3 className="text-sm font-semibold text-slate-100 flex items-center gap-2">
                <Flame className="w-4 h-4 text-rose-400" />
                Телеметрія температури та FE/t
              </h3>
              <div className="flex items-center gap-3 text-xs font-mono">
                <span className="text-rose-400">― Темп. (°C)</span>
                <span className="text-amber-400">― Генерація FE</span>
              </div>
            </div>

            <div className="w-full overflow-hidden rounded-lg bg-[#0B0E14] border border-slate-800/80 p-2.5">
              <svg
                viewBox={`0 0 ${chartWidth} ${chartHeight}`}
                className="w-full h-36 overflow-visible"
                role="img"
                aria-label="Графік температури та генерації FE/t"
              >
                {/* Horizontal reference grid */}
                {[0.25, 0.5, 0.75].map((ratio) => (
                  <line
                    key={ratio}
                    x1={0}
                    x2={chartWidth}
                    y1={chartHeight * ratio}
                    y2={chartHeight * ratio}
                    stroke="#1E293B"
                    strokeDasharray="4 4"
                    strokeWidth={1}
                  />
                ))}

                {/* SCRAM threshold line */}
                <line
                  x1={0}
                  x2={chartWidth}
                  y1={chartHeight - (SCRAM_TEMP / MELTDOWN_TEMP) * (chartHeight - 16) - 8}
                  y2={chartHeight - (SCRAM_TEMP / MELTDOWN_TEMP) * (chartHeight - 16) - 8}
                  stroke="#F59E0B"
                  strokeOpacity={0.4}
                  strokeDasharray="3 3"
                  strokeWidth={1}
                />

                {/* Meltdown Explosion Line */}
                <line
                  x1={0}
                  x2={chartWidth}
                  y1={8}
                  y2={8}
                  stroke="#EF4444"
                  strokeOpacity={0.6}
                  strokeDasharray="2 2"
                  strokeWidth={1}
                />

                {/* FE/t Curve */}
                <path d={fePath} fill="none" stroke="#F59E0B" strokeWidth={2} />

                {/* Temperature Curve */}
                <path d={tempPath} fill="none" stroke="#FB7185" strokeWidth={2.5} />
              </svg>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
