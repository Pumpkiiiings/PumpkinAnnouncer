"use client";

import React, { useState, useMemo, memo } from 'react';
import { MCItem3D } from '@/components/MCItems3D';

const minecraftData = require('@/data/minecraft_items.json');

interface ItemBrowserProps {
    onSelectItem: (item: { id: string; name: string; index?: number }) => void;
}

const ItemGridCell = memo(({ item, onSelect, idx }: { item: any, onSelect: any, idx: number }) => (
    <div
        className="group relative w-full aspect-square bg-white/5 border border-white/5 rounded-lg flex items-center justify-center cursor-pointer hover:bg-yellow-500/20 hover:border-yellow-500/50 transition-all duration-200"
        onClick={() => onSelect(item)}
    >
        <div className="group-hover:scale-125 transition-transform duration-300 pointer-events-none">
            {/* AQUÍ LEEMOS EL INDEX DE TU ARCHIVO JSON */}
            <MCItem3D spriteIndex={item.index ?? 0} size={32} />
        </div>

        <div className={`absolute ${idx % 5 >= 3 ? 'right-full mr-2' : 'left-full ml-2'} top-1/2 -translate-y-1/2 hidden group-hover:block pointer-events-none z-[9999]`}>
            <div className="mc-tooltip whitespace-nowrap shadow-[4px_4px_0_rgba(0,0,0,0.5)] border-2 border-[#2d0a6b] bg-[#100010] p-2">
                <p className="text-white text-[10px] mb-1 font-minecraft drop-shadow-md leading-none">
                    {item.name}
                </p>
                <p className="text-blue-400 text-[8px] italic font-minecraft leading-none">
                    minecraft:{item.id} <br />(Sprite ID: {item.index})
                </p>
            </div>
        </div>
    </div>
));
ItemGridCell.displayName = 'ItemGridCell';

export const ItemBrowser = ({ onSelectItem }: ItemBrowserProps) => {
    const [query, setQuery] = useState("");

    const allItems = useMemo(() => Array.isArray(minecraftData) ? minecraftData : (minecraftData.items || []), []);

    const results = useMemo(() => {
        const search = query.toLowerCase().trim();
        if (!search) return allItems;
        return allItems.filter((item: any) =>
            (item?.id?.toLowerCase() || "").includes(search) ||
            (item?.name?.toLowerCase() || "").includes(search)
        );
    }, [query, allItems]);

    return (
        <div className="p-5 bg-[#0a0a0a]/80 backdrop-blur-xl border border-white/10 rounded-2xl shadow-2xl flex flex-col h-[calc(100vh-120px)] relative overflow-hidden">
            <div className="mb-5">
                <h2 className="text-white font-semibold text-sm uppercase tracking-widest flex items-center gap-2 opacity-90">
                    <span className="w-2 h-2 bg-yellow-500 rounded-full shadow-[0_0_10px_rgba(234,179,8,0.5)]"></span>
                    Catálogo de Ítems
                </h2>
                <p className="text-zinc-500 text-[10px] uppercase mt-1 tracking-wider">{results.length} Ítems disponibles</p>
            </div>

            <div className="relative mb-5 group">
                <input
                    type="text"
                    className="w-full p-3.5 pl-10 bg-black/50 border border-white/10 rounded-xl text-white outline-none focus:border-yellow-500/50 transition-all text-xs font-mono placeholder:text-zinc-600 shadow-inner"
                    placeholder="Buscar (ej. diamond_sword)"
                    value={query}
                    onChange={(e) => setQuery(e.target.value)}
                />
                <span className="absolute left-3.5 top-1/2 -translate-y-1/2 text-zinc-500 group-focus-within:text-yellow-500 transition-colors">🔍</span>
            </div>

            <div className="grid grid-cols-5 gap-2 overflow-y-auto custom-scrollbar pr-2 flex-grow align-start content-start pb-4">
                {results.map((item: any, idx: number) => (
                    <ItemGridCell key={`${item.id}-${idx}`} item={item} idx={idx} onSelect={onSelectItem} />
                ))}
            </div>
        </div>
    );
};
