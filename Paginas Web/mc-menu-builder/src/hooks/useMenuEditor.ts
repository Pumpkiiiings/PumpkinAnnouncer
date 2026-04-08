// src/hooks/useMenuEditor.ts
import { useState } from 'react';
import { PlaceholderToggle } from '@/components/PlaceholderToggle';

export const useMenuEditor = () => {
    const [menuConfig, setMenuConfig] = useState({
        title: "<green>Mi Nuevo Menú",
        size: 54,
        pluginType: 'DeluxeMenus', // Default
        items: {} // Usamos un objeto con el slot como llave: { "10": { id: 'stone', ... } }
    });

    const updateItem = (slot: number, data: any) => {
        setMenuConfig(prev => ({
            ...prev,
            items: { ...prev.items, [slot]: data }
        }));
    };

    return { menuConfig, setMenuConfig, updateItem };
};
