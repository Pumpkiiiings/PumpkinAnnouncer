package pumpkin.announcement.core.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class GradientAnimator {

    private static final MiniMessage MM = MiniMessage.miniMessage();

    public static List<Component> generateFrames(String rawText, List<String> colors, int frameCount) {
        List<Component> frames = new ArrayList<>(frameCount);
        if (colors == null || colors.isEmpty() || frameCount <= 0) {
            frames.add(MM.deserialize(rawText));
            return frames;
        }

        String colorStr = String.join(":", colors);

        for (int i = 0; i < frameCount; i++) {
            float phase = ((float) i / frameCount) * 2.0f - 1.0f;
            String phaseStr = String.format(Locale.US, "%.3f", phase);
            String tag = "<gradient:" + colorStr + ":" + phaseStr + ">" + rawText + "</gradient>";
            frames.add(MM.deserialize(tag));
        }

        return frames;
    }
}
