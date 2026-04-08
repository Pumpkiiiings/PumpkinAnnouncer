package com.pumpkin.msg.commands;

import com.pumpkin.msg.PumpkinMsg;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.util.Optional;
import java.util.UUID;

/**
 * Command handler for quick replies to the last messaged player.
 * Inherits security and privacy logic by delegating execution to the main message command.
 */
public class ReplyCommand implements SimpleCommand {

    private final PumpkinMsg plugin;
    private final ProxyServer server;
    private final MiniMessage mm = MiniMessage.miniMessage();

    public ReplyCommand(PumpkinMsg plugin, ProxyServer server) {
        this.plugin = plugin;
        this.server = server;
    }

    @Override
    public void execute(Invocation invocation) {
        if (!(invocation.source() instanceof Player sender)) return;

        // 1. Retrieve the UUID of the last interacted player
        UUID targetUUID = plugin.getLastMessaged().get(sender.getUniqueId());

        if (targetUUID == null) {
            sender.sendMessage(mm.deserialize(plugin.getConfig().getString("messages.no-reply-target")));
            return;
        }

        // 2. Self-reply prevention and data integrity check
        if (targetUUID.equals(sender.getUniqueId())) {
            plugin.getLastMessaged().remove(sender.getUniqueId());
            sender.sendMessage(mm.deserialize(plugin.getConfig().getString("messages.cannot-reply-self")));
            return;
        }

        String[] args = invocation.arguments();

        // 3. Argument validation
        if (args.length == 0) {
            sender.sendMessage(mm.deserialize(plugin.getConfig().getString("messages.reply-usage")));
            return;
        }

        // 4. Target availability check via ProxyServer
        Optional<Player> targetOpt = server.getPlayer(targetUUID);

        if (targetOpt.isEmpty()) {
            sender.sendMessage(mm.deserialize(plugin.getConfig().getString("messages.player-offline")));
            return;
        }

        Player target = targetOpt.get();
        String messageContent = String.join(" ", args);

        /*
         * Asynchronous execution delegation to the core /msg command.
         * This ensures that SocialSpy, Ignore, and Toggle validations are consistently applied.
         */
        String fullCommand = "msg " + target.getUsername() + " " + messageContent;
        server.getCommandManager().executeAsync(sender, fullCommand);
    }
}
