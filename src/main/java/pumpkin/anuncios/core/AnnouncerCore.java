package pumpkin.anuncios.core;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;
import pumpkin.anuncios.core.utils.CenterUtil;
import pumpkin.anuncios.core.utils.ColorUtil;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

public class AnnouncerCore {
    private final PumpkinPlatform platform;
    private final Map<String, Announcement> announcementMap = new HashMap<>();
    private final Map<String, String> lang = new HashMap<>();
    private int cooldown;

    private final MiniMessage mm = MiniMessage.miniMessage();

    public AnnouncerCore(PumpkinPlatform platform) {
        this.platform = platform;
    }

    public void loadConfig() {
        Path dataDir = platform.getPluginDataDirectory();
        File file = new File(dataDir.toFile(), "config.yml");
        if (!file.getParentFile().exists()) file.getParentFile().mkdirs();
        if (!file.exists()) {
            try (InputStream in = getClass().getResourceAsStream("/config.yml")) {
                if (in != null) Files.copy(in, file.toPath());
            } catch (IOException e) { platform.getPluginLogger().severe("Error al crear config"); }
        }

        YamlConfigurationLoader loader = YamlConfigurationLoader.builder().path(file.toPath()).build();
        try {
            ConfigurationNode root = loader.load();
            announcementMap.clear();
            lang.clear();

            root.node("anuncios").childrenMap().forEach((key, node) -> {
                try {
                    List<String> servers = node.node("servers").getList(String.class, List.of("global"));
                    List<String> lines = node.node("lines").getList(String.class);
                    String sound = node.node("sound").getString("");

                    boolean abEnabled = false;
                    String abText = "";
                    int abDuration = 5;

                    if (node.node("actionbar").isMap()) { // Nuevo formato
                        abEnabled = node.node("actionbar", "enabled").getBoolean(false);
                        abText = node.node("actionbar", "text").getString("");
                        abDuration = node.node("actionbar", "duration-seconds").getInt(5);
                    } else { // Fallback por si usan el viejo formato
                        abText = node.node("actionbar").getString("");
                        abEnabled = !abText.isEmpty();
                    }
                    Announcement.ActionBarConfig abConfig = new Announcement.ActionBarConfig(abEnabled, abText, abDuration);

                    boolean bbEnabled = node.node("bossbar", "enabled").getBoolean(false);
                    String bbText = node.node("bossbar", "text").getString("");
                    String bbColor = node.node("bossbar", "color").getString("BLUE");
                    String bbStyle = node.node("bossbar", "style").getString("SOLID");
                    int bbDuration = node.node("bossbar", "duration-seconds").getInt(10);
                    boolean bbDeplete = node.node("bossbar", "deplete").getBoolean(true);

                    Announcement.BossBarConfig bbConfig = new Announcement.BossBarConfig(bbEnabled, bbText, bbColor, bbStyle, bbDuration, bbDeplete);

                    if (lines != null) {
                        announcementMap.put(key.toString(), new Announcement(servers, lines, sound, abConfig, bbConfig));
                    }
                } catch (Exception e) { platform.getPluginLogger().warning("Error en anuncio: " + key); }
            });

            root.node("messages").childrenMap().forEach((key, node) -> {
                String val = node.getString();
                if (val != null) lang.put(key.toString(), val);
            });

            cooldown = root.node("settings", "cooldown-seconds").getInt(60);
        } catch (IOException e) { platform.getPluginLogger().severe("Error al leer config"); }
    }

    public void startTask() {
        platform.cancelTask();
        if (announcementMap.isEmpty()) return;

        platform.scheduleTask(() -> {
            if (announcementMap.isEmpty()) return;
            List<String> keys = new ArrayList<>(announcementMap.keySet());
            String randomKey = keys.get(new Random().nextInt(keys.size()));
            broadcast(announcementMap.get(randomKey));
        }, cooldown > 0 ? cooldown : 60);
    }

