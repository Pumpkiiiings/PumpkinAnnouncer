"use client";

import React, { useState } from 'react';

interface ImportModalProps {
    isOpen: boolean;
    onClose: () => void;
    onImport: (code: string) => void;
}

export const ImportModal = ({ isOpen, onClose, onImport }: ImportModalProps) => {
    const [code, setCode] = useState("");

    if (!isOpen) return null;

    // El estilo "Minecraft Block" que te gustó
    const mcBtnBase = "relative font-minecraft transition-all duration-75 active:translate-y-[2px] active:shadow-none border-2 uppercase tracking-tight shadow-[inset_-2px_-4px_0px_rgba(0,0,0,0.3)]";

    return (
        <div className="fixed inset-0 z-[100] flex items-center justify-center p-4 bg-black/80 backdrop-blur-sm">
            <div className="bg-[#1e1e1e] w-full max-w-2xl border-2 border-[#373737] shadow-[0_0_50px_rgba(0,0,0,0.5)] rounded-lg overflow-hidden animate-in fade-in zoom-in duration-200">

                {/* Header del Modal */}
                <div className="bg-[#252525] p-4 border-b border-[#373737] flex justify-between items-center">
                    <h3 className="text-yellow-500 font-minecraft font-bold text-sm tracking-widest uppercase">Importar Configuración YAML</h3>
                    <button onClick={onClose} className="text-gray-500 hover:text-white transition-colors">
                        <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" /></svg>
                    </button>
                </div>

                {/* Body */}
                <div className="p-6">
                    <p className="text-gray-400 text-[10px] mb-3 font-minecraft uppercase tracking-wider">
                        {">"} Pega el contenido de tu archivo .yml
                    </p>
                    <textarea
                        value={code}
                        onChange={(e) => setCode(e.target.value)}
                        className="w-full h-80 bg-black/50 border border-[#373737] rounded p-4 font-mono text-sm text-blue-400 outline-none focus:border-yellow-600/50 transition-all resize-none"
                        placeholder="menu_title: '&8Mi Menú'..."
                    />
                </div>

                {/* Footer */}
                <div className="bg-[#252525] p-4 flex justify-end gap-3 border-t border-[#373737]">
                    <button
                        onClick={onClose}
                        className="px-4 py-2 text-xs font-minecraft font-bold text-gray-500 hover:text-white transition-colors uppercase"
                    >
                        Cancelar
                    </button>
                    <button
                        onClick={() => {
                            onImport(code);
                            setCode("");
                            onClose();
                        }}
                        className={`${mcBtnBase} bg-yellow-600 border-t-yellow-400 border-l-yellow-400 border-r-yellow-900 border-b-yellow-900 text-white px-6 py-2 text-xs`}
                        style={{ textShadow: '1px 1px 0px rgba(0,0,0,0.5)' }}
                    >
                        PROCESAR YAML
                    </button>
                </div>
            </div>
        </div>
    );
};
