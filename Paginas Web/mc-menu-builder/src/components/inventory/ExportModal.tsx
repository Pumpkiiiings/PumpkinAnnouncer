"use client";

import React, { useState } from 'react';

interface ExportModalProps {
    isOpen: boolean;
    onClose: () => void;
    code: string;
    plugin: string;
}

export const ExportModal = ({ isOpen, onClose, code, plugin }: ExportModalProps) => {
    const [copied, setCopied] = useState(false);

    if (!isOpen) return null;

    // Estilo de botón Minecraft que te gustó
    const mcBtnBase = "relative font-minecraft transition-all duration-75 active:translate-y-[2px] active:shadow-none border-2 uppercase tracking-tight shadow-[inset_-2px_-4px_0px_rgba(0,0,0,0.3)]";

    const copyToClipboard = () => {
        navigator.clipboard.writeText(code);
        setCopied(true);
        setTimeout(() => setCopied(false), 2000);
    };

    const downloadFile = () => {
        const element = document.createElement("a");
        const file = new Blob([code], { type: 'text/yaml' });
        element.href = URL.createObjectURL(file);
        element.download = `menu_${plugin.toLowerCase()}.yml`;
        document.body.appendChild(element);
        element.click();
    };

    return (
        <div className="fixed inset-0 z-[100] flex items-center justify-center p-4 bg-black/60 backdrop-blur-md animate-in fade-in duration-300">
            <div className="bg-[#1e1e1e] w-full max-w-3xl border border-gray-700 shadow-2xl rounded-xl overflow-hidden animate-in zoom-in-95 duration-200">

                {/* Header */}
                <div className="bg-[#252525] p-5 border-b border-gray-800 flex justify-between items-center">
                    <div>
                        <h3 className="text-green-500 font-minecraft font-bold text-sm tracking-widest uppercase">Exportación Lista</h3>
                        <p className="text-[10px] text-gray-500 font-minecraft mt-1 text-shadow-mc">FORMATO: {plugin.toUpperCase()} YAML</p>
                    </div>
                    <button onClick={onClose} className="text-gray-500 hover:text-white transition-all">
                        <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" /></svg>
                    </button>
                </div>

                {/* Code Display */}
                <div className="p-6">
                    <div className="relative group">
                        <div className="absolute top-3 right-3 flex gap-2">
                            <button
                                onClick={copyToClipboard}
                                className={`${mcBtnBase} ${copied ? 'bg-green-600 border-green-400' : 'bg-gray-800 border-gray-600'} text-white text-[10px] px-3 py-1`}
                                style={{ textShadow: '1px 1px 0px rgba(0,0,0,0.5)' }}
                            >
                                {copied ? '¡COPIADO!' : 'COPIAR CÓDIGO'}
                            </button>
                        </div>
                        <pre className="w-full h-[400px] bg-black/40 border border-gray-800 rounded-lg p-5 font-mono text-sm text-green-400 overflow-auto scrollbar-thin scrollbar-thumb-gray-800">
                            {code}
                        </pre>
                    </div>
                </div>

                {/* Footer */}
                <div className="bg-[#252525] p-5 flex justify-between items-center border-t border-gray-800">
                    <span className="text-[10px] text-gray-500 italic font-minecraft">Liric Mistaken v2.0 - Optimized Output</span>
                    <div className="flex gap-4">
                        <button
                            onClick={onClose}
                            className="px-6 py-2 text-xs font-minecraft font-bold text-gray-500 hover:text-white uppercase transition-colors"
                        >
                            Cerrar
                        </button>
                        <button
                            onClick={downloadFile}
                            className={`${mcBtnBase} bg-green-600 border-t-green-400 border-l-green-400 border-r-green-900 border-b-green-900 text-white px-8 py-2 text-xs flex items-center gap-2`}
                            style={{ textShadow: '1px 1px 0px rgba(0,0,0,0.5)' }}
                        >
                            <span>📥</span> Descargar .yml
                        </button>
                    </div>
                </div>
            </div>
            <style jsx>{`
                .text-shadow-mc { text-shadow: 1px 1px 0px rgba(0,0,0,0.8); }
            `}</style>
        </div>
    );
};
