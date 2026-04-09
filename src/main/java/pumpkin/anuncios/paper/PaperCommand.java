package pumpkin.anuncios.paper;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import pumpkin.anuncios.core.AnnouncerCore;

import java.util.List;

public class PaperCommand extends Command {

    private final AnnouncerCore core;

    public PaperCommand(AnnouncerCore core) {
        // Nombre principal, descripción, mensaje de uso y Alias automáticos
        super("pa", "Comando de Pumpkin Announcer", "/pa", List.of("pumpkinannouncer", "anuncios"));
        this.core = core;

        // Manejo nativo de permisos
        setPermission("pumpkin.admin");
        setPermissionMessage("§cNo tienes permisos para usar este comando.");
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args) {
        // testPermission comprueba si tiene "pumpkin.admin" y si no, le manda el PermissionMessage automáticamente
        if (!testPermission(sender)) return true;

        // CommandSender en Paper funciona perfectamente como "Audience" para MiniMessage
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
