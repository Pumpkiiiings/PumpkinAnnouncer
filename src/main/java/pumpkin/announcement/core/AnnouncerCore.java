package pumpkin.announcement.core;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.title.Title;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;
import pumpkin.announcement.core.utils.CenterUtil;
import pumpkin.announcement.core.utils.ColorUtil;
import pumpkin.announcement.core.utils.GradientAnimator;
import pumpkin.announcement.core.utils.UpdateChecker;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

public class AnnouncerCore {

    public static final String VERSION = "2.4";
    private final String MODRINTH_SLUG = "pumpkinannouncer";
    private String latestVersion = null;
    private String releaseName = null;

    private final PumpkinPlatform platform;
    private final Map<String, Announcement> announcementMap = new HashMap<>();
    private final Map<String, String> lang = new HashMap<>();
    private int cooldown;

    private final List<String> announcementQueue = new ArrayList<>();
    private int queueIndex = 0;

    private final MiniMessage mm = MiniMessage.miniMessage();

    public AnnouncerCore(PumpkinPlatform platform) {
        this.platform = platform;
    }

    public void initUpdateChecker() {
        new UpdateChecker(VERSION, MODRINTH_SLUG).check().thenAccept(result -> {
            if (result != null) {
                latestVersion = result[0];
                releaseName = result[1];
                platform.getPluginLogger().info("A new update is available: v" + latestVersion);
            }
        });
    }

    public void notifyUpdate(Audience player) {
        if (latestVersion != null) {
            player.sendMessage(mm.deserialize("\n<gradient:gold:yellow><bold>🎃 PumpkinAnnouncer Update Available!</bold></gradient>"));
            player.sendMessage(mm.deserialize("<gray>Current version: <red>" + VERSION + " <gray>| New version: <green>" + latestVersion));
            player.sendMessage(mm.deserialize("<yellow>What's new:</yellow> <white>" + releaseName));
            player.sendMessage(mm.deserialize("<click:open_url:'https://modrinth.com/plugin/" + MODRINTH_SLUG + "'><hover:show_text:'<green>Click here to download the new version!'><yellow><bold>👉 [CLICK HERE TO DOWNLOAD] 👈</bold></yellow></hover></click>\n"));
        }
    }

