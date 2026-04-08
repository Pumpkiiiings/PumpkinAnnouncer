// src/lib/parser.ts
import yaml from 'js-yaml';

export const parseInventoryYAML = (text: string) => {
    try {
        const data: any = yaml.load(text);
        const slots: Record<number, any> = {};
        let detectedPlugin = 'Unknown';

        // --- DETECTAR DELUXEMENUS ---
        if (data.items || data.menu_title) {
            detectedPlugin = 'DeluxeMenus';
            const items = data.items || {};
            Object.keys(items).forEach(key => {
                const item = items[key];
                const slotIdx = item.slot;
                if (slotIdx !== undefined) {
                    slots[slotIdx] = {
                        id: item.material?.toLowerCase() || 'stone',
                        title: item.display_name || '',
                        lore: item.lore || [],
                        sound: extractSound(item.left_click_commands || []),
                        commands: item.left_click_commands || []
                    };
                }
            });
        }
        // --- DETECTAR ZMENU ---
        else if (data.items && !data.menu_title) {
            detectedPlugin = 'ZMenu';
            Object.keys(data.items).forEach(key => {
                const item = data.items[key];
                if (item.slot !== undefined) {
                    slots[item.slot] = {
                        id: item.material?.toLowerCase() || 'stone',
                        title: item.name || '',
                        lore: item.lore || [],
                        sound: extractSoundFromActions(item.actions),
                        commands: item.actions?.all || []
                    };
                }
            });
        }

        return { slots, plugin: detectedPlugin, size: data.size || 54 };
    } catch (e) {
        console.error("Error al parsear YAML:", e);
        return null;
    }
};

// Helper para sacar el sonido de DeluxeMenus: [sound] ID
const extractSound = (cmds: string[]) => {
    const soundCmd = cmds.find(c => c.startsWith('[sound]'));
    return soundCmd ? soundCmd.replace('[sound] ', '').trim() : "";
};

// Helper para ZMenu: sound: ID
const extractSoundFromActions = (actions: any) => {
    const allActions = actions?.all || [];
    const soundAct = allActions.find((a: string) => a.startsWith('sound:'));
    return soundAct ? soundAct.replace('sound: ', '').trim() : "";
};
