import { useState } from 'react';
import { Download } from 'lucide-react';
import { ReactorSimulatorView } from './components/ReactorSimulatorView';
import { MultiblockBlueprintView } from './components/MultiblockBlueprintView';
import { IERecipesView } from './components/IERecipesView';
import { CodeExplorerView } from './components/CodeExplorerView';
import { TexturesGalleryView } from './components/TexturesGalleryView';
import { exportModZip } from './modProject';

type ActiveTab = 'simulator' | 'multiblock' | 'textures' | 'recipes' | 'code';

const NAV_ITEMS: { id: ActiveTab; label: string }[] = [
  { id: 'simulator', label: 'Симулятор GUI' },
  { id: 'multiblock', label: 'Гайд IE · Мультиблок' },
  { id: 'textures', label: 'Текстури 16×16' },
  { id: 'recipes', label: 'Рецепти IE' },
  { id: 'code', label: 'Код моду' },
];

export default function App() {
  const [activeTab, setActiveTab] = useState<ActiveTab>('simulator');
  const [exporting, setExporting] = useState(false);

  const handleDownload = async () => {
    setExporting(true);
    try {
      await exportModZip();
    } finally {
      setExporting(false);
    }
  };

  return (
    <div className="min-h-screen bg-[#0B0E14] text-slate-100 flex flex-col">
      {/* Top Bar Contract: 3 zones separated by gap-8 */}
      <header className="border-b border-slate-800 bg-[#0B0E14]">
        <div className="max-w-[1320px] mx-auto px-6 h-16 flex items-center justify-between gap-8">
          {/* Zone 1: Single-element brand wordmark */}
          <button
            onClick={() => setActiveTab('simulator')}
            className="text-lg font-bold tracking-tight text-slate-100 whitespace-nowrap shrink-0 hover:text-amber-400 transition-colors"
          >
            Immersive Nuclear
          </button>

          {/* Zone 2: Clean single-line navigation links */}
          <nav className="hidden md:flex items-center gap-7" aria-label="Навігація">
            {NAV_ITEMS.map((item) => {
              const isActive = activeTab === item.id;
              return (
                <button
                  key={item.id}
                  onClick={() => setActiveTab(item.id)}
                  className={`py-5 text-sm font-medium border-b-2 transition-colors whitespace-nowrap shrink-0 ${
                    isActive
                      ? 'border-amber-500 text-slate-100'
                      : 'border-transparent text-slate-400 hover:text-slate-100 hover:border-slate-700'
                  }`}
                >
                  {item.label}
                </button>
              );
            })}
          </nav>

          {/* Zone 3: Single primary CTA */}
          <div className="shrink-0">
            <button
              onClick={handleDownload}
              disabled={exporting}
              className="inline-flex items-center gap-2 px-4 py-2 text-xs font-semibold text-slate-950 bg-amber-500 rounded-lg hover:bg-amber-400 disabled:opacity-60 transition-colors whitespace-nowrap shrink-0"
            >
              <Download className="w-4 h-4" />
              {exporting ? 'Формування архіву...' : 'Завантажити мод (.zip)'}
            </button>
          </div>
        </div>

        {/* Mobile navigation strip */}
        <div className="md:hidden border-t border-slate-800/80 px-4 py-2 flex items-center gap-2 overflow-x-auto">
          {NAV_ITEMS.map((item) => {
            const isActive = activeTab === item.id;
            return (
              <button
                key={item.id}
                onClick={() => setActiveTab(item.id)}
                className={`px-3 py-1.5 text-xs font-medium rounded-md whitespace-nowrap shrink-0 ${
                  isActive
                    ? 'bg-amber-500 text-slate-950 font-semibold'
                    : 'text-slate-400 hover:text-slate-100'
                }`}
              >
                {item.label}
              </button>
            );
          })}
        </div>
      </header>

      {/* Main Content Container */}
      <main className="flex-1 max-w-[1320px] w-full mx-auto px-6 py-8">
        {activeTab === 'simulator' && <ReactorSimulatorView />}
        {activeTab === 'multiblock' && <MultiblockBlueprintView />}
        {activeTab === 'textures' && <TexturesGalleryView />}
        {activeTab === 'recipes' && <IERecipesView />}
        {activeTab === 'code' && <CodeExplorerView />}
      </main>

      {/* Quiet Footer */}
      <footer className="border-t border-slate-800/80 py-6 px-6 text-xs text-slate-500">
        <div className="max-w-[1320px] mx-auto flex flex-col sm:flex-row items-center justify-between gap-4">
          <span>Immersive Nuclear · Minecraft NeoForge 1.21.1 · Сумісний з Immersive Engineering</span>
          <button
            onClick={() => setActiveTab('code')}
            className="text-slate-400 hover:text-amber-400 transition-colors whitespace-nowrap"
          >
            Переглянути Gradle та Java-класи
          </button>
        </div>
      </footer>
    </div>
  );
}
