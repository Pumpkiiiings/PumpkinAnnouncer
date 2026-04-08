// src/lib/converters/zmenu.ts
export const toZMenu = (config: any) => {
    const items: any = {};

    Object.entries(config.items).forEach(([slot, data]: any) => {
        items[`item_${slot}`] = {
            slot: parseInt(slot),
            material: data.id.toUpperCase(),
            name: data.title, // ZMenu usa 'name' en lugar de 'display_name'
            lore: data.lore,
            // En ZMenu los sonidos suelen ir como acciones
            actions: {
                all: [
                    data.sound ? `sound: ${data.sound}` : null,
                    ...data.commands.map((cmd: string) => `player: ${cmd}`)
                ].filter(Boolean)
            }
        };
    });

    return {
        // Estructura base de un inventario en ZMenu
        title: config.title,
        size: config.size / 9, // ZMenu a veces usa filas (size 54 = 6 filas)
        items: items
    };
};
