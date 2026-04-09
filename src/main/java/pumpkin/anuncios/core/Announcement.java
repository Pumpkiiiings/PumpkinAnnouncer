package pumpkin.anuncios.core;

import java.util.List;

public record Announcement(
        List<String> servers,
        List<String> lines,
        String sound,
        ActionBarConfig actionbar,
        BossBarConfig bossbar
) {
    public record ActionBarConfig(boolean enabled, String text, int durationSeconds) {}
    public record BossBarConfig(boolean enabled, String text, String color, String style, int durationSeconds, boolean deplete) {}
}
