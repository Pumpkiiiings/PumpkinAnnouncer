package pumpkin.announcement.paper;

import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.audience.Audience;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;
import pumpkin.announcement.core.AnnouncerCore;
import pumpkin.announcement.core.PumpkinPlatform;

import java.nio.file.Path;
import java.util.Collection;
import java.util.logging.Logger;

public class PumpkinPaper extends JavaPlugin implements PumpkinPlatform, Listener {

    private int taskId = -1;
    private AnnouncerCore core;
    private boolean papiEnabled = false;

    @Override
    public void onEnable() {
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            papiEnabled = true;
            getLogger().info("[PumpkinAnnouncer] PlaceholderAPI detected — placeholder support enabled.");
        } else {
            getLogger().info("[PumpkinAnnouncer] PlaceholderAPI not found — placeholders disabled.");
        }

        this.core = new AnnouncerCore(this);
        core.loadConfig();
        core.startTask();
        getServer().getCommandMap().register("pumpkin", new PaperCommand(core));

        core.initUpdateChecker();
        getServer().getPluginManager().registerEvents(this, this);

        getLogger().info("Successfully loaded in Paper mode!");
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        if (event.getPlayer().hasPermission("pumpkin.admin")) {
            scheduleDelayedTask(() -> core.notifyUpdate(event.getPlayer()), 2);
        }
    }

    @Override public Logger getPluginLogger() { return getLogger(); }
    @Override public Path getPluginDataDirectory() { return getDataFolder().toPath(); }
    @Override public String getConfigFileName() { return "paper-config.yml"; }

    @Override public void cancelTask() {
        if (taskId != -1) Bukkit.getScheduler().cancelTask(taskId);
    }

    @Override public void scheduleTask(Runnable r, int secs) {
        taskId = Bukkit.getScheduler().runTaskTimerAsynchronously(this, r, 0L, secs * 20L).getTaskId();
    }

    @Override public void scheduleDelayedTask(Runnable task, int delaySeconds) {
        Bukkit.getScheduler().runTaskLaterAsynchronously(this, task, delaySeconds * 20L);
    }

    @Override public void scheduleDelayedTaskTicks(Runnable task, long delayTicks) {
        Bukkit.getScheduler().runTaskLaterAsynchronously(this, task, delayTicks);
    }

    @Override public Collection<? extends Audience> getOnlinePlayers() {
        return (Collection<? extends Audience>) Bukkit.getOnlinePlayers();
    }

    @Override public String getServerName(Audience player) { return "global"; }

    @Override public String getWorldName(Audience player) {
        if (player instanceof Player p) return p.getWorld().getName();
        return "global";
    }

    @Override public String parsePlaceholders(String text, Audience player) {
        if (!papiEnabled) return text;
        Player bukkit = (player instanceof Player p) ? p : null;
        return PlaceholderAPI.setPlaceholders(bukkit, text);
    }
}
