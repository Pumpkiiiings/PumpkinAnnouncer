package pumpkin.anuncios.core;

import net.kyori.adventure.audience.Audience;
import java.nio.file.Path;
import java.util.Collection;
import java.util.logging.Logger;

public interface PumpkinPlatform {
    Logger getPluginLogger();
    Path getPluginDataDirectory();
    void cancelTask();
    void scheduleTask(Runnable task, int intervalSeconds);
    void scheduleDelayedTask(Runnable task, int delaySeconds);
    Collection<? extends Audience> getOnlinePlayers();
    String getServerName(Audience player);
    String getWorldName(Audience player); // ¡NUEVO!
}
