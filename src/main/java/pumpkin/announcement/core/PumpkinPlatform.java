package pumpkin.announcement.core;

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
    void scheduleDelayedTaskTicks(Runnable task, long delayTicks);
    Collection<? extends Audience> getOnlinePlayers();
    String getServerName(Audience player);
    String getWorldName(Audience player);
    String getConfigFileName();
    /**
     * Aplica PlaceholderAPI (si disponible) al texto dado.
     * En Velocity o sin PAPI instalado, devuelve el texto sin cambios.
     * @param text   Texto con placeholders %placeholder%
     * @param player Jugador contexto, puede ser null para placeholders globales
     */
    String parsePlaceholders(String text, Audience player);
}
