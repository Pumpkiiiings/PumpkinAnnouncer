package pumpkin.anuncios.paper;

import net.kyori.adventure.audience.Audience;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import pumpkin.anuncios.core.AnnouncerCore;
import pumpkin.anuncios.core.PumpkinPlatform;

import java.nio.file.Path;
import java.util.Collection;
import java.util.logging.Logger;

public class PumpkinPaper extends JavaPlugin implements PumpkinPlatform {

    private int taskId = -1;

    @Override
    public void onEnable() {
        AnnouncerCore core = new AnnouncerCore(this);
        core.loadConfig();
        core.startTask();

        // INYECCIÓN PRO: Registramos el comando directamente al CommandMap
        // El "pumpkin" es un fallback prefix por si otro plugin tiene el comando /pa
        getServer().getCommandMap().register("pumpkin", new PaperCommand(core));

        getLogger().info("¡Cargado correctamente en modo Paper!");
    }

    @Override public Logger getPluginLogger() { return getLogger(); }
    @Override public Path getPluginDataDirectory() { return getDataFolder().toPath(); }

    @Override public void cancelTask() {
        if (taskId != -1) Bukkit.getScheduler().cancelTask(taskId);
    }

    @Override public void scheduleTask(Runnable r, int secs) {
        taskId = Bukkit.getScheduler().runTaskTimerAsynchronously(this, r, 0L, secs * 20L).getTaskId();
    }

    // --- NUEVO: Para ocultar la BossBar después del tiempo configurado ---
    @Override public void scheduleDelayedTask(Runnable task, int delaySeconds) {
        Bukkit.getScheduler().runTaskLaterAsynchronously(this, task, delaySeconds * 20L);
    }

    // (Casteo añadido por seguridad para evitar advertencias de IDE)
    @Override public Collection<? extends Audience> getOnlinePlayers() {
        return (Collection<? extends Audience>) Bukkit.getOnlinePlayers();
    }

    @Override public String getServerName(Audience player) { return "global"; }
}
