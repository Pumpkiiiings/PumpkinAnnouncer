# 🎃 Pumpkin Announcer 

![Velocity](https://img.shields.io/badge/Velocity-Ready-blue?style=for-the-badge)
![Paper](https://img.shields.io/badge/Paper-Ready-white?style=for-the-badge)
![Java](https://img.shields.io/badge/Java-17+-orange?style=for-the-badge&logo=java)
![Version](https://img.shields.io/badge/Version-v2.2-success?style=for-the-badge)

The ultimate automated announcement system, now natively supporting both **Paper** servers and **Velocity** proxies in a single, smart `.jar` file! 
PumpkinAnnouncer allows you to schedule highly immersive messages with full support for chat, ActionBars, BossBars, and custom sound effects.

## ✨ Features

- 🌐 **Cross-Platform (1 File):** Works seamlessly whether you drop it into your Paper `plugins` folder or your Velocity proxy. The plugin intelligently adapts to the platform!
- 🎨 **Universal Color Support:** Mix and match Legacy codes (`&a&l`), Bungee Hex (`&#FF0000`), and modern MiniMessage tags (`<gradient:red:blue>`) on the same line. The plugin automatically translates everything flawlessly.
- 🪧 **BossBars & ActionBars:** Grab your players' attention with animated, temporary boss bars and action bar messages paired with custom sounds.
- 🎯 **Multi-Server (Velocity):** Broadcast announcements to the entire network (`global`) or restrict them to specific servers (e.g., `["lobby", "survival"]`).
- 🌍 **Multi-World (Paper):** Filter announcements so they only appear to players in specific worlds (e.g., `["world_nether"]`).
- 📏 **Mathematical Centering:** Simply add the `[center]` tag at the beginning of any line to center it perfectly in the Minecraft chat. It calculates pixels and intelligently ignores color codes!
- 🖱️ **Interactive Chat:** Full support for `<click>` and `<hover>` tags to create clickable links and informative tooltips.
- ⌨️ **Smart Tab-Completion:** Press `TAB` while typing `/pa test` to instantly auto-complete the IDs of all your configured announcements.

---

## 📜 Commands & Permissions

| Command | Description | Permission |
| :--- | :--- | :--- |
| `/pa reload` | Reloads the configuration in real-time. | `pumpkin.admin` |
| `/pa list` | Displays a list of all loaded announcement IDs. | `pumpkin.admin` |
| `/pa test <id>` | Instantly previews a specific announcement. | `pumpkin.admin` |

*Tip: When using `/pa test`, press the `TAB` key to auto-complete available announcements!*

---

## 🛠️ Installation

1. Download the `PumpkinAnnouncer-2.2.jar` file.
2. Drop it into the `plugins` folder of your **Paper** server or **Velocity** proxy.
3. Restart your server/proxy.
4. Edit the newly generated `plugins/pumpkinannouncer/config.yml` file to your liking.
5. Run `/pa reload` in-game or from the console to apply your changes instantly.

---

## 📖 Documentation & Examples (`config.yml`)

The v2.2 configuration system allows you to fully customize every aspect of your announcements. Here are 4 of the most common setups (feel free to copy and paste them!).

### 1. Store Sale (Features BossBar, ActionBar & Sound)
A premium-tier announcement. It plays a "Level Up" sound, shows a title above the hotbar, and displays a BossBar that smoothly depletes (`deplete: true`) over 15 seconds.
```yaml
  store_sale:
    servers: ["global"]
    worlds: ["global"]
    sound: "ENTITY_PLAYER_LEVELUP"
    actionbar:
      enabled: true
      text: "<gradient:red:gold><bold>🔥 HUGE WEEKEND SALE - 50% OFF! 🔥</bold></gradient>"
      duration-seconds: 10
    bossbar:
      enabled: true
      text: "<gradient:red:gold><bold>WEEKEND SALE IS LIVE!</bold></gradient>"
      color: "RED"
      style: "SOLID"
      duration-seconds: 15
      deplete: true
    lines:
      - "[center]<gradient:red:gold><bold>🔥 WEEKEND SALE 🔥</bold></gradient>"
      - ""
      - "[center]<white>Get a <green><bold>50% DISCOUNT</bold></green> on all ranks and keys!"
      - "[center]<white>Support the server and get epic perks."
      - ""
      - "[center]<click:open_url:'https://store.yourserver.com'><hover:show_text:'<green>Click to visit the store!'><yellow><bold>👉 store.yourserver.com 👈</bold></yellow></hover></click>"
```

### 2. Voting Reminder (Velocity Server Filter)
If you are running Velocity, this announcement will **only** broadcast to the "survival" and "skyblock" servers. It utilizes an ActionBar but disables the BossBar.
```yaml
  voting:
    servers: ["survival", "skyblock"] 
    worlds: ["global"]
    sound: "ENTITY_EXPERIENCE_ORB_PICKUP"
    actionbar:
      enabled: true
      text: "<gold><bold>Vote for us and earn epic rewards!</bold></gold>"
      duration-seconds: 8
    bossbar:
      enabled: false
    lines:
      - "[center]<color:#FFD900><bold>SUPPORT THE SERVER</bold></color>"
      - ""
      - "[center]<white>Do you enjoy playing here? Help us grow!"
      - "[center]<white>Use <gold>/vote</gold> or click the link below."
      - "[center]<click:open_url:'https://vote.yourserver.com'><hover:show_text:'<yellow>Click to vote!'><underlined><aqua>vote.yourserver.com</aqua></underlined></hover></click>"
```

### 3. Danger Alert (Paper World Filter)
If you are running Paper, this is perfect. It will **only** appear to players who are currently in the Nether dimension. Features a Ghast warning sound and a segmented BossBar for a "timer" feel.
```yaml
  nether_warning:
    servers: ["global"]
    worlds: ["world_nether"] #[PAPER ONLY] Exclusive to this world
    sound: "ENTITY_GHAST_WARN"
    actionbar:
      enabled: false
    bossbar:
      enabled: true
      text: "<red><bold>⚠ DANGEROUS ZONE ⚠</bold></red>"
      color: "RED"
      style: "SEGMENTED_6" 
      duration-seconds: 10
      deplete: true
    lines:
      - "[center]<red><bold>⚠ CAUTION ⚠</bold></red>"
      - ""
      - "[center]<white>You are currently exploring the <color:#FF5733>Nether</color>!"
      - "[center]<white>Keep your inventory safe and watch out for lava."
      - "[center]<gray>» <white>PvP is <red>enabled</red> in this world."
```

### 4. Informational Announcement (Clean & Minimalist)
A simple staff recruitment announcement. No ActionBars, no BossBars, no sounds. Just perfectly centered chat text.
```yaml
  staff_recruitment:
    servers: ["global"]
    worlds: ["global"]
    sound: ""
    actionbar:
      enabled: false
    bossbar:
      enabled: false
    lines:
      - "[center]<color:#00FFD1><bold>WE ARE HIRING!</bold></color>"
      - ""
      - "[center]<white>Are you active and love helping others?"
      - "[center]<white>We are currently looking for <color:#00FFD1>Helpers</color> and <color:#FF8C00>Moderators</color>."
      - "[center]<gray>» <white>Apply here: <click:open_url:'https://apply.yourserver.com'><hover:show_text:'<gray>Good luck!'><underlined><color:#00FFD1>apply.yourserver.com</color></underlined></hover></click>"
```

---

## ⚙️ General Settings

At the very top of your `config.yml`, you can control the main timer and fully translate/customize all the plugin's command responses.
```yaml
settings:
  cooldown-seconds: 60

messages:
  help: "<gradient:gold:yellow><bold>PumpkinAnnouncer</bold></gradient>\n<gray>» <yellow>/pa reload <dark_gray>- <white>Reload settings\n<gray>» <yellow>/pa list <dark_gray>- <white>View all IDs\n<gray>» <yellow>/pa test <id> <dark_gray>- <white>Preview an announcement"
  reload-success: "<green>Reload complete! Everything is running smoothly."
  list-header: "<gold><bold>Currently Loaded Announcements:</bold></gold>"
  id-not-found: "<red>That announcement ID doesn't exist. Please check your config."
```
```
