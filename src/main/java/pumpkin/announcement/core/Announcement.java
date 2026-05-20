package pumpkin.announcement.core;

import java.util.List;

public record Announcement(
        List<String> servers,
        List<String> worlds,
        List<String> lines,
        String sound,
        ActionBarConfig actionbar,
        BossBarConfig bossbar,
        TitleConfig title
) {
    public record ActionBarConfig(boolean enabled, String text, int durationSeconds) {}

    public record AnimatedGradient(boolean enabled, List<String> colors, int frameIntervalTicks) {
        public static AnimatedGradient disabled() {
            return new AnimatedGradient(false, List.of(), 4);
        }
    }

    public record BossBarConfig(
            boolean enabled, String text, String color, String style,
            int durationSeconds, boolean deplete, AnimatedGradient animated
    ) {}

    public record TitleConfig(
            boolean enabled,
            String title,
            String subtitle,
            int fadeInTicks,
            int stayTicks,
            int fadeOutTicks,
            AnimatedGradient animated
    ) {
        public static TitleConfig disabled() {
            return new TitleConfig(false, "", "", 10, 60, 10, AnimatedGradient.disabled());
        }
    }
}
