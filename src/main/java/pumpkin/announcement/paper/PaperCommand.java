package pumpkin.announcement.paper;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import pumpkin.announcement.core.AnnouncerCore;

import java.util.List;

public class PaperCommand extends Command {

    private final AnnouncerCore core;

    public PaperCommand(AnnouncerCore core) {
        super("pa", "Pumpkin Announcer command", "/pa", List.of("pumpkinannouncer", "announcements"));
        this.core = core;

        setPermission("pumpkin.admin");
        setPermissionMessage("§cYou do not have permission to use this command.");
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
