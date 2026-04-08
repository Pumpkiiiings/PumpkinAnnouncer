// src/lib/converters/commandPanels.ts
export const toCommandPanels = (config: any) => {
    const p_items: any = {};

    Object.entries(config.items).forEach(([slot, data]: any) => {
        p_items[`slot_${slot}`] = {
            item: `material:${data.id.toUpperCase()}`,
            slot: parseInt(slot),
            name: data.title,
            lore: data.lore,
            commands: [
                data.sound ? `sound: ${data.sound};1.0;1.0` : null,
                ...data.commands.map((cmd: string) => `player: ${cmd}`)
            ].filter(Boolean)
        };
    });

    return {
        [config.name.replace(/\s+/g, '_')]: {
            title: config.title,
            rows: config.size / 9,
            items: p_items
        }
    };
};
