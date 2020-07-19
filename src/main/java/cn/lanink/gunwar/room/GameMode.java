package cn.lanink.gunwar.room;

public enum GameMode {
    CLASSIC("classic"), //经典
    CTF("ctf"); //夺旗

    private final String name;

    GameMode(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }
}
