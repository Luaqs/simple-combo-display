package dev.luaq.combos.profiles;

import java.util.regex.Pattern;

public abstract class Profile {
    private final String name;
    private final Pattern ipRegex;

    protected boolean inRound = false;

    protected Profile(String name, String ipRegex) {
        this.name = name;
        this.ipRegex = Pattern.compile(ipRegex);
    }

    abstract public void handleMessage(String chatMessage);

    public final void setInRound(boolean inRound) {
        this.inRound = inRound;
    }

    public final boolean inRound() {
        return inRound;
    }

    public String getName() {
        return name;
    }

    public final Pattern getIpRegex() {
        return ipRegex;
    }
}