    public void broadcast(Announcement ann) {
        if (ann == null) return;

        List<Audience> targets = new ArrayList<>();
        platform.getOnlinePlayers().forEach(player -> {
            boolean isGlobal = ann.servers().stream().anyMatch(s -> s.equalsIgnoreCase("global"));
            if (isGlobal || ann.servers().stream().anyMatch(srv -> srv.equalsIgnoreCase(platform.getServerName(player)))) {
                targets.add(player);
            }
        });

        if (targets.isEmpty()) return;

        List<Component> chatMessages = new ArrayList<>();
        if (ann.lines() != null && !ann.lines().isEmpty()) {
            for (String line : ann.lines()) {
                boolean shouldCenter = line.toLowerCase().startsWith("[center]");
                if (shouldCenter) line = line.substring(8).trim();

                Component msg = parseMsg(line);
                if (shouldCenter) msg = CenterUtil.centerComponent(msg);
                chatMessages.add(msg);
            }
        }

        Sound soundObj = null;
        if (ann.sound() != null && !ann.sound().isEmpty()) {
            String soundKey = ann.sound().toLowerCase();
            if (soundKey.contains("_") && !soundKey.contains(".")) soundKey = soundKey.replace('_', '.');
            if (!soundKey.contains(":")) soundKey = "minecraft:" + soundKey;

            try {
                soundObj = Sound.sound(Key.key(soundKey), Sound.Source.MASTER, 1f, 1f);
            } catch (Exception ignored) {}
        }

        final Component finalActionBar = (ann.actionbar().enabled() && !ann.actionbar().text().isEmpty())
                ? parseMsg(ann.actionbar().text()) : null;

        BossBar tempBossBar = null;
        if (ann.bossbar().enabled()) {
            try {
                BossBar.Color color = BossBar.Color.valueOf(ann.bossbar().color().toUpperCase());
                BossBar.Overlay overlay = BossBar.Overlay.valueOf(ann.bossbar().style().toUpperCase());
                tempBossBar = BossBar.bossBar(parseMsg(ann.bossbar().text()), 1.0f, color, overlay);
            } catch (Exception e) {
                tempBossBar = BossBar.bossBar(parseMsg(ann.bossbar().text()), 1.0f, BossBar.Color.BLUE, BossBar.Overlay.PROGRESS);
            }
        }
        final BossBar finalBossBar = tempBossBar;
        final Sound finalSound = soundObj;

        targets.forEach(player -> {
            if (!chatMessages.isEmpty()) chatMessages.forEach(player::sendMessage);
            if (finalSound != null) player.playSound(finalSound);
            if (finalActionBar != null) player.sendActionBar(finalActionBar);
            if (finalBossBar != null) player.showBossBar(finalBossBar);
        });

        if (finalActionBar != null && ann.actionbar().durationSeconds() > 1) {
            for (int i = 1; i < ann.actionbar().durationSeconds(); i++) {
                platform.scheduleDelayedTask(() -> targets.forEach(p -> p.sendActionBar(finalActionBar)), i);
            }
        }

        if (finalBossBar != null) {
            int duration = ann.bossbar().durationSeconds();
            boolean deplete = ann.bossbar().deplete();

            for (int i = 1; i <= duration; i++) {
                final int elapsed = i;
                platform.scheduleDelayedTask(() -> {
                    if (deplete) {
                        float remaining = 1.0f - ((float) elapsed / duration);
                        finalBossBar.progress(Math.max(0.0f, remaining));
                    }

                    if (elapsed == duration) {
                        targets.forEach(p -> p.hideBossBar(finalBossBar));
                    }
                }, i);
            }
        }
    }

    public void executeCommand(Audience sender, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(parseMsg(lang.getOrDefault("help", "&6Usa /pa reload, list o test <anuncio>")));
            return;
        }

        switch (args[0].toLowerCase()) {
            case "reload" -> {
                loadConfig();
                startTask();
                sender.sendMessage(parseMsg(lang.getOrDefault("reload-success", "&a¡Config v2.1 cargada!")));
            }
            case "list" -> {
                sender.sendMessage(parseMsg(lang.getOrDefault("list-header", "&6&lAnuncios en el sistema:")));
                announcementMap.forEach((id, ann) ->
                        sender.sendMessage(parseMsg("&e» &f" + id + " &7(" + ann.servers() + ")"))
                );
            }
            case "test" -> {
                if (args.length < 2) {
                    sender.sendMessage(parseMsg("&cUso: /pa test <nombre_anuncio>"));
                    return;
                }
                Announcement ann = announcementMap.get(args[1]);
                if (ann != null) {
                    broadcast(ann);
                } else {
                    sender.sendMessage(parseMsg(lang.getOrDefault("id-not-found", "&cEse anuncio no existe.")));
                }
            }
            default -> sender.sendMessage(parseMsg("&cComando desconocido."));
        }
    }

    public List<String> getTabCompletions(String[] args) {
        List<String> suggestions = new ArrayList<>();
        if (args.length == 0 || args.length == 1) {
            suggestions.addAll(List.of("reload", "list", "test"));
            String typing = args.length == 1 ? args[0].toLowerCase() : "";
            return suggestions.stream().filter(s -> s.startsWith(typing)).collect(Collectors.toList());
        } else if (args.length == 2 && args[0].equalsIgnoreCase("test")) {
            String typing = args[1].toLowerCase();
            return announcementMap.keySet().stream().filter(id -> id.toLowerCase().startsWith(typing)).collect(Collectors.toList());
        }
        return List.of();
    }

    private Component parseMsg(String text) {
        return mm.deserialize(ColorUtil.translateAll(text));
    }
}
