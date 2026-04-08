package com.pumpkin.msg.commands;

import com.pumpkin.msg.PumpkinMsg;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CommandSpy implements SimpleCommand {
    private final PumpkinMsg plugin;
    private final MiniMessage mm = MiniMessage.miniMessage();

    public CommandSpy(PumpkinMsg plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(Invocation invocation) {
        if (!(invocation.source() instanceof Player player)) return;

        if (!player.hasPermission("pumpkinmsg.staff.cmdspy")) {
            player.sendMessage(mm.deserialize(plugin.getConfig().getString("messages.no-permission")));
            return;
        }

        String[] args = invocation.arguments();
        UUID uuid = player.getUniqueId();

        if (args.length == 0) {
            if (plugin.getCmdSpyUsers().containsKey(uuid)) {
                plugin.getCmdSpyUsers().remove(uuid);
                player.sendMessage(mm.deserialize(plugin.getConfig().getString("messages.cmdspy-disabled")));
            } else {
                plugin.getCmdSpyUsers().put(uuid, "ALL");
                player.sendMessage(mm.deserialize(plugin.getConfig().getString("messages.cmdspy-enabled"),
                        Placeholder.parsed("mode", "Global")));
            }
        } else {
            String mode = args[0].toLowerCase();
            if (mode.equalsIgnoreCase("off") || mode.equalsIgnoreCase("disable")) {
                plugin.getCmdSpyUsers().remove(uuid);
                player.sendMessage(mm.deserialize(plugin.getConfig().getString("messages.cmdspy-disabled")));
            } else {
                plugin.getCmdSpyUsers().put(uuid, mode);
                player.sendMessage(mm.deserialize(plugin.getConfig().getString("messages.cmdspy-enabled"),
                        Placeholder.parsed("mode", mode.equalsIgnoreCase("global") ? "Global" : "Server: " + mode)));
            }
        }
        plugin.getConfig().saveCmdSpy(plugin.getCmdSpyUsers());
    }

    @Override
    public List<String> suggest(Invocation invocation) {
        if (invocation.arguments().length <= 1) {
            String arg = invocation.arguments().length == 0 ? "" : invocation.arguments()[0].toLowerCase();
            List<String> s = new ArrayList<>();
            s.add("global");
            s.add("off");
            plugin.getServer().getAllServers().forEach(sv -> s.add(sv.getServerInfo().getName()));
            return s.stream().filter(sv -> sv.toLowerCase().startsWith(arg)).toList();
        }
        return List.of();
    }
}
