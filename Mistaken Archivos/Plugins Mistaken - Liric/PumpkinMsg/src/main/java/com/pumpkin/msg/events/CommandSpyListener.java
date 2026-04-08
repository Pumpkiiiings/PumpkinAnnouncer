package com.pumpkin.msg.events;

import com.pumpkin.msg.PumpkinMsg;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.command.CommandExecuteEvent;
import com.velocitypowered.api.proxy.Player;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;

import java.util.UUID;

public class CommandSpyListener {
    private final PumpkinMsg plugin;
    private final MiniMessage mm = MiniMessage.miniMessage();

    public CommandSpyListener(PumpkinMsg plugin) {
        this.plugin = plugin;
    }

    @Subscribe
    public void onCommandExecute(CommandExecuteEvent event) {
        // CORRECCIÓN: Se debe usar getCommandSource() en lugar de getSource()
        if (!event.getResult().isAllowed() || !(event.getCommandSource() instanceof Player player)) return;

        String command = event.getCommand();

        // Medida de seguridad: Ocultar contraseñas
        String checkCmd = command.toLowerCase();
        if (checkCmd.startsWith("login ") || checkCmd.startsWith("l ") ||
                checkCmd.startsWith("register ") || checkCmd.startsWith("reg ") ||
                checkCmd.startsWith("changepassword ")) {
            return;
        }

        String serverName = player.getCurrentServer().map(sv -> sv.getServerInfo().getName()).orElse("Ninguno");
        String spyFormat = plugin.getConfig().getString("format.cmdspy");

        for (Player staff : plugin.getServer().getAllPlayers()) {
            UUID staffId = staff.getUniqueId();
            if (!plugin.getCmdSpyUsers().containsKey(staffId)) continue;

            // Ignoramos espiarte a ti mismo mandando comandos
            if (staffId.equals(player.getUniqueId())) continue;

            String mode = plugin.getCmdSpyUsers().get(staffId);
            if (mode.equalsIgnoreCase("ALL") || mode.equalsIgnoreCase(serverName)) {
                staff.sendMessage(mm.deserialize(spyFormat,
                        Placeholder.component("player_prefix", plugin.getPrefix(player)),
                        Placeholder.parsed("player", player.getUsername()),
                        Placeholder.parsed("server", serverName),
                        Placeholder.parsed("command", command)
                ));
            }
        }
    }
}
