package dev.luaq.combos.util;

import com.mojang.realmsclient.gui.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ChatComponentText;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChatUtils {
    public static final Pattern COLOR_REGEX = Pattern.compile("&([0-9A-FK-OR])", Pattern.CASE_INSENSITIVE);

    public static void sendMessage(String message) {
        String toPrint = message;
        Matcher matcher = COLOR_REGEX.matcher(message);

        while (matcher.find()) {
            // the color char
            char match = matcher.group(1).charAt(0);
            toPrint = toPrint.replace(matcher.group(0), ChatFormatting.getByChar(match).toString());
        }

        Minecraft.getMinecraft().ingameGUI.getChatGUI().printChatMessage(new ChatComponentText(toPrint));
    }
}
