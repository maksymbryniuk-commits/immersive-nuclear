import { useMemo, useState } from 'react';
import { Download, Grid3X3 } from 'lucide-react';
import { getRenderedTextures, type RenderedTexture } from '../modProject/pixelTextures';

type TextureFilter = 'all' | 'block' | 'item';

export function TexturesGalleryView() {
  const textures = useMemo(() => getRenderedTextures(), []);
  const [filter, setFilter] = useState<TextureFilter>('all');
  const [selectedId, setSelectedId] = useState<string>(textures[0].id);

  const filtered = useMemo(
    () => textures.filter((t) => filter === 'all' || t.kind === filter),
    [textures, filter]
  );

  const selected: RenderedTexture =
    filtered.find((t) => t.id === selectedId) ?? filtered[0] ?? textures[0];

  const downloadSinglePng = (tex: RenderedTexture) => {
    const blob = new Blob([new Uint8Array(tex.pngBytes)], { type: 'image/png' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `${tex.id}.png`;
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
    URL.revokeObjectURL(url);
  };

  return (
    <div className="space-y-8">
      <div className="flex flex-col lg:flex-row lg:items-end justify-between gap-4 pb-6 border-b border-slate-800">
        <div>
          <p className="text-xs font-mono text-slate-400 mb-2">
            Minecraft 16×16 Pixel Art · PNG RGBA · Автоматично пакуються у .zip
          </p>
          <h1 className="text-2xl sm:text-3xl font-semibold tracking-tight text-slate-100 max-w-2xl">
            Текстури 16×16 пікселів для блоків, палива та рідин
          </h1>
        </div>

        <div className="flex items-center gap-1 p-1 bg-[#121722] border border-slate-800 rounded-lg self-start lg:self-auto">
          {(
            [
              { id: 'all', label: `Усі (${textures.length})` },
              { id: 'block', label: 'Блоки (7)' },
              { id: 'item', label: 'Предмети (9)' },
            ] as const
          ).map((tab) => (
            <button
              key={tab.id}
              onClick={() => setFilter(tab.id)}
              className={`px-3 py-1.5 text-xs font-medium rounded-md transition-colors whitespace-nowrap shrink-0 ${
                filter === tab.id
                  ? 'bg-amber-500 text-slate-950 font-semibold'
                  : 'text-slate-400 hover:text-slate-100'
              }`}
            >
              {tab.label}
            </button>
          ))}
        </div>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-12 gap-8 items-start">
        {/* Left: Grid of 16x16 Textures */}
        <div className="lg:col-span-7 grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 gap-3">
          {filtered.map((tex) => {
            const isSelected = tex.id === selected.id;
            return (
              <button
                key={tex.id}
                onClick={() => setSelectedId(tex.id)}
                className={`p-3 rounded-xl border text-left transition-colors flex flex-col items-center gap-2.5 ${
                  isSelected
                    ? 'bg-[#161D2B] border-amber-500/70'
                    : 'bg-[#121722] border-slate-800 hover:border-slate-700'
                }`}
              >
                <div className="w-16 h-16 bg-[#0B0E14] border border-slate-800 rounded-lg flex items-center justify-center p-2">
                  <img
                    src={tex.dataUrl}
                    alt={tex.nameUk}
                    className="w-12 h-12"
                    style={{ imageRendering: 'pixelated' }}
                  />
                </div>
                <div className="w-full text-center">
                  <div className="text-xs font-medium text-slate-100 truncate">{tex.nameUk}</div>
                  <div className="text-[10px] font-mono text-slate-500 truncate">
                    {tex.kind === 'block' ? 'block/' : 'item/'}
                    {tex.id}.png
                  </div>
                </div>
              </button>
            );
          })}
        </div>

        {/* Right: Pixel Inspector for Selected 16x16 Texture */}
        <div className="lg:col-span-5 bg-[#121722] border border-slate-800 rounded-xl p-6 space-y-5">
          <div className="flex items-start justify-between gap-4">
            <div className="min-w-0">
              <div className="text-xs font-mono text-amber-400 mb-1">
                16×16 px · {selected.kind === 'block' ? 'Текстура блоку' : 'Текстура предмета'}
              </div>
              <h2 className="text-lg font-semibold text-slate-100">{selected.nameUk}</h2>
              <p className="text-xs text-slate-400 mt-1">{selected.description}</p>
            </div>
            <button
              onClick={() => downloadSinglePng(selected)}
              className="inline-flex items-center gap-1.5 px-3 py-1.5 text-xs font-medium bg-[#0B0E14] border border-slate-700 rounded-lg text-slate-200 hover:border-amber-500/60 transition-colors whitespace-nowrap shrink-0"
            >
              <Download className="w-3.5 h-3.5" />
              PNG
            </button>
          </div>

          {/* 16x16 Pixel Grid Zoom Preview */}
          <div className="bg-[#0B0E14] border border-slate-800 rounded-xl p-5 flex flex-col items-center gap-4">
            {selected.rawBase64 ? (
              <div
                className="relative border border-slate-700 bg-[#07090D]"
                style={{ width: '224px', height: '224px' }}
                aria-label={`Збільшена 16x16 текстура для ${selected.nameUk}`}
              >
                <img
                  src={selected.dataUrl}
                  alt={selected.nameUk}
                  className="w-full h-full"
                  style={{ imageRendering: 'pixelated' }}
                />
                <div className="absolute inset-0 grid grid-cols-16 pointer-events-none">
                  {Array.from({ length: 256 }, (_, i) => (
                    <div key={i} className="border-[0.5px] border-slate-950/25" />
                  ))}
                </div>
              </div>
            ) : (
              <div
                className="grid grid-cols-16 border border-slate-700"
                style={{ width: '224px', height: '224px' }}
                aria-label={`Піксельна сітка 16 на 16 для ${selected.nameUk}`}
              >
                {selected.grid.map((row, y) =>
                  Array.from({ length: 16 }, (_, x) => {
                    const ch = row[x] ?? '.';
                    const hex = selected.palette[ch] ?? '#00000000';
                    const isTransparent = hex.endsWith('00');
                    return (
                      <div
                        key={`${x}-${y}`}
                        className="border-[0.5px] border-slate-900/40"
                        style={{
                          backgroundColor: isTransparent ? 'transparent' : hex,
                        }}
                      />
                    );
                  })
                )}
              </div>
            )}

            <div className="flex items-center gap-6 text-xs font-mono text-slate-400">
              <span className="inline-flex items-center gap-1.5">
                <Grid3X3 className="w-3.5 h-3.5 text-slate-500" />
                Розмір: 16×16 px
              </span>
              <span className="inline-flex items-center gap-2">
                1:1 превʼю:
                <img
                  src={selected.dataUrl}
                  alt={selected.id}
                  className="w-4 h-4 inline-block bg-slate-900"
                  style={{ imageRendering: 'pixelated' }}
                />
              </span>
            </div>
          </div>

          <div className="bg-[#0B0E14] border border-slate-800 rounded-lg p-3 text-xs font-mono text-slate-400 break-all">
            {selected.zipPath}
          </div>
        </div>
      </div>
    </div>
  );
}
