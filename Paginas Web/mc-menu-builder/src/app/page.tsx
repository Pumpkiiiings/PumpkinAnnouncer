"use client";

import React, { useState } from 'react';
import { ItemBrowser } from '@/components/inventory/ItemBrowser';
import { Slot } from '@/components/inventory/Slot';
import { ImportModal } from '@/components/inventory/ImportModal';
import { ExportModal } from '@/components/inventory/ExportModal';
import { ItemTooltip } from '@/components/inventory/ItemTooltip';
import { MCItem3D } from '@/components/MCItems3D';
import { toDeluxeMenus } from '@/lib/converters/deluxeMenus';
import { toZMenu } from '@/lib/converters/zmenu';
import { toCommandPanels } from '@/lib/converters/commandPanels';
import { parseInventoryYAML } from '@/lib/parser';
import yaml from 'js-yaml';
import { PlaceholderToggle } from '@/components/PlaceholderToggle';

export default function MenuBuilderPage() {
  const [menuName, setMenuName] = useState("Nuevo Menú");
  const [pluginType, setPluginType] = useState<'DeluxeMenus' | 'ZMenu' | 'CommandPanels'>('DeluxeMenus');
  const [slots, setSlots] = useState<Record<number, any>>({});
  const [selectedSlot, setSelectedSlot] = useState<number | null>(null);
  const [menuSize, setMenuSize] = useState(54);
  const [activeItem, setActiveItem] = useState<any>(null);
  const [isImportOpen, setIsImportOpen] = useState(false);
  const [isExportOpen, setIsExportOpen] = useState(false);
  const [exportedCode, setExportedCode] = useState("");
  const [hoveredItem, setHoveredItem] = useState<any>(null);

  const btnModern = "text-xs font-semibold px-5 py-2.5 rounded-xl transition-all active:scale-95 flex items-center gap-2 shadow-lg backdrop-blur-md uppercase tracking-wider";

  const handleImportLogic = (yamlInput: string) => {
    // Tu lógica de importación...
  };

  const handleExport = () => {
    const config = { name: menuName, title: menuName, size: menuSize, items: slots };
    let result = pluginType === 'DeluxeMenus' ? toDeluxeMenus(config) : pluginType === 'ZMenu' ? toZMenu(config) : toCommandPanels(config);
    setExportedCode(yaml.dump(result, { indent: 2 }));
    setIsExportOpen(true);
  };

  const handleSlotClick = (index: number) => {
    if (activeItem) {
      // 🚨 CORRECCIÓN CLAVE: Guardamos el 'mcIndex' (o index) del JSON 🚨
      // Si no guardas esto, el Slot no sabrá qué recorte de la imagen mostrar
      setSlots({
        ...slots,
        [index]: {
          id: activeItem.id,
          title: `<white>${activeItem.name}`,
          lore: ["<gray>Nueva línea"],
          // Guardamos el índice numérico que viene del JSON
          mcIndex: activeItem.index
        }
      });
      setActiveItem(null);
    } else {
      setSelectedSlot(index);
    }
  };

  const updateCurrentItem = (field: string, value: any) => {
    if (selectedSlot === null || !slots[selectedSlot]) return;
    setSlots({ ...slots, [selectedSlot]: { ...slots[selectedSlot], [field]: value } });
  };

  return (
    <main className="min-h-screen bg-[#000000] bg-[radial-gradient(ellipse_at_top,_var(--tw-gradient-stops))] from-zinc-900 via-[#000] to-black text-white p-6 font-sans selection:bg-yellow-500/30">
      <ItemTooltip visible={!!hoveredItem} title={hoveredItem?.title || ""} lore={hoveredItem?.lore || []} />
      <ImportModal isOpen={isImportOpen} onClose={() => setIsImportOpen(false)} onImport={handleImportLogic} />
      <ExportModal isOpen={isExportOpen} onClose={() => setIsExportOpen(false)} code={exportedCode} plugin={pluginType} />

      {/* Navbar */}
      <div className="max-w-[1600px] mx-auto mb-8 flex justify-between items-center bg-white/[0.02] border border-white/5 px-8 py-5 rounded-2xl shadow-2xl backdrop-blur-sm">
        <div className="flex items-center gap-8">
          <h1 className="text-3xl text-yellow-500 uppercase tracking-widest mc-shadow" style={{ fontFamily: 'MinecraftTen, sans-serif' }}>
            PUMPKIN<span className="text-white">GUI</span>
          </h1>
          <div className="flex bg-black/50 p-1 rounded-lg border border-white/5">
            {['DeluxeMenus', 'ZMenu', 'CommandPanels'].map((type) => (
              <button
                key={type}
                onClick={() => setPluginType(type as any)}
                className={`text-[10px] font-mono px-4 py-2 rounded-md transition-all uppercase tracking-widest ${pluginType === type
                  ? 'bg-yellow-500 text-black shadow-[0_0_15px_rgba(234,179,8,0.4)]'
                  : 'text-zinc-500 hover:text-white'
                  }`}
              >
                {type}
              </button>
            ))}
          </div>
        </div>

        <div className="flex items-center gap-6">
          <div className="border-r border-white/10 pr-6 opacity-70 hover:opacity-100 transition-opacity">
            <PlaceholderToggle />
          </div>

          <div className="flex gap-4 items-center">
            <select
              className="bg-black/50 border border-white/10 text-xs p-3 rounded-xl outline-none font-mono cursor-pointer text-yellow-500 hover:border-yellow-500/50 transition-colors appearance-none px-6"
              onChange={(e) => setMenuSize(Number(e.target.value))}
              value={menuSize}
            >
              {[9, 18, 27, 36, 45, 54].map(s => <option key={s} value={s}>{s} Slots</option>)}
            </select>

            <button onClick={() => setIsImportOpen(true)} className={`${btnModern} bg-white/5 hover:bg-white/10 text-white border border-white/10`}>
              IMPORTAR
            </button>
            <button onClick={handleExport} className={`${btnModern} bg-yellow-500 hover:bg-yellow-400 text-black shadow-[0_0_20px_rgba(234,179,8,0.3)]`}>
              EXPORTAR
            </button>
          </div>
        </div>
      </div>

      <div className="max-w-[1600px] mx-auto grid grid-cols-12 gap-8">

        {/* Panel Izquierdo: Librería */}
        <div className="col-span-4 h-fit sticky top-6">
          <ItemBrowser onSelectItem={(item) => setActiveItem(item)} />
        </div>

        {/* Panel Central: Inventario */}
        <div className="col-span-5 flex flex-col items-center justify-start pt-12">

          {/* Indicador Flotante (Colocando...) */}
          {activeItem && (
            <div className="mb-6 px-4 py-2 bg-yellow-500/20 border border-yellow-500/50 rounded-full flex items-center gap-3 animate-pulse">
              {/* CAMBIO: Usamos index en vez de ID para la previsualización */}
              <MCItem3D index={activeItem.index} size={24} />
              <span className="text-xs font-mono text-yellow-500 uppercase">Colocando: {activeItem.name}</span>
            </div>
          )}

          <div className="bg-[#c6c6c6] p-4 border-[6px] border-t-[#fff] border-l-[#fff] border-b-[#555] border-r-[#555] shadow-[0_0_50px_rgba(0,0,0,0.8)] relative transition-all">
            <input
              value={menuName}
              onChange={(e) => setMenuName(e.target.value)}
              className="bg-transparent text-[#373737] text-xl font-minecraft mb-4 outline-none w-full px-2 py-1 placeholder:text-[#555] focus:bg-white/10 transition-colors rounded-sm"
              spellCheck="false"
            />
            <div className="grid grid-cols-9 gap-1 bg-[#8b8b8b] p-2 border-[4px] border-[#373737] shadow-inner">
              {Array.from({ length: menuSize }).map((_, i) => (
                <Slot
                  key={i} index={i} item={slots[i]}
                  onSelect={handleSlotClick}
                  onMouseEnter={(item: any) => setHoveredItem(item)}
                  onMouseLeave={() => setHoveredItem(null)}
                />
              ))}
            </div>
          </div>
        </div>

        {/* Panel Derecho: Propiedades */}
        <div className="col-span-3 bg-[#0a0a0a]/80 backdrop-blur-xl p-6 border border-white/10 rounded-2xl shadow-2xl h-fit sticky top-6">
          <h2 className="text-white font-semibold text-sm mb-6 flex justify-between items-center uppercase tracking-widest opacity-90">
            <span className="flex items-center gap-2">
              <span className="w-2 h-2 bg-blue-500 rounded-full shadow-[0_0_10px_rgba(59,130,246,0.5)]"></span>
              Propiedades
            </span>
            {selectedSlot !== null && <span className="bg-white/10 text-zinc-300 px-2 py-1 rounded text-[10px] font-mono">SLOT #{selectedSlot}</span>}
          </h2>

          {selectedSlot !== null && slots[selectedSlot] ? (
            <div className="space-y-6 animate-in fade-in slide-in-from-bottom-4 duration-300">

              <div className="flex items-center gap-4 bg-black/50 p-4 rounded-xl border border-white/5">
                <div className="w-12 h-12 flex items-center justify-center bg-white/5 rounded-lg border border-white/10 shadow-inner">
                  {/* CAMBIO: Usamos 'mcIndex' (que guardamos antes) para mostrar la imagen correcta */}
                  <MCItem3D index={slots[selectedSlot].mcIndex ?? 0} size={36} />
                </div>
                <div>
                  <span className="text-xs font-mono text-zinc-400 block uppercase tracking-wider mb-1">ID del ítem</span>
                  <span className="text-sm font-minecraft text-white drop-shadow-md">{slots[selectedSlot].id}</span>
                </div>
              </div>

              <div className="space-y-2">
                <label className="text-[10px] text-zinc-500 font-mono uppercase tracking-widest ml-1">Título Display</label>
                <input
                  type="text" value={slots[selectedSlot].title}
                  onChange={(e) => updateCurrentItem('title', e.target.value)}
                  className="w-full bg-black/50 border border-white/10 rounded-xl p-3.5 text-sm text-green-400 outline-none font-minecraft focus:border-blue-500/50 focus:bg-blue-500/5 transition-all shadow-inner"
                />
              </div>

              <div className="space-y-2">
                <label className="text-[10px] text-zinc-500 font-mono uppercase tracking-widest ml-1">Lore / Descripción</label>
                <textarea
                  rows={6} value={slots[selectedSlot].lore.join('\n')}
                  onChange={(e) => updateCurrentItem('lore', e.target.value.split('\n'))}
                  className="w-full bg-black/50 border border-white/10 rounded-xl p-3.5 text-xs text-zinc-300 outline-none font-minecraft resize-none custom-scrollbar focus:border-blue-500/50 focus:bg-blue-500/5 transition-all shadow-inner leading-relaxed"
                />
              </div>

              <button
                onClick={() => {
                  const newSlots = { ...slots };
                  delete newSlots[selectedSlot];
                  setSlots(newSlots);
                  setSelectedSlot(null);
                }}
                className="w-full text-xs font-semibold bg-red-500/10 hover:bg-red-500/20 text-red-500 border border-red-500/20 py-3 rounded-xl transition-all uppercase tracking-widest"
              >
                ELIMINAR ÍTEM
              </button>
            </div>
          ) : (
            <div className="flex flex-col items-center justify-center py-24 opacity-40">
              <div className="w-16 h-16 mb-6 border-2 border-dashed border-zinc-600 rounded-2xl flex items-center justify-center">
                <span className="text-2xl">🪄</span>
              </div>
              <p className="font-mono text-xs text-zinc-400 uppercase tracking-widest text-center leading-relaxed">
                Selecciona un slot<br />para comenzar a editar
              </p>
            </div>
          )}
        </div>
      </div>
    </main>
  );
}
