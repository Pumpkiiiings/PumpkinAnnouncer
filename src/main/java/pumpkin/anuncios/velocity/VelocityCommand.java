package pumpkin.anuncios.velocity;

import com.velocitypowered.api.command.SimpleCommand;
import pumpkin.anuncios.core.AnnouncerCore;
import java.util.List;

public class VelocityCommand implements SimpleCommand {
    private final AnnouncerCore core;

    public VelocityCommand(AnnouncerCore core) { this.core = core; }

    @Override
    public void execute(Invocation invocation) {
        core.executeCommand(invocation.source(), invocation.arguments());
    }

    @Override
    public List<String> suggest(Invocation invocation) {
        return core.getTabCompletions(invocation.arguments());
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return invocation.source().hasPermission("pumpkin.admin");
    }
}
