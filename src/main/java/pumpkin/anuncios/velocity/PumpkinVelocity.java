package pumpkin.anuncios.velocity;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.scheduler.ScheduledTask;
import net.kyori.adventure.audience.Audience;
import pumpkin.anuncios.core.AnnouncerCore;
import pumpkin.anuncios.core.PumpkinPlatform;

import java.nio.file.Path;
import java.util.Collection;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

@Plugin(id = "pumpkinannouncer", name = "PumpkinAnnouncer", version = "2.1", authors = {"Pumpkingz"})
public class PumpkinVelocity implements PumpkinPlatform {

    private final ProxyServer server;
    private final Logger logger;
    private final Path dataDirectory;
    private ScheduledTask task;

    @Inject
    public PumpkinVelocity(ProxyServer server, Logger logger, @DataDirectory Path dataDir) {
        this.server = server;
        this.logger = logger;
        this.dataDirectory = dataDir;
    }

    @Subscribe
    public void onInit(ProxyInitializeEvent event) {
        AnnouncerCore core = new AnnouncerCore(this);
        core.loadConfig();
        core.startTask();
        server.getCommandManager().register("pa", new VelocityCommand(core));
        logger.info("Cargado correctamente en modo Velocity.");
    }

    @Override public Logger getPluginLogger() { return logger; }
    @Override public Path getPluginDataDirectory() { return dataDirectory; }

    @Override public void cancelTask() {
        if (task != null) task.cancel();
    }

    @Override public void scheduleTask(Runnable r, int secs) {
        task = server.getScheduler().buildTask(this, r).repeat(secs, TimeUnit.SECONDS).schedule();
    }

    // --- NUEVO: Para ocultar la BossBar después del tiempo configurado ---
    @Override public void scheduleDelayedTask(Runnable task, int delaySeconds) {
        server.getScheduler().buildTask(this, task).delay(delaySeconds, TimeUnit.SECONDS).schedule();
    }

    @Override public Collection<? extends Audience> getOnlinePlayers() {
        return server.getAllPlayers();
    }

    @Override public String getServerName(Audience player) {
        if (player instanceof Player p) {
            return p.getCurrentServer().map(s -> s.getServerInfo().getName()).orElse("global");
        }
        return "global";
    }
}
