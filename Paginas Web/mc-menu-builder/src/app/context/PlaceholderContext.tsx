"use client";
import React, { createContext, useContext, useState, useCallback } from 'react';

// 1. EL MAPA MASIVO ESTÁTICO
// Aquí van todos los placeholders que NO requieren parámetros dinámicos.
const MOCK_PAPI: Record<string, string> = {
    // === VAULT ECONOMY (Static) ===
    '%vault_eco_balance%': '1250500.5',
    '%vault_eco_balance_fixed%': '1250500.50',
    '%vault_eco_balance_formatted%': '$1,250,500.50',
    '%vault_eco_balance_commas%': '1,250,500',

    // === VAULT PERMISSIONS / CHAT (Static) ===
    '%vault_group%': 'owner',
    '%vault_group_capital%': 'Owner',
    '%vault_groups%': 'owner, vip, member',
    '%vault_groups_capital%': 'Owner, Vip, Member',
    '%vault_prefix%': '&8[&6Owner&8] &f',
    '%vault_suffix%': '&f',
    '%vault_groupprefix%': '&8[&6Owner&8] &f',
    '%vault_groupsuffix%': '&f',

    // === BUNGEE ===
    '%bungee_total%': '450',

    // === PLAYER ARMOR & INVENTORY ===
    '%player_allow_flight%': 'no',
    '%player_armor_helmet_name%': 'Diamond Helmet',
    '%player_armor_helmet_data%': '0',
    '%player_armor_helmet_durability%': '363',
    '%player_armor_chestplate_name%': 'Diamond Chestplate',
    '%player_armor_chestplate_data%': '0',
    '%player_armor_chestplate_durability%': '528',
    '%player_armor_leggings_name%': 'Diamond Leggings',
    '%player_armor_leggings_data%': '0',
    '%player_armor_leggings_durability%': '495',
    '%player_armor_boots_name%': 'Diamond Boots',
    '%player_armor_boots_data%': '0',
    '%player_armor_boots_durability%': '429',
    '%player_has_empty_slot%': 'yes',
    '%player_empty_slots%': '12',
    '%player_item_in_hand%': 'DIAMOND_SWORD',
    '%player_item_in_hand_name%': 'Excalibur',
    '%player_item_in_hand_data%': '0',
    '%player_item_in_hand_durability%': '1500',
    '%player_item_in_offhand%': 'SHIELD',
    '%player_item_in_offhand_name%': 'Shield',
    '%player_item_in_offhand_data%': '0',
    '%player_item_in_offhand_durability%': '336',

    // === PLAYER LOCATION & MOVEMENT ===
    '%player_bed_x%': '150',
    '%player_bed_y%': '64',
    '%player_bed_z%': '-300',
    '%player_bed_world%': 'world',
    '%player_biome%': 'plains',
    '%player_biome_capitalized%': 'Plains',
    '%player_block_underneath%': 'GRASS_BLOCK',
    '%player_compass_world%': 'world',
    '%player_compass_x%': '0',
    '%player_compass_y%': '64',
    '%player_compass_z%': '0',
    '%player_direction%': 'North',
    '%player_direction_xz%': 'N',
    '%player_fly_speed%': '0.1',
    '%player_walk_speed%': '0.2',
    '%player_world%': 'world',
    '%player_world_type%': 'NORMAL',
    '%player_world_time_12%': '12:00 PM',
    '%player_world_time_24%': '12:00',
    '%player_x%': '120.5',
    '%player_y%': '65.0',
    '%player_z%': '-250.3',
    '%player_yaw%': '90.0',
    '%player_pitch%': '0.0',

    // === PLAYER STATS & INFO ===
    '%player_name%': 'Steve',
    '%player_custom_name%': 'Steve_Pro',
    '%player_displayname%': '&#FFF26FSteve_Pro',
    '%player_list_name%': 'Steve_Pro',
    '%player_uuid%': '123e4567-e89b-12d3-a456-426614174000',
    '%player_ip%': '192.168.1.100',
    '%player_colored_ping%': '&a15',
    '%player_ping%': '15',
    '%player_locale%': 'es_ES',
    '%player_locale_display_name%': 'Español',
    '%player_locale_short%': 'es',
    '%player_locale_country%': 'ES',
    '%player_locale_display_country%': 'España',
    '%player_first_join_date%': '01/01/2023',
    '%player_first_played%': '1672531200000',
    '%player_first_join%': '01/01/2023',
    '%player_first_played_formatted%': '01 Jan 2023',
    '%player_last_played%': '1698765432000',
    '%player_last_join%': '01/10/2023',
    '%player_last_played_formatted%': '01 Oct 2023',
    '%player_last_join_date%': '01/10/2023',
    '%player_has_played_before%': 'yes',
    '%player_online%': 'yes',

    // === PLAYER STATES ===
    '%player_can_pickup_items%': 'yes',
    '%player_gamemode%': 'SURVIVAL',
    '%player_is_whitelisted%': 'yes',
    '%player_is_banned%': 'no',
    '%player_is_flying%': 'no',
    '%player_is_sneaking%': 'no',
    '%player_is_sprinting%': 'yes',
    '%player_is_sleeping%': 'no',
    '%player_is_inside_vehicle%': 'no',
    '%player_is_op%': 'yes',

    // === PLAYER COMBAT, HEALTH & EXP ===
    '%player_current_exp%': '125',
    '%player_exp%': '125',
    '%player_exp_to_level%': '50',
    '%player_total_exp%': '5430',
    '%player_level%': '42',
    '%player_food_level%': '20',
    '%player_saturation%': '5.0',
    '%player_health%': '20.0',
    '%player_health_rounded%': '20',
    '%player_health_scale%': '20',
    '%player_max_health%': '20.0',
    '%player_max_health_rounded%': '20',
    '%player_has_health_boost%': 'no',
    '%player_health_boost%': '0',
    '%player_absorption%': '0',
    '%player_last_damage%': '2.0',
    '%player_max_no_damage_ticks%': '20',
    '%player_no_damage_ticks%': '0',
    '%player_max_air%': '300',
    '%player_remaining_air%': '300',

    // === PLAYER TIME LIVED ===
    '%player_minutes_lived%': '120',
    '%player_seconds_lived%': '7200',
    '%player_ticks_lived%': '144000',
    '%player_sleep_ticks%': '0',
    '%player_time%': '6000',
    '%player_time_offset%': '0',
    '%player_thunder_duration%': '0',
    '%player_weather_duration%': '12000',
    '%player_light_level%': '15',

    // === STATISTICS (STATIC) ===
    '%statistic_mob_kills%': '450',
    '%statistic_mine_block%': '15420',
    '%statistic_use_item%': '8400',
    '%statistic_break_item%': '120',
    '%statistic_craft_item%': '3200',
    '%statistic_ticks_played%': '1728000',
    '%statistic_seconds_played%': '86400',
    '%statistic_minutes_played%': '1440',
    '%statistic_hours_played%': '24',
    '%statistic_days_played%': '1',
    '%statistic_time_played%': '1d 0h 0m',
    '%statistic_time_played:seconds%': '86400',
    '%statistic_time_played:minutes%': '1440',
    '%statistic_time_played:hours%': '24',
    '%statistic_time_played:days%': '1',
    '%statistic_animals_bred%': '45',
    '%statistic_armor_cleaned%': '2',
    '%statistic_banner_cleaned%': '1',
    '%statistic_beacon_interacted%': '10',
    '%statistic_boat_one_cm%': '150000',
    '%statistic_brewingstand_interaction%': '25',
    '%statistic_cake_slices_eaten%': '5',
    '%statistic_cauldron_filled%': '8',
    '%statistic_cauldron_used%': '12',
    '%statistic_chest_opened%': '1450',
    '%statistic_climb_one_cm%': '8500',
    '%statistic_crafting_table_interaction%': '340',
    '%statistic_crouch_one_cm%': '25000',
    '%statistic_damage_dealt%': '15000',
    '%statistic_damage_taken%': '4500',
    '%statistic_deaths%': '12',
    '%statistic_dispenser_inspected%': '4',
    '%statistic_dive_one_cm%': '12000',
    '%statistic_drop%': '530',
    '%statistic_dropper_inspected%': '2',
    '%statistic_enderchest_opened%': '85',
    '%statistic_fall_one_cm%': '34000',
    '%statistic_fish_caught%': '56',
    '%statistic_flower_potted%': '3',
    '%statistic_fly_one_cm%': '850000',
    '%statistic_furnace_interaction%': '420',
    '%statistic_hopper_inspected%': '15',
    '%statistic_horse_one_cm%': '45000',
    '%statistic_item_enchanted%': '24',
    '%statistic_jump%': '15400',
    '%statistic_junk_fished%': '8',
    '%statistic_leave_game%': '45',
    '%statistic_minecart_one_cm%': '12000',
    '%statistic_noteblock_played%': '50',
    '%statistic_noteblock_tuned%': '10',
    '%statistic_pig_one_cm%': '0',
    '%statistic_player_kills%': '5',
    '%statistic_record_played%': '2',
    '%statistic_sprint_one_cm%': '650000',
    '%statistic_swim_one_cm%': '45000',
    '%statistic_talked_to_villager%': '120',
    '%statistic_time_since_death%': '5h 20m',
    '%statistic_ticks_since_death%': '384000',
    '%statistic_seconds_since_death%': '19200',
    '%statistic_minutes_since_death%': '320',
    '%statistic_hours_since_death%': '5',
    '%statistic_days_since_death%': '0',
    '%statistic_traded_with_villager%': '85',
    '%statistic_trapped_chest_triggered%': '5',
    '%statistic_walk_one_cm%': '1250000',
    '%statistic_sleep_in_bed%': '14',
    '%statistic_sneak_time%': '45m',
    '%statistic_aviate_one_cm%': '450000',

    // === SERVER INFO ===
    '%server_name%': 'MiSuperServer',
    '%server_online%': '124',
    '%server_version%': '1.20.4',
    '%server_max_players%': '200',
    '%server_unique_joins%': '5420',
    '%server_uptime%': '12d 4h 30m',
    '%server_ram_used%': '4096',
    '%server_ram_free%': '4096',
    '%server_ram_total%': '8192',
    '%server_ram_max%': '8192',
    '%server_tps%': '20.0',
    '%server_tps_1%': '20.0',
    '%server_tps_5%': '19.9',
    '%server_tps_15%': '19.9',
    '%server_tps_1_colored%': '&a20.0',
    '%server_tps_5_colored%': '&a19.9',
    '%server_tps_15_colored%': '&a19.9',
    '%server_has_whitelist%': 'false',
    '%server_total_chunks%': '15420',
    '%server_total_living_entities%': '2450',
    '%server_total_entities%': '3100',

    // === LUCKPERMS (STATIC) ===
    '%luckperms_prefix%': '&8[&6Owner&8] &f',
    '%luckperms_suffix%': ' &7[Pro]',
    '%luckperms_groups%': 'admin, vip, default',
    '%luckperms_inherited_groups%': 'admin, vip, default',
    '%luckperms_primary_group_name%': 'admin',
    '%luckperms_highest_group_by_weight%': 'admin',
    '%luckperms_lowest_group_by_weight%': 'default',
    '%luckperms_highest_inherited_group_by_weight%': 'admin',
    '%luckperms_lowest_inherited_group_by_weight%': 'default',
};