    public void loadConfig() {
        String configFileName = platform.getConfigFileName();
        Path dataDir = platform.getPluginDataDirectory();
        File file = new File(dataDir.toFile(), configFileName);
        if (!file.getParentFile().exists()) file.getParentFile().mkdirs();

        if (!file.exists()) {
            try (InputStream in = getClass().getResourceAsStream("/" + configFileName)) {
                if (in != null) {
                    Files.copy(in, file.toPath());
                } else {
                    platform.getPluginLogger().warning("[PumpkinAnnouncer] Resource not found: " + configFileName);
                }
            } catch (IOException e) {
                platform.getPluginLogger().severe("Error creating " + configFileName);
            }
        }

        YamlConfigurationLoader loader = YamlConfigurationLoader.builder().path(file.toPath()).build();
        try {
            ConfigurationNode root = loader.load();
            announcementMap.clear();
            lang.clear();

            root.node("announcements").childrenMap().forEach((key, node) -> {
                try {
                    List<String> servers = node.node("servers").getList(String.class, List.of("global"));
                    List<String> worlds  = node.node("worlds").getList(String.class, List.of("global"));
                    List<String> lines   = node.node("lines").getList(String.class);
                    String sound = node.node("sound").getString("");

                    boolean abEnabled = false;
                    String abText = "";
                    int abDuration = 5;
                    if (node.node("actionbar").isMap()) {
                        abEnabled  = node.node("actionbar", "enabled").getBoolean(false);
                        abText     = node.node("actionbar", "text").getString("");
                        abDuration = node.node("actionbar", "duration-seconds").getInt(5);
                    } else {
                        abText    = node.node("actionbar").getString("");
                        abEnabled = !abText.isEmpty();
                    }
                    Announcement.ActionBarConfig abConfig = new Announcement.ActionBarConfig(abEnabled, abText, abDuration);

                    boolean bbEnabled  = node.node("bossbar", "enabled").getBoolean(false);
                    String bbText      = node.node("bossbar", "text").getString("");
                    String bbColor     = node.node("bossbar", "color").getString("BLUE");
                    String bbStyle     = node.node("bossbar", "style").getString("PROGRESS");
                    int bbDuration     = node.node("bossbar", "duration-seconds").getInt(10);
                    boolean bbDeplete  = node.node("bossbar", "deplete").getBoolean(true);
                    Announcement.AnimatedGradient bbAnim = readAnimatedGradient(node.node("bossbar", "animated"));
                    Announcement.BossBarConfig bbConfig = new Announcement.BossBarConfig(
                            bbEnabled, bbText, bbColor, bbStyle, bbDuration, bbDeplete, bbAnim);

                    Announcement.TitleConfig titleConfig = Announcement.TitleConfig.disabled();
                    if (!node.node("title").isNull() && node.node("title").isMap()) {
                        boolean titleEnabled = node.node("title", "enabled").getBoolean(false);
                        String titleText     = node.node("title", "title").getString("");
                        String subtitleText  = node.node("title", "subtitle").getString("");
                        int fadeIn  = node.node("title", "fade-in-ticks").getInt(10);
                        int stay    = node.node("title", "stay-ticks").getInt(60);
                        int fadeOut = node.node("title", "fade-out-ticks").getInt(10);
                        Announcement.AnimatedGradient titleAnim = readAnimatedGradient(node.node("title", "animated"));
                        titleConfig = new Announcement.TitleConfig(titleEnabled, titleText, subtitleText,
                                fadeIn, stay, fadeOut, titleAnim);
                    }

                    if (lines != null) {
                        announcementMap.put(key.toString(),
                                new Announcement(servers, worlds, lines, sound, abConfig, bbConfig, titleConfig));
                    }
                } catch (Exception e) {
                    platform.getPluginLogger().warning("Error in announcement: " + key + " — " + e.getMessage());
                }
            });

            root.node("messages").childrenMap().forEach((key, node) -> {
                String val = node.getString();
                if (val != null) lang.put(key.toString(), val);
            });

            cooldown = root.node("settings", "cooldown-seconds").getInt(60);
        } catch (IOException e) {
            platform.getPluginLogger().severe("Error reading " + configFileName);
        }
    }

    private Announcement.AnimatedGradient readAnimatedGradient(ConfigurationNode node) {
        try {
            if (node.isNull() || !node.isMap()) return Announcement.AnimatedGradient.disabled();
            boolean enabled = node.node("enabled").getBoolean(false);
            List<String> colors = node.node("gradient-colors").getList(String.class, List.of("#FFFFFF", "#AAAAAA"));
            int interval = node.node("frame-interval-ticks").getInt(4);
            return new Announcement.AnimatedGradient(enabled, colors, interval);
        } catch (Exception e) {
            return Announcement.AnimatedGradient.disabled();
        }
    }

    private void rebuildQueue() {
        announcementQueue.clear();
        announcementQueue.addAll(announcementMap.keySet());
        Collections.shuffle(announcementQueue);
        queueIndex = 0;
    }

    private Announcement nextAnnouncement() {
        if (announcementQueue.isEmpty() || queueIndex >= announcementQueue.size()) {
            rebuildQueue();
            if (announcementQueue.isEmpty()) return null;
        }
        return announcementMap.get(announcementQueue.get(queueIndex++));
    }

    public void startTask() {
        platform.cancelTask();
        if (announcementMap.isEmpty()) return;
        rebuildQueue();

        platform.scheduleTask(() -> {
            if (announcementMap.isEmpty()) return;
            broadcast(nextAnnouncement());
        }, cooldown > 0 ? cooldown : 60);
    }

