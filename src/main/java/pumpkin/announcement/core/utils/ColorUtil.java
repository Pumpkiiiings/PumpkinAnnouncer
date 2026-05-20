package pumpkin.announcement.core.utils;

public class ColorUtil {
    
    public static String translateAll(String text) {
        if (text == null || text.isEmpty()) return "";

        text = text.replaceAll("&#([a-fA-F0-9]{6})", "<#$1>");

        text = text.replaceAll("&x&([a-fA-F0-9])&([a-fA-F0-9])&([a-fA-F0-9])&([a-fA-F0-9])&([a-fA-F0-9])&([a-fA-F0-9])", "<#$1$2$3$4$5$6>");

        text = text.replace("&0", "<black>").replace("&1", "<dark_blue>").replace("&2", "<dark_green>")
                .replace("&3", "<dark_aqua>").replace("&4", "<dark_red>").replace("&5", "<dark_purple>")
                .replace("&6", "<gold>").replace("&7", "<gray>").replace("&8", "<dark_gray>")
                .replace("&9", "<blue>").replace("&a", "<green>").replace("&b", "<aqua>")
                .replace("&c", "<red>").replace("&d", "<light_purple>").replace("&e", "<yellow>")
                .replace("&f", "<white>").replace("&l", "<bold>").replace("&o", "<italic>")
                .replace("&n", "<underlined>").replace("&m", "<strikethrough>").replace("&k", "<obfuscated>")
                .replace("&r", "<reset>");

        return text;
    }
}
