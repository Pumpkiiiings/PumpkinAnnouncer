package com.pumpkin.msg.commands;

import com.pumpkin.msg.PumpkinMsg;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

public class SpyCommand implements SimpleCommand {
    private final PumpkinMsg plugin;
    private final MiniMessage mm = MiniMessage.miniMessage();

    public SpyCommand(PumpkinMsg plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(Invocation invocation) {
        if (!(invocation.source() instanceof Player player)) return;

        // 1. Verificación de permisos (Solo Staff de alto rango, bro)
        if (!player.hasPermission("pumpkinmsg.staff.spy")) {
            player.sendMessage(mm.deserialize(plugin.getConfig().getString("messages.no-permission")));
            return;
        }

        String[] args = invocation.arguments();
        UUID staffUuid = player.getUniqueId();

        // 2. Lógica para objetivo específico (/spy <jugador>)
        if (args.length > 0) {
            Optional<Player> targetOpt = plugin.getServer().getPlayer(args[0]);

            if (targetOpt.isEmpty()) {
                player.sendMessage(mm.deserialize(plugin.getConfig().getString("messages.player-offline")));
                return;
            }

            Player target = targetOpt.get();

            // Seteamos el objetivo y activamos el spy
            plugin.getSpyTargets().put(staffUuid, target.getUniqueId());
            plugin.getSocialSpyUsers().add(staffUuid);

            player.sendMessage(mm.deserialize(plugin.getConfig().getString("messages.spy-target-set"),
                    Placeholder.parsed("target", target.getUsername())));
        }
        // 3. Lógica para Toggle Global o Apagar
        else {
            if (plugin.getSocialSpyUsers().contains(staffUuid)) {
                plugin.getSocialSpyUsers().remove(staffUuid);
                plugin.getSpyTargets().remove(staffUuid);
                player.sendMessage(mm.deserialize(plugin.getConfig().getString("messages.spy-disabled")));
            } else {
                plugin.getSocialSpyUsers().add(staffUuid);
                plugin.getSpyTargets().remove(staffUuid);
                player.sendMessage(mm.deserialize(plugin.getConfig().getString("messages.spy-enabled")));
            }
        }

        // Persistencia para que no se pierda el estado tras un reinicio
        plugin.getConfig().saveSpyUsers(plugin.getSocialSpyUsers());
    }

    /**
     * Sugerencias automáticas de nombres de jugadores para el Staff.
     */
    @Override
    public List<String> suggest(Invocation invocation) {
        String[] args = invocation.arguments();

        // Solo sugerimos nombres en el primer argumento
        if (args.length <= 1) {
            String search = args.length == 0 ? "" : args[0].toLowerCase();
            return plugin.getServer().getAllPlayers().stream()
                    .map(Player::getUsername)
                    .filter(name -> name.toLowerCase().startsWith(search))
                    .collect(Collectors.toList());
        }

        return List.of();
    }
}
