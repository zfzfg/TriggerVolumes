package de.zfzfg.triggervolumes.utils;

import org.bukkit.ChatColor;

/**
 * Utility class for message formatting and color codes.
 * 
 * @author zfzfg
 */
public final class MessageUtils {

    private MessageUtils() {
        // Private constructor to prevent instantiation
    }

    /**
     * Translates color codes in a message using the '&' character.
     * 
     * @param message The message to translate
     * @return The translated message
     */
    public static String colorize(String message) {
        if (message == null) {
            return "";
        }
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    /**
     * Strips all color codes from a message.
     * 
     * @param message The message to strip
     * @return The stripped message
     */
    public static String stripColors(String message) {
        if (message == null) {
            return "";
        }
        return ChatColor.stripColor(colorize(message));
    }

    /**
     * Centers a message for chat display.
     * 
     * @param message The message to center
     * @return The centered message
     */
    public static String centerMessage(String message) {
        if (message == null || message.isEmpty()) {
            return "";
        }

        final int CENTER_PX = 154; // Center pixel width for Minecraft chat
        int messagePxSize = 0;
        boolean previousCode = false;
        boolean isBold = false;

        for (char c : message.toCharArray()) {
            if (c == '§') {
                previousCode = true;
            } else if (previousCode) {
                previousCode = false;
                isBold = c == 'l' || c == 'L';
            } else {
                messagePxSize += getCharWidth(c, isBold);
            }
        }

        int halvedMessageSize = messagePxSize / 2;
        int toCompensate = CENTER_PX - halvedMessageSize;
        int spaceLength = 4; // Default space width
        int compensated = 0;
        StringBuilder sb = new StringBuilder();

        while (compensated < toCompensate) {
            sb.append(" ");
            compensated += spaceLength;
        }

        return sb + message;
    }

    /**
     * Gets the pixel width of a character.
     * 
     * @param c The character
     * @param isBold Whether the character is bold
     * @return The pixel width
     */
    private static int getCharWidth(char c, boolean isBold) {
        int width;
        switch (c) {
            case ' ':
            case 'I':
            case 'i':
            case 'l':
            case '!':
            case '|':
            case '.':
            case '\'':
            case ',':
            case ':':
            case ';':
                width = 2;
                break;
            case 't':
            case '*':
            case '<':
            case '>':
            case '(':
            case ')':
            case '{':
            case '}':
            case '[':
            case ']':
                width = 4;
                break;
            case 'f':
            case 'k':
            case '"':
                width = 5;
                break;
            case '@':
            case '~':
                width = 7;
                break;
            default:
                width = 6;
        }

        if (isBold) {
            width++;
        }

        return width;
    }

    /**
     * Formats a location string.
     * 
     * @param x The X coordinate
     * @param y The Y coordinate
     * @param z The Z coordinate
     * @return The formatted location string
     */
    public static String formatLocation(double x, double y, double z) {
        return String.format("%.1f, %.1f, %.1f", x, y, z);
    }

    /**
     * Formats a location string with block coordinates (integers).
     * 
     * @param x The X coordinate
     * @param y The Y coordinate
     * @param z The Z coordinate
     * @return The formatted location string
     */
    public static String formatBlockLocation(int x, int y, int z) {
        return String.format("%d, %d, %d", x, y, z);
    }
}
