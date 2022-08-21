package cn.lanink.gunwar.utils.gamerecord;

/**
 * @author lt_name
 */
public enum RecordType {
    KILLS("kills"),
    DEATHS("deaths"),
    VICTORY("victory"),
    DEFEAT("defeat");

    private final String name;

    RecordType(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public static RecordType of(String name) {
        for (RecordType type : values()) {
            if (type.name.equalsIgnoreCase(name)) { //忽略大小写
                return type;
            }
        }
        return null;
    }

}
