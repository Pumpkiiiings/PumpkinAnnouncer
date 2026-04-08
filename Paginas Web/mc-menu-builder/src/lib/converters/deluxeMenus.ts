// src/lib/converters/deluxeMenus.ts
export const toDeluxeMenus = (config: any) => {
    const yamlItems: any = {};

    Object.entries(config.items).forEach(([slot, data]: any) => {
        yamlItems[`item_${slot}`] = {
            material: data.id.toUpperCase(),
            slot: parseInt(slot),
            display_name: data.title,
            lore: data.lore,
            left_click_commands: data.sound ? [`[sound] ${data.sound}`, ...data.commands] : data.commands
        };
    });

    return {
        menu_title: config.title,
        open_command: "menu",
        size: config.size,
        items: yamlItems
    };
};
