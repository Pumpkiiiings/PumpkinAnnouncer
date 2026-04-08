package com.pumpkin.msg.commands;

import com.pumpkin.msg.PumpkinMsg;
import com.velocitypowered.api.command.SimpleCommand;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.util.List;

public class ReloadCommand implements SimpleCommand {

    private final PumpkinMsg plugin;
    private final MiniMessage mm = MiniMessage.miniMessage();

    public ReloadCommand(PumpkinMsg plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(Invocation invocation) {
        // 1. Verificación de permisos (Admins de alto nivel)
        // Usamos invocation.source() para que funcione igual en Consola y Jugadores
        if (!invocation.source().hasPermission("pumpkinmsg.admin.reload")) {
            invocation.source().sendMessage(mm.deserialize(plugin.getConfig().getString("messages.no-permission")));
            return;
        }

        // 2. Recarga formal de los archivos de configuración
        try {
            plugin.getConfig().loadConfiguration();
            invocation.source().sendMessage(mm.deserialize(plugin.getConfig().getString("messages.reload-success")));
        } catch (Exception e) {
            // Un aviso por si algo truena en la recarga, bro
            invocation.source().sendMessage(mm.deserialize("<red>Error crítico al recargar la configuración. Revisa la consola."));
            e.printStackTrace();
        }
    }

    /**
     * Como es un comando de una sola palabra, no necesitamos sugerencias,
     * pero devolvemos una lista vacía para que el Tab no haga cosas raras.
     */
    @Override
    public List<String> suggest(Invocation invocation) {
        return List.of();
    }
}
