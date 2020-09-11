package dev.luaq.combos.profiles;

public class Minemen extends Profile {
    public Minemen() {
        super("Minemen", "(((na|eu|sa)\\.)?minemen\\.club|icantjoinlmfao\\.club)");
    }

    @Override
    public void handleMessage(String chatMessage) {
        if (chatMessage.equals("The match has started!")) {
            inRound = true;
            return;
        }

        if (chatMessage.startsWith("Winner: ")) {
            inRound = false;
        }
    }
}
