"use client";

import React from 'react';

interface MCItem3DProps {
    spriteIndex: number; // CAMBIO CLAVE: Nombre específico para evitar bugs
    size?: number;
}

export const MCItem3D = ({ spriteIndex, size = 32 }: MCItem3DProps) => {
    const COLS = 32;
    // Si viene nulo o no existe, forzamos a 0
    const safeIndex = Number(spriteIndex) >= 0 ? Number(spriteIndex) : 0;

    const row = Math.floor(safeIndex / COLS);
    const col = safeIndex % COLS;

    return (
        <div
            style={{
                width: `${size}px`,
                height: `${size}px`,
                // Usamos tu URL obligatoria
                backgroundImage: `url('https://builder.nohaxito.xyz/sprites.webp')`,

                // Matemática de píxeles EXACTA
                backgroundSize: `${COLS * size}px auto`,
                backgroundPosition: `-${col * size}px -${row * size}px`,

                imageRendering: 'pixelated',
                display: 'inline-block',
                pointerEvents: 'none',
                filter: 'drop-shadow(2px 2px 0px rgba(0,0,0,0.5))'
            }}
            className="transition-transform duration-200"
        />
    );
};
