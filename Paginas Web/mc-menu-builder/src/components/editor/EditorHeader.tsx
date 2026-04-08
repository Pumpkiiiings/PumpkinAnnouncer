// src/components/Editor/EditorHeader.tsx
"use client"; // <--- No olvides esto porque el Toggle usa hooks
import React from 'react';
// Asegúrate de que la ruta sea la correcta, si está en src/components/
import { PlaceholderToggle } from '@/components/PlaceholderToggle';

export const EditorHeader = () => {
    return (
        <header className="flex h-16 w-full items-center justify-between px-6 bg-[#1e1e1e] border-b border-white/5 shrink-0">
            {/* Lado Izquierdo: Título y Versión */}
            <div className="flex items-center gap-4">
                <h1 className="text-white font-minecraft text-lg tracking-wide">
                    Menu Editor
                </h1>
                <span className="text-[10px] text-zinc-500 bg-zinc-800/80 px-2 py-0.5 rounded-sm border border-white/5 font-mono">
                    V1.0
                </span>
            </div>

            {/* Lado Derecho: Acciones y el Switch Maestro */}
            <div className="flex items-center gap-6">
                {/* Aquí pusimos el Toggle que controla los placeholders de todo el editor */}
                <div className="border-r border-white/10 pr-6 h-8 flex items-center">
                    <PlaceholderToggle />
                </div>

                {/* Botón de Guardar con estilo Minecraft azul */}
                <button
                    className="bg-[#3c59a6] hover:bg-[#4a69bd] border-2 border-[#54a0ff]/50 px-5 py-1.5 text-[12px] text-white font-minecraft shadow-[inset_-2px_-4px_0px_rgba(0,0,0,0.3)] transition-all active:translate-y-0.5"
                    style={{ textShadow: '1px 1px 0px rgba(0,0,0,0.5)' }}
                >
                    GUARDAR
                </button>
            </div>
        </header>
    );
};