interface PlaceholderContextType {
    previewRealValues: boolean;
    setPreviewRealValues: (val: boolean) => void;
    parseText: (text: string) => string;
}

const PlaceholderContext = createContext<PlaceholderContextType | undefined>(undefined);

export const PlaceholderProvider = ({ children }: { children: React.ReactNode }) => {
    const [previewRealValues, setPreviewRealValues] = useState(true);

    // 2. LÓGICA DINÁMICA
    // Atrapa los que tienen "<argumentos>" o nombres variables.
    const parseText = useCallback((text: string): string => {
        if (!previewRealValues || !text) return text;

        return text.replace(/%[a-z0-9_<>:.\- ]+%/gi, (match) => {
            const lowMatch = match.toLowerCase();

            // 1. Si está en nuestro mapa estático (exacto), lo cambiamos.
            if (MOCK_PAPI[lowMatch]) return MOCK_PAPI[lowMatch];

            // 2. Lógica dinámica para los placeholders parametrizados:

            // --- VAULT ECONOMY ---
            // Detecta algo como %vault_eco_balance_2dp%
            if (lowMatch.includes('vault_eco_balance_') && lowMatch.includes('dp')) {
                return '1250500.50';
            }

            // --- VAULT PERMISSIONS ---
            if (lowMatch.startsWith('%vault_groupprefix_')) return '&c[Admin]';
            if (lowMatch.startsWith('%vault_groupsuffix_')) return '&f';
            if (lowMatch.startsWith('%vault_hasgroup_')) return 'yes';
            if (lowMatch.startsWith('%vault_inprimarygroup_')) return 'yes';

            // --- BungeeCord ---
            if (lowMatch.startsWith('%bungee_')) return '120';

            // --- Player ---
            if (lowMatch.startsWith('%player_has_potioneffect_')) return 'yes';
            if (lowMatch.startsWith('%player_has_permission_')) return 'yes';
            if (lowMatch.startsWith('%player_ping_')) return '25';
            if (lowMatch.startsWith('%player_item_in_hand_level_')) return '5';
            if (lowMatch.startsWith('%player_item_in_offhand_level_')) return '3';

            // --- Statistics ---
            if (lowMatch.startsWith('%statistic_mine_block:')) return '2450';
            if (lowMatch.startsWith('%statistic_use_item:')) return '150';
            if (lowMatch.startsWith('%statistic_break_item:')) return '12';
            if (lowMatch.startsWith('%statistic_craft_item:')) return '64';
            if (lowMatch.startsWith('%statistic_kill_entity:')) return '300';
            if (lowMatch.startsWith('%statistic_entity_killed_by:')) return '2';

            // --- Server ---
            if (lowMatch.startsWith('%server_online_')) return '85';
            if (lowMatch.startsWith('%server_time_')) return '14:30:00';
            if (lowMatch.startsWith('%server_countdown_')) return '2d 14h 5m';

            // --- LuckPerms ---
            if (lowMatch.startsWith('%luckperms_meta_all_') || lowMatch.startsWith('%luckperms_meta_all%')) return 'value1, value2';
            if (lowMatch.startsWith('%luckperms_meta_') || lowMatch.startsWith('%luckperms_meta%')) return 'some_meta_value';
            if (lowMatch.startsWith('%luckperms_prefix_element_') || lowMatch.startsWith('%luckperms_prefix_element%')) return '&c[Admin]';
            if (lowMatch.startsWith('%luckperms_suffix_element_') || lowMatch.startsWith('%luckperms_suffix_element%')) return '&7[OG]';
            if (lowMatch.startsWith('%luckperms_context_') || lowMatch.startsWith('%luckperms_context%')) return 'global';

            // Permisos / Grupos
            if (lowMatch.startsWith('%luckperms_has_permission_') || lowMatch.startsWith('%luckperms_has_permission%')) return 'yes';
            if (lowMatch.startsWith('%luckperms_inherits_permission_') || lowMatch.startsWith('%luckperms_inherits_permission%')) return 'yes';
            if (lowMatch.startsWith('%luckperms_check_permission_') || lowMatch.startsWith('%luckperms_check_permission%')) return 'true';
            if (lowMatch.startsWith('%luckperms_in_group_') || lowMatch.startsWith('%luckperms_in_group%')) return 'yes';
            if (lowMatch.startsWith('%luckperms_inherits_group_') || lowMatch.startsWith('%luckperms_inherits_group%')) return 'yes';

            // Tracks
            if (lowMatch.startsWith('%luckperms_on_track_') || lowMatch.startsWith('%luckperms_on_track%')) return 'yes';
            if (lowMatch.startsWith('%luckperms_has_groups_on_track_') || lowMatch.startsWith('%luckperms_has_groups_on_track%')) return 'yes';
            if (lowMatch.startsWith('%luckperms_current_group_on_track_') || lowMatch.startsWith('%luckperms_current_group_on_track%')) return 'admin';
            if (lowMatch.startsWith('%luckperms_next_group_on_track_') || lowMatch.startsWith('%luckperms_next_group_on_track%')) return 'owner';
            if (lowMatch.startsWith('%luckperms_previous_group_on_track_') || lowMatch.startsWith('%luckperms_previous_group_on_track%')) return 'mod';
            if (lowMatch.startsWith('%luckperms_first_group_on_tracks_') || lowMatch.startsWith('%luckperms_first_group_on_tracks%')) return 'vip';
            if (lowMatch.startsWith('%luckperms_last_group_on_tracks_') || lowMatch.startsWith('%luckperms_last_group_on_tracks%')) return 'default';

            // Expiry
            if (lowMatch.startsWith('%luckperms_expiry_time_') || lowMatch.startsWith('%luckperms_expiry_time%')) return '10d 5h';
            if (lowMatch.startsWith('%luckperms_inherited_expiry_time_') || lowMatch.startsWith('%luckperms_inherited_expiry_time%')) return '10d 5h';
            if (lowMatch.startsWith('%luckperms_group_expiry_time_') || lowMatch.startsWith('%luckperms_group_expiry_time%')) return '30d 12h';
            if (lowMatch.startsWith('%luckperms_inherited_group_expiry_time_') || lowMatch.startsWith('%luckperms_inherited_group_expiry_time%')) return '30d 12h';

            // Si nada coincide, lo dejamos tal cual
            return match;
        });
    }, [previewRealValues]);

    return (
        <PlaceholderContext.Provider value={{ previewRealValues, setPreviewRealValues, parseText }}>
            {children}
        </PlaceholderContext.Provider>
    );
};

export const usePlaceholder = () => {
    const context = useContext(PlaceholderContext);
    if (!context) {
        throw new Error("usePlaceholder debe usarse dentro de un PlaceholderProvider, bro.");
    }
    return context;
};
