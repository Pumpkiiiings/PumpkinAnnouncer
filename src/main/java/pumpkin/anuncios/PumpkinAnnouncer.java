package pumpkin.anuncios;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.scheduler.ScheduledTask;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;
import pumpkin.anuncios.commands.PumpkinCommand;
import pumpkin.anuncios.utils.CenterUtil;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

@Plugin(id = "pumpkinannouncer", name = "PumpkinAnnouncer", version = "2.1", authors = {"Pumpkingz"})
public class PumpkinAnnouncer {

    private final ProxyServer server;
    private final Logger logger;
    private final Path dataDirectory;
    private final Map<String, Announcement> announcementMap = new HashMap<>();
    private final Map<String, String> lang = new HashMap<>();
    private int cooldown;
    private ScheduledTask currentTask;

    public record Announcement(List<String> servers, List<String> lines) {}

    @Inject
    public PumpkinAnnouncer(ProxyServer server, Logger logger, @DataDirectory Path dataDirectory) {
        this.server = server;
        this.logger = logger;
        this.dataDirectory = dataDirectory;
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        loadConfig();
        startAnnouncerTask();

        // Registrar comando importado del otro paquete
        server.getCommandManager().register("pa", new PumpkinCommand(this, server));

        var mm = MiniMessage.miniMessage();
        var cs = server.getConsoleCommandSource();
        cs.sendMessage(mm.deserialize("\n<gold><bold>Pumpkin Announcer <yellow>v2.1\n<green>Rework: Comandos, Centrado y Autocompletado Activos\n"));
    }

    public void loadConfig() {
        File file = new File(dataDirectory.toFile(), "config.yml");
        if (!file.getParentFile().exists()) file.getParentFile().mkdirs();
        if (!file.exists()) {
            try (InputStream in = getClass().getResourceAsStream("/config.yml")) {
                if (in != null) Files.copy(in, file.toPath());
            } catch (IOException e) { logger.severe("Error al crear config"); }
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
                    if (lines != null) {
                        announcementMap.put(key.toString(), new Announcement(servers, lines));
                    }
                } catch (Exception e) { logger.warning("Error en anuncio: " + key); }
            });

            root.node("messages").childrenMap().forEach((key, node) -> {
                String val = node.getString();
                if (val != null) lang.put(key.toString(), val);
            });

            cooldown = root.node("settings", "cooldown-seconds").getInt(60);
        } catch (IOException e) { logger.severe("Error al leer config"); }
    }

    public void startAnnouncerTask() {
        if (currentTask != null) currentTask.cancel();
        if (announcementMap.isEmpty()) return;

        currentTask = server.getScheduler().buildTask(this, () -> {
                    if (announcementMap.isEmpty()) return;
                    List<String> keys = new ArrayList<>(announcementMap.keySet());
                    String randomKey = keys.get(new Random().nextInt(keys.size()));
                    broadcast(announcementMap.get(randomKey));
                })
                .repeat(cooldown > 0 ? cooldown : 60, TimeUnit.SECONDS)
                .schedule();
    }

    public void broadcast(Announcement announcement) {
        if (announcement == null) return;
        var mm = MiniMessage.miniMessage();

        for (String line : announcement.lines()) {
            boolean shouldCenter = false;

            // Detectar si la línea pide ser centrada
            if (line.toLowerCase().startsWith("[center]")) {
                shouldCenter = true;
                line = line.substring(8).trim(); // Quitar la etiqueta "[center]"
            }

            Component msg = mm.deserialize(line);
            if (shouldCenter) {
                msg = CenterUtil.centerComponent(msg);
            }

            final Component finalMsg = msg;

            server.getAllPlayers().forEach(player -> {
                boolean isGlobal = announcement.servers().stream().anyMatch(s -> s.equalsIgnoreCase("global"));
                if (isGlobal) {
                    player.sendMessage(finalMsg);
                } else {
                    player.getCurrentServer().ifPresent(s -> {
                        String current = s.getServerInfo().getName();
                        if (announcement.servers().stream().anyMatch(srv -> srv.equalsIgnoreCase(current))) {
                            player.sendMessage(finalMsg);
                        }
                    });
                }
            });
        }
    }

    public String getMsg(String key, String def) {
        return lang.getOrDefault(key, def);
    }

    public Map<String, Announcement> getAnnouncementMap() {
        return announcementMap;
    }
}
