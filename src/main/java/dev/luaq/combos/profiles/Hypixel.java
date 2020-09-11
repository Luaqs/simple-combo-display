package dev.luaq.combos.profiles;

import java.util.Arrays;
import java.util.List;

public class Hypixel extends Profile {
    private final List<String> endingStrings = Arrays.asList(
            "1st Killer - ",
            "1st Place - ",
            "Winner: ",
            " - Damage Dealt - ",
            "Winning Team - ",
            "1st - ",
            "Winners: ",
            "Winner: ",
            "Winning Team: ",
            " won the game!",
            "Top Seeker: ",
            "1st Place: ",
            "Last team standing!",
            "Winner #1 (",
            "Top Survivors",
            "Winners - ");

    public Hypixel() {
        super("Hypixel", "(\\w+\\.)?hypixel\\.net");
    }

    @Override
    public void handleMessage(String chatMessage) {
        // if the message is "the game starts in" and it wasn't a chat message
        if (chatMessage.contains("The game starts in ") && !chatMessage.contains(":")) {
            // extract the integer (by replacing all things not an integer)
            try {
                int timeRemaining = Integer.parseInt(chatMessage.replaceAll("\\D+", ""));
                if (timeRemaining <= 10) {
                    inRound = true;
                }
            } catch (NumberFormatException ignored) {
            }
        }

        // thanks autogg <3
        if (endingStrings.stream().anyMatch(chatMessage::contains)) {
            inRound = false;
        }
    }
}
