import { useMemo, useState } from 'react';
import { Check, Copy, Download, FileCode2, Search } from 'lucide-react';
import { ALL_MOD_FILES, exportModZip, type ModFileEntry } from '../modProject';

type CategoryFilter = 'all' | 'java' | 'recipes' | 'gradle' | 'resources';

export function CodeExplorerView() {
  const [category, setCategory] = useState<CategoryFilter>('all');
  const [search, setSearch] = useState('');
  const [selectedPath, setSelectedPath] = useState<string>(ALL_MOD_FILES[5]?.path ?? ALL_MOD_FILES[0].path);
  const [copied, setCopied] = useState(false);

  const filteredFiles = useMemo(() => {
    const q = search.trim().toLowerCase();
    return ALL_MOD_FILES.filter((f) => {
      if (category !== 'all' && f.category !== category) return false;
      if (!q) return true;
      return f.path.toLowerCase().includes(q) || f.description.toLowerCase().includes(q);
    });
  }, [category, search]);

  const activeFile: ModFileEntry =
    filteredFiles.find((f) => f.path === selectedPath) ?? filteredFiles[0] ?? ALL_MOD_FILES[0];

  const handleCopy = () => {
    navigator.clipboard.writeText(activeFile.content);
    setCopied(true);
    setTimeout(() => setCopied(false), 1800);
  };

  return (
    <div className="space-y-6">
      <div className="flex flex-col lg:flex-row lg:items-end justify-between gap-4 pb-6 border-b border-slate-800">
        <div>
          <p className="text-xs font-mono text-slate-400 mb-2">
            Готовий Gradle-проєкт · {ALL_MOD_FILES.length} файлів · Java 21 + NeoForge 1.21.1
          </p>
          <h1 className="text-2xl sm:text-3xl font-semibold tracking-tight text-slate-100">
            Вихідний код моду та конфігурація збірки
          </h1>
        </div>

        <button
          onClick={exportModZip}
          className="inline-flex items-center gap-2 px-4 py-2 text-sm font-semibold bg-amber-500 text-slate-950 rounded-lg hover:bg-amber-400 transition-colors whitespace-nowrap shrink-0"
        >
          <Download className="w-4 h-4" />
          Завантажити проєкт (.zip)
        </button>
      </div>

      {/* Category Filter + Search Bar */}
      <div className="flex flex-col sm:flex-row items-stretch sm:items-center justify-between gap-4">
        <div className="flex items-center gap-1 p-1 bg-[#121722] border border-slate-800 rounded-lg overflow-x-auto">
          {(
            [
              { id: 'all', label: 'Усі файли' },
              { id: 'java', label: 'Java класи' },
              { id: 'recipes', label: 'Рецепти IE' },
              { id: 'gradle', label: 'Gradle / README' },
              { id: 'resources', label: 'Мова / Ресурси' },
            ] as const
          ).map((tab) => (
            <button
              key={tab.id}
              onClick={() => setCategory(tab.id)}
              className={`px-3 py-1.5 text-xs font-medium rounded-md transition-colors whitespace-nowrap shrink-0 ${
                category === tab.id
                  ? 'bg-amber-500 text-slate-950 font-semibold'
                  : 'text-slate-400 hover:text-slate-100'
              }`}
            >
              {tab.label}
            </button>
          ))}
        </div>

        <div className="relative w-full sm:w-72">
          <Search className="w-4 h-4 text-slate-500 absolute left-3 top-1/2 -translate-y-1/2" />
          <input
            type="text"
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            placeholder="Пошук файлу..."
            className="w-full pl-9 pr-3 py-2 text-xs bg-[#121722] border border-slate-800 rounded-lg text-slate-200 placeholder:text-slate-500 focus:outline-none focus:border-amber-500/70"
          />
        </div>
      </div>

      {/* Split IDE File Tree + Source Viewer */}
      <div className="grid grid-cols-1 lg:grid-cols-12 gap-6 items-start">
        {/* File List */}
        <div className="lg:col-span-4 bg-[#121722] border border-slate-800 rounded-xl overflow-hidden max-h-[640px] flex flex-col">
          <div className="px-4 py-3 border-b border-slate-800 text-xs font-mono text-slate-400">
            Структура проєкту ({filteredFiles.length})
          </div>
          <div className="overflow-y-auto divide-y divide-slate-800/50">
            {filteredFiles.length === 0 ? (
              <div className="p-6 text-center text-xs text-slate-500">
                Файлів за запитом не знайдено
              </div>
            ) : (
              filteredFiles.map((file) => {
                const isSelected = file.path === activeFile.path;
                const shortName = file.path.split('/').pop() ?? file.path;
                const dirPath = file.path.slice(0, file.path.length - shortName.length);
                return (
                  <button
                    key={file.path}
                    onClick={() => setSelectedPath(file.path)}
                    className={`w-full text-left px-4 py-2.5 flex items-start gap-2.5 transition-colors ${
                      isSelected ? 'bg-amber-500/10' : 'hover:bg-slate-800/40'
                    }`}
                  >
                    <FileCode2
                      className={`w-4 h-4 mt-0.5 shrink-0 ${
                        isSelected ? 'text-amber-400' : 'text-slate-500'
                      }`}
                    />
                    <div className="min-w-0">
                      <div
                        className={`text-xs font-mono truncate ${
                          isSelected ? 'text-amber-300 font-semibold' : 'text-slate-200'
                        }`}
                      >
                        {shortName}
                      </div>
                      <div className="text-[11px] font-mono text-slate-500 truncate">{dirPath}</div>
                    </div>
                  </button>
                );
              })
            )}
          </div>
        </div>

        {/* Code Viewer */}
        <div className="lg:col-span-8 bg-[#121722] border border-slate-800 rounded-xl overflow-hidden flex flex-col">
          <div className="px-5 py-3.5 border-b border-slate-800 flex flex-wrap items-center justify-between gap-3">
            <div className="min-w-0">
              <div className="text-xs font-mono text-amber-400 truncate">{activeFile.path}</div>
              <div className="text-xs text-slate-400 mt-0.5">{activeFile.description}</div>
            </div>
            <button
              onClick={handleCopy}
              className="inline-flex items-center gap-2 px-3 py-1.5 text-xs font-medium bg-[#0B0E14] border border-slate-700 rounded-lg text-slate-200 hover:border-slate-500 transition-colors whitespace-nowrap shrink-0"
            >
              {copied ? <Check className="w-3.5 h-3.5 text-emerald-400" /> : <Copy className="w-3.5 h-3.5" />}
              {copied ? 'Скопійовано' : 'Копіювати'}
            </button>
          </div>
          <div className="bg-[#0B0E14] overflow-auto max-h-[560px]">
            <pre className="p-5 text-xs font-mono text-slate-200 leading-relaxed">
              <code>{activeFile.content}</code>
            </pre>
          </div>
        </div>
      </div>
    </div>
  );
}
