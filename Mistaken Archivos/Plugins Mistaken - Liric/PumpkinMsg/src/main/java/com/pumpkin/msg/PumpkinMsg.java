package com.pumpkin.msg;

import com.google.inject.Inject;
import com.pumpkin.msg.commands.*;
import com.pumpkin.msg.config.ConfigManager;
import com.pumpkin.msg.events.CommandSpyListener;
import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import org.slf4j.Logger;

import java.nio.file.Path;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Plugin(
        id = "pumpkinmsg",
        name = "PumpkinMsg",
        version = "1.0",
        authors = {"Pumpkingz"}
)
public class PumpkinMsg {

    private final ProxyServer server;
    private final Logger logger;
    private final ConfigManager configManager;

    private final Set<UUID> socialSpyUsers = ConcurrentHashMap.newKeySet();
    private final Set<UUID> msgDisabledUsers = ConcurrentHashMap.newKeySet();
    private final Map<UUID, Set<UUID>> ignoredPlayers = new ConcurrentHashMap<>();
    private final Map<UUID, UUID> lastMessaged = new ConcurrentHashMap<>();
    private final Map<UUID, UUID> spyTargets = new ConcurrentHashMap<>();
    // Mapa para el CommandSpy (Almacena el UUID del staff y el Servidor que está espiando, o "ALL")
    private final Map<UUID, String> cmdSpyUsers = new ConcurrentHashMap<>();

    @Inject
    public PumpkinMsg(ProxyServer server, Logger logger, @DataDirectory Path dataDirectory) {
        this.server = server;
        this.logger = logger;
        this.configManager = new ConfigManager(dataDirectory);
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        loadData();

        CommandManager cm = server.getCommandManager();
        cm.register(cm.metaBuilder("msg").aliases("w", "tell", "message").build(), new MsgCommand(this, server));
        cm.register(cm.metaBuilder("reply").aliases("r").build(), new ReplyCommand(this, server));
        cm.register(cm.metaBuilder("socialspy").aliases("spy").build(), new SpyCommand(this));
        cm.register(cm.metaBuilder("spycommands").aliases("cmdspy").build(), new CommandSpy(this));
        cm.register(cm.metaBuilder("ignore").build(), new IgnoreCommand(this, server));
        cm.register(cm.metaBuilder("togglemsg").aliases("tmsg", "pmtoggle").build(), new ToggleMsgCommand(this));
        cm.register(cm.metaBuilder("pumpkinreload").build(), new ReloadCommand(this));

        // Registramos el Listener de comandos espiados
        server.getEventManager().register(this, new CommandSpyListener(this));

        var console = server.getConsoleCommandSource();
        var mm = net.kyori.adventure.text.minimessage.MiniMessage.miniMessage();

        console.sendMessage(mm.deserialize(""));
        console.sendMessage(mm.deserialize("<#FF9500>▄▄▄▄▄▄▄                                         ▄▄▄      ▄▄▄  ▄▄▄▄▄▄▄  ▄▄▄▄▄▄▄  "));
        console.sendMessage(mm.deserialize("<#FF9500>███▀▀███▄                      ▄▄     ▀▀        ████▄  ▄████ █████▀▀▀ ███▀▀▀▀▀ "));
        console.sendMessage(mm.deserialize("<#FF9500>███▄▄███▀ ██ ██ ███▄███▄ ████▄ ██ ▄█▀ ██  ████▄ ███▀████▀███  ▀████▄  ███           "));
        console.sendMessage(mm.deserialize("<#E5C07B>███▀▀▀▀   ██ ██ ██ ██ ██ ██ ██ ████   ██  ██ ██ ███  ▀▀  ███    ▀████ ███  ███▀ "));
        console.sendMessage(mm.deserialize("<#E5C07B>███       ▀██▀█ ██ ██ ██ ████▀ ██ ▀█▄ ██▄ ██ ██ ███      ███ ███████▀ ▀██████▀"));
        console.sendMessage(mm.deserialize("<#E5C07B>                         ██     "));
        console.sendMessage(mm.deserialize("<#E5C07B>                         ▀▀    "));
        console.sendMessage(mm.deserialize(""));
        console.sendMessage(mm.deserialize("<#ABB2BF>  > <white>Desarrollador: <#FF9500>Pumpkingz"));
        console.sendMessage(mm.deserialize("<#ABB2BF>  > <white>Version: <#61AFEF>1.0-STABLE"));
        console.sendMessage(mm.deserialize("<#ABB2BF>  > <white>Estado: <#98C379>¡Listo con LuckPerms!"));
        console.sendMessage(mm.deserialize(""));
    }

    @Subscribe
    public void onProxyShutdown(ProxyShutdownEvent event) {
        saveData();
        logger.info("PumpkinMsg: Data successfully synchronized to disk.");
    }

    private void loadData() {
        try {
            this.socialSpyUsers.addAll(configManager.loadUUIDSet("spy_data.txt"));
            this.msgDisabledUsers.addAll(configManager.loadUUIDSet("toggled_msgs.txt"));
            this.ignoredPlayers.putAll(configManager.loadIgnoreMap());
            this.cmdSpyUsers.putAll(configManager.loadCmdSpy());

            logger.info("Persistence: Loaded {} spy users, {} toggled users, {} cmdspies and {} ignore lists.",
                    socialSpyUsers.size(), msgDisabledUsers.size(), cmdSpyUsers.size(), ignoredPlayers.size());
        } catch (Exception e) {
            logger.error("Critical error loading PumpkinMsg database:", e);
        }
    }

    public void saveData() {
        configManager.saveUUIDSet(socialSpyUsers, "spy_data.txt");
        configManager.saveUUIDSet(msgDisabledUsers, "toggled_msgs.txt");
        configManager.saveIgnoreMap(ignoredPlayers);
        configManager.saveCmdSpy(cmdSpyUsers);
    }

    /**
     * Utilidad para obtener el prefijo de LuckPerms convertido a Componente de Adventure.
     */
    public Component getPrefix(Player player) {
        try {
            LuckPerms api = LuckPermsProvider.get();
            User user = api.getUserManager().getUser(player.getUniqueId());
            if (user != null) {
                String prefix = user.getCachedData().getMetaData().getPrefix();
                if (prefix != null) {
                    return LegacyComponentSerializer.legacyAmpersand().deserialize(prefix);
                }
            }
        } catch (Throwable ignored) {
            // Falla silenciosamente si LuckPerms no está activo
        }
        return Component.empty();
    }

    public ConfigManager getConfig() { return configManager; }
    public Set<UUID> getSocialSpyUsers() { return socialSpyUsers; }
    public Set<UUID> getMsgDisabledUsers() { return msgDisabledUsers; }
    public Map<UUID, Set<UUID>> getIgnoredPlayers() { return ignoredPlayers; }
    public Map<UUID, UUID> getLastMessaged() { return lastMessaged; }
    public Map<UUID, UUID> getSpyTargets() { return spyTargets; }
    public Map<UUID, String> getCmdSpyUsers() { return cmdSpyUsers; }
    public ProxyServer getServer() { return server; }
}
