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

@Plugin(id = "pumpkinannouncer", name = "PumpkinAnnouncer", version = "1.8", authors = {"Bro"})
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

        // Usamos MiniMessage para que los colores se vean perrones en consola
        net.kyori.adventure.text.minimessage.MiniMessage mm = net.kyori.adventure.text.minimessage.MiniMessage.miniMessage();

        // Mandamos el mensaje al ConsoleSource para que no salga el prefijo de la clase
        invocationSource().sendMessage(mm.deserialize(""));
        invocationSource().sendMessage(mm.deserialize("<gold><bold>Pumpkin    <gray>|  <white>Version: <yellow>1.8"));
        invocationSource().sendMessage(mm.deserialize("<gold><bold>Announcer  <gray>|  <white>Desarrollador: <gold>Pumpkingz"));
        invocationSource().sendMessage(mm.deserialize("<dark_gray>--------------------------------------------"));
        invocationSource().sendMessage(mm.deserialize("<yellow> > <white>Estado: <green>Activo"));
        invocationSource().sendMessage(mm.deserialize("<yellow> > <white>Anuncios cargados: <yellow>" + announcementMap.size()));
        invocationSource().sendMessage(mm.deserialize(""));
    }

    private net.kyori.adventure.audience.Audience invocationSource() {
        return server.getConsoleCommandSource();
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

            // Limpiamos antes de cargar
            announcementMap.clear();
            lang.clear();

            // 1. Cargar Anuncios (Manejando la excepción de serialización)
            root.node("anuncios").childrenMap().forEach((key, node) -> {
                try {
                    List<String> lines = node.getList(String.class);
                    if (lines != null) announcementMap.put(key.toString(), lines);
                } catch (SerializationException e) {
                    logger.warning("Error cargando el anuncio '" + key + "': " + e.getMessage());
                }
            });

            // 2. Cargar Mensajes editables del sistema
            root.node("messages").childrenMap().forEach((key, node) -> {
                String val = node.getString();
                if (val != null) lang.put(key.toString(), val);
            });

            // 3. Settings
            cooldown = root.node("settings", "cooldown-seconds").getInt(60);

        } catch (IOException e) {
            logger.severe("Error al leer el archivo físico de la config, bro.");
        }
    }

    private void startAnnouncerTask() {
        if (currentTask != null) currentTask.cancel();
        currentTask = server.getScheduler().buildTask(this, () -> {
                    if (announcementMap.isEmpty()) return;
                    List<String> keys = new ArrayList<>(announcementMap.keySet());
                    String randomKey = keys.get(new Random().nextInt(keys.size()));
                    broadcast(announcementMap.get(randomKey));
                })
                .repeat(cooldown > 0 ? cooldown : 60, TimeUnit.SECONDS)
                .schedule();
    }

    private void broadcast(List<String> lines) {
        if (lines == null) return;
        lines.forEach(line -> {
            Component component = MiniMessage.miniMessage().deserialize(line);
            server.getAllPlayers().forEach(p -> p.sendMessage(component));
        });
    }

    private String getMsg(String key, String def) {
        return lang.getOrDefault(key, def);
    }

    private class PumpkinCommand implements SimpleCommand {
        @Override
        public void execute(Invocation invocation) {
            String[] args = invocation.arguments();
            if (args.length == 0) {
                invocation.source().sendMessage(MiniMessage.miniMessage().deserialize(getMsg("help", "<gold>Usa /pa reload, list o test")));
                return;
            }

            switch (args[0].toLowerCase()) {
                case "reload":
                    loadConfig();
                    startAnnouncerTask();
                    invocation.source().sendMessage(MiniMessage.miniMessage().deserialize(getMsg("reload-success", "<green>¡Todo recargado al cien!")));
                    break;
                case "list":
                    invocation.source().sendMessage(MiniMessage.miniMessage().deserialize(getMsg("list-header", "<gold>Anuncios disponibles:")));
                    announcementMap.keySet().forEach(id ->
                            invocation.source().sendMessage(MiniMessage.miniMessage().deserialize("<yellow>» <white>" + id))
                    );
                    break;
                case "test":
                    if (args.length < 2) return;
                    String id = args[1];
                    if (announcementMap.containsKey(id)) {
                        broadcast(announcementMap.get(id));
                    } else {
                        invocation.source().sendMessage(MiniMessage.miniMessage().deserialize(getMsg("id-not-found", "<red>Ese anuncio no existe.")));
                    }
                    break;
            }
        }
        @Override
        public boolean hasPermission(Invocation invocation) { return invocation.source().hasPermission("pumpkin.admin"); }
    }
}
