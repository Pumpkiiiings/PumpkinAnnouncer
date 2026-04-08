"use client";

import React from 'react';
import { MCItem3D } from '@/components/MCItems3D';

interface SlotProps {
    index: number;
    item?: {
        id: string;
        title?: string;
        lore?: string[];
        spriteIndex?: number; // Propiedad que guarda qué textura usar
    };
    onSelect: (index: number) => void;
    onMouseEnter: (item: any) => void;
    onMouseLeave: () => void;
}

export const Slot = ({ index, item, onSelect, onMouseEnter, onMouseLeave }: SlotProps) => {
    return (
        <div
            onClick={() => onSelect(index)}
            onMouseEnter={() => item && onMouseEnter(item)}
            onMouseLeave={onMouseLeave}
            className="relative w-12 h-12 bg-[#8b8b8b] border-2 border-t-[#373737] border-l-[#373737] border-b-[#ffffff] border-r-[#ffffff] hover:bg-[#a0a0a0] cursor-pointer flex items-center justify-center group"
        >
            {item ? (
                <div className="z-10 pointer-events-none transform group-hover:scale-110">
                    {/* Le pasamos el spriteIndex, si falla, avisa visualmente usando el 0 */}
                    <MCItem3D spriteIndex={item.spriteIndex ?? 0} size={32} />
                </div>
            ) : (
                <span className="text-[10px] text-black/20 opacity-0 group-hover:opacity-100 font-minecraft select-none pointer-events-none transition-opacity">
                    {index}
                </span>
            )}
            <div className="absolute inset-0 bg-white/0 group-hover:bg-white/10 pointer-events-none transition-colors"></div>
        </div>
    );
};
