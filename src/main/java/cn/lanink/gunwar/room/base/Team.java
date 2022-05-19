package cn.lanink.gunwar.room.base;

import cn.lanink.gunwar.utils.Tools;

/**
 * @author LT_Name
 */
public enum Team {

    /**
     * 未分配队伍
     */
    NULL,

    /**
     * 红队
     */
    RED,

    /**
     * 红队(已死亡)
     */
    RED_DEATH,

    /**
     * 蓝队
     */
    BLUE,

    /**
     * 蓝队(已死亡)
     */
    BLUE_DEATH;

    public String getShowName() {
        return Tools.getShowTeamName(this);
    }

}
