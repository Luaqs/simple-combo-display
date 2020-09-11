package dev.luaq.combos.profiles;

public class PvpLand extends Profile {
    public PvpLand() {
        super("PvP Land", "pvp\\.land");
    }

    @Override
    public void handleMessage(String chatMessage) {
        if (chatMessage.equals("The match has started!")) {
            inRound = true;
            return;
        }

        if (chatMessage.equals("The match has ended!")) {
            inRound = false;
        }
    }
}