    public void broadcast(Announcement ann) {
        if (ann == null) return;

        List<Audience> targets = new ArrayList<>();
        platform.getOnlinePlayers().forEach(player -> {
            boolean isGlobalServer = ann.servers().stream().anyMatch(s -> s.equalsIgnoreCase("global"));
            boolean matchServer = isGlobalServer || ann.servers().stream()
                    .anyMatch(srv -> srv.equalsIgnoreCase(platform.getServerName(player)));

            boolean isGlobalWorld = ann.worlds().stream().anyMatch(w -> w.equalsIgnoreCase("global"));
            boolean matchWorld = isGlobalWorld || ann.worlds().stream()
                    .anyMatch(w -> w.equalsIgnoreCase(platform.getWorldName(player)));

            if (matchServer && matchWorld) targets.add(player);
        });
        if (targets.isEmpty()) return;

        final Sound finalSound = parseSound(ann.sound());

        final BossBar finalBossBar = ann.bossbar().enabled() ? buildBossBar(ann.bossbar()) : null;

        targets.forEach(player -> {
            if (ann.lines() != null && !ann.lines().isEmpty()) {
                for (String line : ann.lines()) {
                    boolean center = line.toLowerCase().startsWith("[center]");
                    String raw = center ? line.substring(8).trim() : line;
                    raw = platform.parsePlaceholders(raw, player);
                    Component msg = parseMsg(raw);
                    if (center) msg = CenterUtil.centerComponent(msg);
                    player.sendMessage(msg);
                }
            }

            if (finalSound != null) player.playSound(finalSound);

            if (ann.actionbar().enabled() && !ann.actionbar().text().isEmpty()) {
                String abRaw = platform.parsePlaceholders(ann.actionbar().text(), player);
                Component abComp = parseMsg(abRaw);
                player.sendActionBar(abComp);
                for (int i = 1; i < ann.actionbar().durationSeconds(); i++) {
                    final Component fc = abComp;
                    platform.scheduleDelayedTask(() -> player.sendActionBar(fc), i);
                }
            }

            if (finalBossBar != null) player.showBossBar(finalBossBar);

            if (ann.title().enabled()) {
                sendTitleToPlayer(ann.title(), player);
            }
        });

        if (finalBossBar != null) {
            scheduleBossBarLifecycle(finalBossBar, ann.bossbar(), targets);
        }

        if (ann.title().enabled() && ann.title().animated().enabled()) {
            scheduleAnimatedTitle(ann.title(), targets);
        }
    }

    private BossBar buildBossBar(Announcement.BossBarConfig cfg) {
        BossBar.Color color = BossBar.Color.BLUE;
        try {
            color = BossBar.Color.valueOf(cfg.color().toUpperCase());
        } catch (IllegalArgumentException e) {
            platform.getPluginLogger().warning("[PumpkinAnnouncer] Invalid BossBar color: '" + cfg.color() + "'. Using BLUE.");
        }

        BossBar.Overlay overlay = BossBar.Overlay.PROGRESS;
        String styleName = cfg.style().toUpperCase();
        if (styleName.equals("SOLID")) styleName = "PROGRESS";
        try {
            overlay = BossBar.Overlay.valueOf(styleName);
        } catch (IllegalArgumentException e) {
            platform.getPluginLogger().warning("[PumpkinAnnouncer] Invalid BossBar style: '" + cfg.style() + "'. Using PROGRESS.");
        }

        Component nameComp;
        if (cfg.animated().enabled() && !cfg.animated().colors().isEmpty()) {
            nameComp = GradientAnimator.generateFrames(cfg.text(), cfg.animated().colors(), 1).get(0);
        } else {
            nameComp = parseMsg(cfg.text());
        }

        return BossBar.bossBar(nameComp, 1.0f, color, overlay);
    }

    private void scheduleBossBarLifecycle(BossBar bar, Announcement.BossBarConfig cfg, List<Audience> targets) {
        int duration = cfg.durationSeconds();
        boolean deplete = cfg.deplete();
        boolean animated = cfg.animated().enabled() && !cfg.animated().colors().isEmpty();
        int frameInterval = cfg.animated().frameIntervalTicks();

        List<Component> gradientFrames = animated
                ? GradientAnimator.generateFrames(cfg.text(), cfg.animated().colors(),
                Math.max(1, (duration * 20) / frameInterval))
                : List.of();

        int totalTicks = duration * 20;

        for (long tick = 1; tick <= totalTicks; tick++) {
            final long t = tick;
            final int frame = animated ? (int) ((t / frameInterval) % gradientFrames.size()) : 0;

            platform.scheduleDelayedTaskTicks(() -> {
                if (deplete) {
                    float remaining = 1.0f - ((float) t / totalTicks);
                    bar.progress(Math.max(0.0f, remaining));
                }
                if (animated) {
                    bar.name(gradientFrames.get(frame));
                }
                if (t == totalTicks) {
                    targets.forEach(p -> p.hideBossBar(bar));
                }
            }, tick);
        }
    }

