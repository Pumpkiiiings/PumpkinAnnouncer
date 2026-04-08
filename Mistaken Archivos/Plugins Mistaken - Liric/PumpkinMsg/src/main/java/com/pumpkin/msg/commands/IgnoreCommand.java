package com.pumpkin.msg.commands;

import com.pumpkin.msg.PumpkinMsg;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class IgnoreCommand implements SimpleCommand {

    private final PumpkinMsg plugin;
    private final ProxyServer server;
    private final MiniMessage mm = MiniMessage.miniMessage();

    public IgnoreCommand(PumpkinMsg plugin, ProxyServer server) {
        this.plugin = plugin;
        this.server = server;
    }

    @Override
    public void execute(Invocation invocation) {
        if (!(invocation.source() instanceof Player sender)) return;

        String[] args = invocation.arguments();

        if (args.length == 0) {
            sender.sendMessage(mm.deserialize(plugin.getConfig().getString("messages.ignore-usage")));
            return;
        }

        Optional<Player> targetOpt = server.getPlayer(args[0]);

        if (targetOpt.isEmpty()) {
            sender.sendMessage(mm.deserialize(plugin.getConfig().getString("messages.player-offline")));
            return;
        }

        Player target = targetOpt.get();
        UUID senderId = sender.getUniqueId();
        UUID targetId = target.getUniqueId();

        if (senderId.equals(targetId)) {
            sender.sendMessage(mm.deserialize(plugin.getConfig().getString("messages.cannot-ignore-self")));
            return;
        }

        Set<UUID> ignored = plugin.getIgnoredPlayers().computeIfAbsent(senderId, k -> ConcurrentHashMap.newKeySet());

        if (ignored.contains(targetId)) {
            ignored.remove(targetId);
            sender.sendMessage(mm.deserialize(plugin.getConfig().getString("messages.unignored-player"),
                    Placeholder.parsed("target", target.getUsername())));
        } else {
            ignored.add(targetId);
            sender.sendMessage(mm.deserialize(plugin.getConfig().getString("messages.ignored-player"),
                    Placeholder.parsed("target", target.getUsername())));
        }

        plugin.getConfig().saveIgnoreMap(plugin.getIgnoredPlayers());
    }

    // --- Autocompletado (TAB) Nativo ---
    @Override
    public List<String> suggest(Invocation invocation) {
        String[] args = invocation.arguments();
        if (args.length <= 1) {
            String search = args.length == 0 ? "" : args[0].toLowerCase();
            return server.getAllPlayers().stream()
                    .map(Player::getUsername)
                    .filter(name -> name.toLowerCase().startsWith(search))
                    .collect(Collectors.toList());
        }
        return List.of();
    }
}
