package com.pumpkin.msg.commands;

import com.pumpkin.msg.PumpkinMsg;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class MsgCommand implements SimpleCommand {

    private final PumpkinMsg plugin;
    private final ProxyServer server;
    private final MiniMessage mm = MiniMessage.miniMessage();

    public MsgCommand(PumpkinMsg plugin, ProxyServer server) {
        this.plugin = plugin;
        this.server = server;
    }

    @Override
    public void execute(Invocation invocation) {
        if (!(invocation.source() instanceof Player sender)) return;

        String[] args = invocation.arguments();

        if (args.length < 2) {
            sender.sendMessage(mm.deserialize(plugin.getConfig().getString("messages.usage")));
            return;
        }

        if (plugin.getMsgDisabledUsers().contains(sender.getUniqueId())) {
            sender.sendMessage(mm.deserialize(plugin.getConfig().getString("messages.sender-toggled-off")));
            return;
        }

        Optional<Player> targetOpt = server.getPlayer(args[0]);

        if (targetOpt.isEmpty()) {
            sender.sendMessage(mm.deserialize(plugin.getConfig().getString("messages.player-offline")));
            return;
        }

        Player target = targetOpt.get();

        if (sender.getUniqueId().equals(target.getUniqueId())) {
            sender.sendMessage(mm.deserialize(plugin.getConfig().getString("messages.cannot-msg-self")));
            return;
        }

        boolean hasBypass = sender.hasPermission("pumpkinmsg.staff.bypass");

        if (plugin.getMsgDisabledUsers().contains(target.getUniqueId()) && !hasBypass) {
            sender.sendMessage(mm.deserialize(plugin.getConfig().getString("messages.target-toggled-off")));
            return;
        }

        Set<UUID> targetIgnoredList = plugin.getIgnoredPlayers().get(target.getUniqueId());
        if (targetIgnoredList != null && targetIgnoredList.contains(sender.getUniqueId()) && !hasBypass) {
            sender.sendMessage(mm.deserialize(plugin.getConfig().getString("messages.player-ignoring-you")));
            return;
        }

        String messageContent = String.join(" ", Arrays.copyOfRange(args, 1, args.length));

        sender.sendMessage(mm.deserialize(plugin.getConfig().getString("format.sender"),
                Placeholder.parsed("target", target.getUsername()),
                Placeholder.parsed("message", messageContent)));

        target.sendMessage(mm.deserialize(plugin.getConfig().getString("format.receiver"),
                Placeholder.parsed("sender", sender.getUsername()),
                Placeholder.parsed("message", messageContent)));

        plugin.getLastMessaged().put(sender.getUniqueId(), target.getUniqueId());
        plugin.getLastMessaged().put(target.getUniqueId(), sender.getUniqueId());

        broadcastToStaff(sender, target, messageContent);
    }

    private void broadcastToStaff(Player sender, Player target, String message) {
        String spyFormat = plugin.getConfig().getString("format.spy");
        UUID senderId = sender.getUniqueId();
        UUID targetId = target.getUniqueId();

        Component senderPrefix = plugin.getPrefix(sender);
        Component targetPrefix = plugin.getPrefix(target);

        for (Player staff : server.getAllPlayers()) {
            UUID staffId = staff.getUniqueId();

            if (plugin.getSocialSpyUsers().contains(staffId) && !staffId.equals(senderId) && !staffId.equals(targetId)) {
                UUID specificTarget = plugin.getSpyTargets().get(staffId);

                if (specificTarget == null || senderId.equals(specificTarget) || targetId.equals(specificTarget)) {
                    staff.sendMessage(mm.deserialize(spyFormat,
                            Placeholder.component("sender_prefix", senderPrefix),
                            Placeholder.parsed("sender", sender.getUsername()),
                            Placeholder.component("target_prefix", targetPrefix),
                            Placeholder.parsed("target", target.getUsername()),
                            Placeholder.parsed("message", message)));
                }
            }
        }
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
