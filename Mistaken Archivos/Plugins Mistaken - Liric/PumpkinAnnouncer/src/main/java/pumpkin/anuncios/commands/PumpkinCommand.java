package pumpkin.anuncios.commands;

import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.ProxyServer;
import net.kyori.adventure.text.minimessage.MiniMessage;
import pumpkin.anuncios.PumpkinAnnouncer;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class PumpkinCommand implements SimpleCommand {

    private final PumpkinAnnouncer plugin;
    private final ProxyServer server;

    public PumpkinCommand(PumpkinAnnouncer plugin, ProxyServer server) {
        this.plugin = plugin;
        this.server = server;
    }

    @Override
    public void execute(Invocation invocation) {
        String[] args = invocation.arguments();
        var mm = MiniMessage.miniMessage();

        if (args.length == 0) {
            invocation.source().sendMessage(mm.deserialize(plugin.getMsg("help", "<gold>Usa /pa reload, list o test <anuncio>")));
            return;
        }

        switch (args[0].toLowerCase()) {
            case "reload" -> {
                plugin.loadConfig();
                plugin.startAnnouncerTask();
                invocation.source().sendMessage(mm.deserialize(plugin.getMsg("reload-success", "<green>¡Config v2.1 cargada!")));
            }
            case "list" -> {
                // Ahora usa el list-header de tu config
                invocation.source().sendMessage(mm.deserialize(plugin.getMsg("list-header", "<gold><bold>Anuncios en el sistema:</bold></gold>")));
                plugin.getAnnouncementMap().forEach((id, ann) ->
                        invocation.source().sendMessage(mm.deserialize("<yellow>» <white>" + id + " <gray>(" + ann.servers() + ")"))
                );
            }
            case "test" -> {
                if (args.length < 2) {
                    invocation.source().sendMessage(mm.deserialize("<red>Uso: /pa test <nombre_anuncio>"));
                    return;
                }
                PumpkinAnnouncer.Announcement ann = plugin.getAnnouncementMap().get(args[1]);
                if (ann != null) {
                    plugin.broadcast(ann);
                } else {
                    // Ahora usa el id-not-found de tu config
                    invocation.source().sendMessage(mm.deserialize(plugin.getMsg("id-not-found", "<red>Ese anuncio no existe, checa tu config.")));
                }
            }
            default -> invocation.source().sendMessage(mm.deserialize("<red>Comando desconocido."));
        }
    }

    @Override
    public List<String> suggest(Invocation invocation) {
        String[] args = invocation.arguments();
        List<String> suggestions = new ArrayList<>();

        if (args.length == 0 || args.length == 1) {
            suggestions.add("reload");
            suggestions.add("list");
            suggestions.add("test");

            String typing = args.length == 1 ? args[0].toLowerCase() : "";
            return suggestions.stream()
                    .filter(s -> s.startsWith(typing))
                    .collect(Collectors.toList());

        } else if (args.length == 2 && args[0].equalsIgnoreCase("test")) {
            String typing = args[1].toLowerCase();
            return plugin.getAnnouncementMap().keySet().stream()
                    .filter(id -> id.toLowerCase().startsWith(typing))
                    .collect(Collectors.toList());
        }

        return List.of();
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return invocation.source().hasPermission("pumpkin.admin");
    }
}
