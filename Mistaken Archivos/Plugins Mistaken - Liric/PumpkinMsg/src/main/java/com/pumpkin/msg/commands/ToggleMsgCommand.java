package com.pumpkin.msg.commands;

import com.pumpkin.msg.PumpkinMsg;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.util.UUID;

public class ToggleMsgCommand implements SimpleCommand {

    private final PumpkinMsg plugin;
    private final MiniMessage mm = MiniMessage.miniMessage();

    public ToggleMsgCommand(PumpkinMsg plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(Invocation invocation) {
        if (!(invocation.source() instanceof Player player)) return;

        UUID uuid = player.getUniqueId();
        if (plugin.getMsgDisabledUsers().contains(uuid)) {
            plugin.getMsgDisabledUsers().remove(uuid);
            player.sendMessage(mm.deserialize(plugin.getConfig().getString("messages.toggle-on")));
        } else {
            plugin.getMsgDisabledUsers().add(uuid);
            player.sendMessage(mm.deserialize(plugin.getConfig().getString("messages.toggle-off")));
        }

        // Guardar persistencia
        plugin.getConfig().saveUUIDSet(plugin.getMsgDisabledUsers(), "toggled_msgs.txt");
    }
}
