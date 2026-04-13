package pumpkin.anuncios.paper;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import pumpkin.anuncios.core.AnnouncerCore;

import java.util.List;

public class PaperCommand extends Command {

    private final AnnouncerCore core;

    public PaperCommand(AnnouncerCore core) {
        super("pa", "Comando de Pumpkin Announcer", "/pa", List.of("pumpkinannouncer", "anuncios"));
        this.core = core;

        setPermission("pumpkin.admin");
        setPermissionMessage("§cNo tienes permisos para usar este comando.");
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args) {
        if (!testPermission(sender)) return true;

        core.executeCommand(sender, args);
        return true;
    }

    @NotNull
    @Override
    public List<String> tabComplete(@NotNull CommandSender sender, @NotNull String alias, @NotNull String[] args) {
        if (!sender.hasPermission("pumpkin.admin")) return List.of();
        return core.getTabCompletions(args);
    }
}
