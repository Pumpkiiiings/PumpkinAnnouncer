"use client";
import React from 'react';
// 1. IMPORTANTE: Cambia la ruta hacia donde está tu contexto
import { usePlaceholder } from '@/app/context/PlaceholderContext';

export const PlaceholderToggle = () => {
    // 2. Ahora sí va a jalar el estado global, ¡arre!
    const { previewRealValues, setPreviewRealValues } = usePlaceholder();

    return (
        <div className="flex items-center gap-3">
            <span className="text-[10px] text-zinc-500 font-minecraft uppercase tracking-wider">
                Preview Mode
            </span>

            <button
                onClick={() => setPreviewRealValues(!previewRealValues)}
                className={`
                    relative px-4 py-1.5 font-minecraft text-[12px] border-2 
                    transition-all duration-75 active:translate-y-[2px]
                    ${previewRealValues
                        ? 'bg-[#3c8527] border-[#5db239] text-white shadow-[inset_-2px_-4px_0px_rgba(0,0,0,0.3)]'
                        : 'bg-[#4a4a4a] border-[#8b8b8b] text-[#bfbfbf] shadow-[inset_-2px_-4px_0px_rgba(0,0,0,0.5)]'}
                `}
                style={{
                    textShadow: '1px 1px 0px rgba(0,0,0,0.8)'
                }}
            >
                {previewRealValues ? 'VALORES REALES' : 'PLACEHOLDERS %'}
            </button>
        </div>
    );
};