    private void sendTitleToPlayer(Announcement.TitleConfig cfg, Audience player) {
        String titleRaw    = platform.parsePlaceholders(cfg.title(), player);
        String subtitleRaw = platform.parsePlaceholders(cfg.subtitle(), player);

        Component titleComp;
        if (cfg.animated().enabled() && !cfg.animated().colors().isEmpty()) {
            titleComp = GradientAnimator.generateFrames(titleRaw, cfg.animated().colors(), 1).get(0);
        } else {
            titleComp = parseMsg(titleRaw);
        }

        Component subtitleComp = parseMsg(subtitleRaw);

        Title.Times times = Title.Times.times(
                Duration.ofMillis(cfg.fadeInTicks() * 50L),
                Duration.ofMillis(cfg.stayTicks()   * 50L),
                Duration.ofMillis(cfg.fadeOutTicks() * 50L));

        player.showTitle(Title.title(titleComp, subtitleComp, times));
    }

    private void scheduleAnimatedTitle(Announcement.TitleConfig cfg, List<Audience> targets) {
        int frameInterval = cfg.animated().frameIntervalTicks();
        int totalTicks = cfg.fadeInTicks() + cfg.stayTicks() + cfg.fadeOutTicks();
        int frameCount = Math.max(1, totalTicks / frameInterval);

        Component subtitleComp = parseMsg(cfg.subtitle());

        Title.Times refreshTimes = Title.Times.times(
                Duration.ZERO,
                Duration.ofMillis((frameInterval + 4) * 50L),
                Duration.ZERO);

        List<Component> frames = GradientAnimator.generateFrames(cfg.title(), cfg.animated().colors(), frameCount);

        for (int i = 0; i < frameCount; i++) {
            final Component frame = frames.get(i);
            final boolean isFirst = (i == 0);
            final long delay = cfg.fadeInTicks() + (long) i * frameInterval;

            platform.scheduleDelayedTaskTicks(() -> {
                if (isFirst) return;
                Title title = Title.title(frame, subtitleComp, refreshTimes);
                targets.forEach(p -> p.showTitle(title));
            }, delay);
        }
    }

    private Sound parseSound(String rawSound) {
        if (rawSound == null || rawSound.isEmpty()) return null;
        String key = rawSound.toLowerCase();
        if (key.contains("_") && !key.contains(".")) key = key.replace('_', '.');
        if (!key.contains(":")) key = "minecraft:" + key;
        try {
            return Sound.sound(Key.key(key), Sound.Source.MASTER, 1f, 1f);
        } catch (Exception e) {
            return null;
        }
    }

    private Component parseMsg(String text) {
        return mm.deserialize(ColorUtil.translateAll(text));
    }

    public void executeCommand(Audience sender, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(parseMsg(lang.getOrDefault("help", "&6Use /pa reload, list or test <id>")));
            return;
        }

        switch (args[0].toLowerCase()) {
            case "reload" -> {
                loadConfig();
                startTask();
                sender.sendMessage(parseMsg(lang.getOrDefault("reload-success", "&aConfig loaded!")));
            }
            case "list" -> {
                sender.sendMessage(parseMsg(lang.getOrDefault("list-header", "&6&lLoaded announcements:")));
                announcementMap.forEach((id, ann) ->
                        sender.sendMessage(parseMsg("&e» &f" + id
                                + " &7(Servers: " + ann.servers() + ") &b(Worlds: " + ann.worlds() + ")")));
            }
            case "test" -> {
                if (args.length < 2) {
                    sender.sendMessage(parseMsg("&cUsage: /pa test <id>"));
                    return;
                }
                Announcement ann = announcementMap.get(args[1]);
                if (ann != null) {
                    broadcast(ann);
                } else {
                    sender.sendMessage(parseMsg(lang.getOrDefault("id-not-found", "&cThat announcement does not exist.")));
                }
            }
            default -> sender.sendMessage(parseMsg("&cUnknown command."));
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
            return announcementMap.keySet().stream()
                    .filter(id -> id.toLowerCase().startsWith(typing))
                    .collect(Collectors.toList());
        }
        return List.of();
    }
}
