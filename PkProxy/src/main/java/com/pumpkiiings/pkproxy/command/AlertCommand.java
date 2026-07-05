package com.pumpkiiings.pkproxy.command;

import com.pumpkiiings.pkproxy.PkProxy;
import com.pumpkiiings.pkproxy.util.ColorUtil;
import com.velocitypowered.api.command.SimpleCommand;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import com.velocitypowered.api.proxy.Player;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.key.Key;

public class AlertCommand implements SimpleCommand {

    private final PkProxy plugin;

    public AlertCommand(PkProxy plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(Invocation invocation) {
        String[] args = invocation.arguments();
        if (args.length == 0) {
            invocation.source().sendMessage(plugin.getMsgManager().getMessage("alert-usage"));
            return;
        }

        StringBuilder message = new StringBuilder();
        for (String arg : args) {
            message.append(arg).append(" ");
        }

        String alertFormat = plugin.getConfigManager().getMessages().node("alert-format").getString("<dark_red><bold>[ALERT]</bold> <red><message>");
        alertFormat = alertFormat.replace("<message>", message.toString().trim());

        Component comp = MiniMessage.miniMessage().deserialize(ColorUtil.translateAll(alertFormat));

        String alertSoundStr = plugin.getConfigManager().getMessages().node("alert-sound").getString("entity.experience_orb.pickup");
        Sound alertSound = null;
        if (alertSoundStr != null && !alertSoundStr.equalsIgnoreCase("none") && !alertSoundStr.isEmpty()) {
            try {
                alertSound = Sound.sound(Key.key(alertSoundStr), Sound.Source.MASTER, 1f, 1f);
            } catch (Exception e) {
                plugin.getLogger().warn("Invalid alert-sound key: " + alertSoundStr);
            }
        }

        // Broadcast to all players and console
        for (Player p : plugin.getProxy().getAllPlayers()) {
            p.sendMessage(comp);
            if (alertSound != null) {
                p.playSound(alertSound);
            }
        }
        plugin.getLogger().info("ALERT: " + message.toString().trim());
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return invocation.source().hasPermission("pkproxy.alert");
    }
}
