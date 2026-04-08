export interface MenuItem {
    id: string;          // ej: 'diamond_sword'
    slot: number;        // 0-53
    title: string;       // Soporta MiniMessage
    lore: string[];      // Lista de líneas
    sound?: string;      // ID del sonido de MC
    commands: string[];  // Acciones al hacer clic
}

export interface MenuConfig {
    name: string;
    size: number;        // 9, 18, 27, 36, 45, 54
    plugin: 'ZMenu' | 'DeluxeMenus' | 'CommandPanels';
    items: MenuItem[];
}
