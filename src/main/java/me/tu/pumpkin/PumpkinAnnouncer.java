package me.tu.pumpkin;

import com.google.inject.Inject;
import com.velocitypowered.api.command.SimpleCommand;
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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

@Plugin(id = "pumpkinannouncer", name = "PumpkinAnnouncer", version = "2.0", authors = {"Pumpkingz"})
public class PumpkinAnnouncer {

    private final ProxyServer server;
    private final Logger logger;
    private final Path dataDirectory;
    private final Map<String, Announcement> announcementMap = new HashMap<>();
    private final Map<String, String> lang = new HashMap<>();
    private int cooldown;
    private ScheduledTask currentTask;

    // Clase interna para manejar la nueva estructura
    private record Announcement(List<String> servers, List<String> lines) {}

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
        server.getCommandManager().register("pa", new PumpkinCommand());

        var mm = MiniMessage.miniMessage();
        var cs = server.getConsoleCommandSource();
        cs.sendMessage(mm.deserialize("\n<gold><bold>Pumpkin Announcer <yellow>v2.0\n<green>Modo Multi-Server Activo\n"));
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

    private void startAnnouncerTask() {
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

    private void broadcast(Announcement announcement) {
        if (announcement == null) return;
        var mm = MiniMessage.miniMessage();

        for (String line : announcement.lines()) {
            Component msg = mm.deserialize(line);
            server.getAllPlayers().forEach(player -> {
                // Lógica de filtrado por servidor
                boolean isGlobal = announcement.servers().stream().anyMatch(s -> s.equalsIgnoreCase("global"));
                if (isGlobal) {
                    player.sendMessage(msg);
                } else {
                    player.getCurrentServer().ifPresent(s -> {
                        String current = s.getServerInfo().getName();
                        if (announcement.servers().stream().anyMatch(srv -> srv.equalsIgnoreCase(current))) {
                            player.sendMessage(msg);
                        }
                    });
                }
            });
        }
    }

    private String getMsg(String key, String def) { return lang.getOrDefault(key, def); }

    private class PumpkinCommand implements SimpleCommand {
        @Override
        public void execute(Invocation invocation) {
            String[] args = invocation.arguments();
            var mm = MiniMessage.miniMessage();
            if (args.length == 0) {
                invocation.source().sendMessage(mm.deserialize(getMsg("help", "<gold>Usa /pa reload, list o test")));
                return;
            }

            switch (args[0].toLowerCase()) {
                case "reload" -> {
                    loadConfig();
                    startAnnouncerTask();
                    invocation.source().sendMessage(mm.deserialize(getMsg("reload-success", "<green>¡Config v2.0 cargada!")));
                }
                case "list" -> {
                    invocation.source().sendMessage(mm.deserialize("<gold>Anuncios:"));
                    announcementMap.forEach((id, ann) -> invocation.source().sendMessage(mm.deserialize("<yellow>» <white>" + id + " <gray>(" + ann.servers() + ")")));
                }
                case "test" -> {
                    if (args.length < 2) return;
                    Announcement ann = announcementMap.get(args[1]);
                    if (ann != null) broadcast(ann);
                }
            }
        }
        @Override
        public boolean hasPermission(Invocation invocation) { return invocation.source().hasPermission("pumpkin.admin"); }
    }
}

