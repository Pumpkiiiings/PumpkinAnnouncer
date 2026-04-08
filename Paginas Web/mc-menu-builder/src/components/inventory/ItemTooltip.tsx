"use client";
import React, { useEffect, useState } from 'react';
import { usePlaceholder } from '@/app/context/PlaceholderContext';

interface TooltipProps {
    title: string;
    lore: string[];
    visible: boolean;
}

const MC_FORMATS: Record<string, { color?: string, weight?: string, style?: string, decoration?: string }> = {
    '&0': { color: '#000000' }, '&1': { color: '#0000AA' }, '&2': { color: '#00AA00' }, '&3': { color: '#00AAAA' },
    '&4': { color: '#AA0000' }, '&5': { color: '#AA00AA' }, '&6': { color: '#FFAA00' }, '&7': { color: '#AAAAAA' },
    '&8': { color: '#555555' }, '&9': { color: '#5555FF' }, '&a': { color: '#55FF55' }, '&b': { color: '#55FFFF' },
    '&c': { color: '#FF5555' }, '&d': { color: '#FF55FF' }, '&e': { color: '#FFFF55' }, '&f': { color: '#FFFFFF' },
    '<black>': { color: '#000000' }, '<dark_blue>': { color: '#0000AA' }, '<dark_green>': { color: '#00AA00' },
    '<dark_aqua>': { color: '#00AAAA' }, '<dark_red>': { color: '#AA0000' }, '<dark_purple>': { color: '#AA00AA' },
    '<gold>': { color: '#FFAA00' }, '<gray>': { color: '#AAAAAA' }, '<dark_gray>': { color: '#555555' },
    '<blue>': { color: '#5555FF' }, '<green>': { color: '#55FF55' }, '<aqua>': { color: '#55FFFF' },
    '<red>': { color: '#FF5555' }, '<light_purple>': { color: '#FF55FF' }, '<yellow>': { color: '#FFFF55' },
    '<white>': { color: '#FFFFFF' },
    '&l': { weight: 'bold' }, '<bold>': { weight: 'bold' }, '<b>': { weight: 'bold' },
    '&o': { style: 'italic' }, '<italic>': { style: 'italic' }, '<i>': { style: 'italic' },
    '&n': { decoration: 'underline' }, '<underlined>': { decoration: 'underline' }, '<u>': { decoration: 'underline' },
    '&m': { decoration: 'line-through' }, '<strikethrough>': { decoration: 'line-through' }, '<st>': { decoration: 'line-through' },
    '&r': { color: '#FFFFFF', weight: 'normal', style: 'normal', decoration: 'none' }, '<reset>': { color: '#FFFFFF', weight: 'normal', style: 'normal', decoration: 'none' }
};

export const ItemTooltip = ({ title, lore, visible }: TooltipProps) => {
    // Jalamos la función de limpieza de tu hook
    const { parseText } = usePlaceholder();
    const [position, setPosition] = useState({ x: 0, y: 0 });

    useEffect(() => {
        const handleMouseMove = (e: MouseEvent) => {
            const x = e.clientX + 18;
            const y = e.clientY - 12;
            const finalX = x + 250 > window.innerWidth ? e.clientX - 260 : x;
            setPosition({ x: finalX, y });
        };
        window.addEventListener('mousemove', handleMouseMove);
        return () => window.removeEventListener('mousemove', handleMouseMove);
    }, []);

    const renderFormattedText = (rawText: string, defaultColor: string) => {
        if (!rawText) return null;

        // 1. Reemplazamos los placeholders primero usando el hook
        const text = parseText(rawText);

        const parts = text.split(/(&#[0-9a-fA-F]{6}|<#[0-9a-fA-F]{6}>|&[0-9a-fA-FK-ORk-or]|<[^>]+>)/g);

        let currentState = {
            color: defaultColor,
            weight: 'normal',
            style: 'normal',
            decoration: 'none'
        };

        return parts.map((part, i) => {
            const lowPart = part.toLowerCase();

            if (part.startsWith('&#') || part.match(/<#[0-9a-fA-F]{6}>/)) {
                const hex = part.replace(/[&<>#]/g, '');
                currentState.color = `#${hex}`;
                return null;
            }

            if (MC_FORMATS[lowPart]) {
                const format = MC_FORMATS[lowPart];
                if (format.color) currentState.color = format.color;
                if (format.weight) currentState.weight = format.weight;
                if (format.style) currentState.style = format.style;
                if (format.decoration) currentState.decoration = format.decoration;
                return null;
            }

            if (part.startsWith('</')) return null;
            if (!part || part.startsWith('<') || part.startsWith('&')) return null;

            return (
                <span
                    key={i}
                    className="mc-shadow"
                    style={{
                        color: currentState.color,
                        fontWeight: currentState.weight,
                        fontStyle: currentState.style,
                        textDecoration: currentState.decoration,
                    }}
                >
                    {part}
                </span>
            );
        });
    };

    if (!visible || !title) return null;

    return (
        <div
            className="fixed z-[9999] pointer-events-none flex flex-col mc-tooltip min-w-max animate-in fade-in zoom-in-95 duration-75"
            style={{
                left: position.x,
                top: position.y,
                imageRendering: 'pixelated'
            }}
        >
            <div className="text-[18px] leading-none mb-2 tracking-wide">
                {renderFormattedText(title, '#55FFFF')}
            </div>

            <div className="flex flex-col gap-0.5">
                {lore.map((line, i) => (
                    <div key={i} className="text-[16px] leading-tight whitespace-nowrap">
                        {renderFormattedText(line, '#AAAAAA')}
                    </div>
                ))}
            </div>
        </div>
    );
};
