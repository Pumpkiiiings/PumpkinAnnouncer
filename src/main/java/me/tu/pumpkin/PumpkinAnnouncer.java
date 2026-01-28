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
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

@Plugin(id = "pumpkinannouncer", name = "PumpkinAnnouncer", version = "1.8", authors = {"Pumpkingz"})
public class PumpkinAnnouncer {

    private final ProxyServer server;
    private final Logger logger;
    private final Path dataDirectory;
    private final Map<String, List<String>> announcementMap = new HashMap<>();
    private final Map<String, String> lang = new HashMap<>();
    private int cooldown;
    private ScheduledTask currentTask;

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

        // Banner limpio en consola usando MiniMessage
        var mm = MiniMessage.miniMessage();
        var cs = server.getConsoleCommandSource();

        cs.sendMessage(mm.deserialize(""));
        cs.sendMessage(mm.deserialize("<gold><bold>Pumpkin    <gray>|  <white>Version: <yellow>1.8"));
        cs.sendMessage(mm.deserialize("<gold><bold>Announcer  <gray>|  <white>Desarrollador: <gold>Pumpkingz"));
        cs.sendMessage(mm.deserialize("<dark_gray>--------------------------------------------"));
        cs.sendMessage(mm.deserialize("<yellow> > <white>Estado: <green>Activo"));
        cs.sendMessage(mm.deserialize("<yellow> > <white>Anuncios cargados: <yellow>" + announcementMap.size()));
        cs.sendMessage(mm.deserialize(""));
    }

    public void loadConfig() {
        File file = new File(dataDirectory.toFile(), "config.yml");
        if (!file.getParentFile().exists()) file.getParentFile().mkdirs();

        if (!file.exists()) {
            try (InputStream in = getClass().getResourceAsStream("/config.yml")) {
                if (in != null) Files.copy(in, file.toPath());
            } catch (IOException e) {
                logger.severe("No pude crear la config.");
            }
        }

        YamlConfigurationLoader loader = YamlConfigurationLoader.builder().path(file.toPath()).build();

        try {
            ConfigurationNode root = loader.load();
            announcementMap.clear();
            lang.clear();

            root.node("anuncios").childrenMap().forEach((key, node) -> {
                try {
                    List<String> lines = node.getList(String.class);
                    if (lines != null) announcementMap.put(key.toString(), lines);
                } catch (SerializationException e) {
                    logger.warning("Error en anuncio '" + key + "': " + e.getMessage());
                }
            });

            root.node("messages").childrenMap().forEach((key, node) -> {
                String val = node.getString();
                if (val != null) lang.put(key.toString(), val);
            });

            cooldown = root.node("settings", "cooldown-seconds").getInt(60);

        } catch (IOException e) {
            logger.severe("Error al leer la config.");
        }
    }

    private void startAnnouncerTask() {
        // MATAMOS cualquier tarea vieja para evitar duplicados al hacer reload
        if (currentTask != null) {
            currentTask.cancel();
        }

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

    // MÉTODO OPTIMIZADO: Manda el mensaje a todos de un solo golpe
    private void broadcast(List<String> lines) {
        if (lines == null || lines.isEmpty()) return;

        var mm = MiniMessage.miniMessage();
        for (String line : lines) {
            // Enviamos al servidor completo (Audience), no jugador por jugador
            server.sendMessage(mm.deserialize(line));
        }
    }

    private String getMsg(String key, String def) {
        return lang.getOrDefault(key, def);
    }

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
                case "reload":
                    loadConfig();
                    startAnnouncerTask();
                    invocation.source().sendMessage(mm.deserialize(getMsg("reload-success", "<green>¡Recargado al cien!")));
                    break;
                case "list":
                    invocation.source().sendMessage(mm.deserialize(getMsg("list-header", "<gold>Anuncios:")));
                    announcementMap.keySet().forEach(id ->
                            invocation.source().sendMessage(mm.deserialize("<yellow>» <white>" + id))
                    );
                    break;
                case "test":
                    if (args.length < 2) return;
                    String id = args[1];
                    if (announcementMap.containsKey(id)) {
                        broadcast(announcementMap.get(id));
                    } else {
                        invocation.source().sendMessage(mm.deserialize(getMsg("id-not-found", "<red>No existe.")));
                    }
                    break;
            }
        }
        @Override
        public boolean hasPermission(Invocation invocation) { return invocation.source().hasPermission("pumpkin.admin"); }
    }
}

