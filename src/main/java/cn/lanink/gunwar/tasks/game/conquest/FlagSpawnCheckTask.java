package cn.lanink.gunwar.tasks.game.conquest;

import cn.lanink.gamecore.room.IRoomStatus;
import cn.lanink.gunwar.GunWar;
import cn.lanink.gunwar.entity.EntityLongFlag;
import cn.lanink.gunwar.room.base.Team;
import cn.lanink.gunwar.room.conquest.ConquestModeRoom;
import cn.lanink.gunwar.utils.FlagSkinType;
import cn.nukkit.entity.data.Skin;
import cn.nukkit.level.Position;
import cn.nukkit.math.Vector3;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.scheduler.PluginTask;

/**
 * 征服模式旗帜实体存活检查
 *
 * @author LT_Name
 */
public class FlagSpawnCheckTask extends PluginTask<GunWar> {

    private final ConquestModeRoom room;

    public FlagSpawnCheckTask(GunWar owner, ConquestModeRoom room) {
        super(owner);
        this.room = room;
    }

    @Override
    public void onRun(int i) {
        if (this.room.getStatus() != IRoomStatus.ROOM_STATUS_GAME) {
            this.cancel();
            return;
        }

        this.checkAndSpawnFlag(1);
        this.checkAndSpawnFlag(2);
        this.checkAndSpawnFlag(3);
    }

    private void checkAndSpawnFlag(int i) {
        EntityLongFlag entityLongFlag = null;
        Vector3 vector3 = null;
        switch (i) {
            case 1:
                entityLongFlag = this.room.aFlag;
                vector3 = this.room.getConquestPointA();
                break;
            case 2:
                entityLongFlag = this.room.bFlag;
                vector3 = this.room.getConquestPointB();
                break;
            case 3:
                entityLongFlag = this.room.cFlag;
                vector3 = this.room.getConquestPointC();
                break;
        }
        if (vector3 == null) {
            return;
        }
        if (entityLongFlag == null || entityLongFlag.isClosed()) {
            Skin skin = GunWar.getInstance().getFlagSkin(FlagSkinType.LONG_FLAGPOLE);
            Position position = Position.fromObject(vector3, this.room.getLevel());
            CompoundTag tag = EntityLongFlag.getDefaultNBT(position);
            tag.putCompound("Skin", new CompoundTag()
                    .putByteArray("Data", skin.getSkinData().data)
                    .putString("ModelId", skin.getSkinId()));
            entityLongFlag = new EntityLongFlag(position.getChunk(), tag, Team.NULL);
            entityLongFlag.setSkin(skin);
            entityLongFlag.spawnToAll();
            switch (i) {
                case 1:
                    this.room.aFlag = entityLongFlag;
                    break;
                case 2:
                    this.room.bFlag = entityLongFlag;
                    break;
                case 3:
                    this.room.cFlag = entityLongFlag;
                    break;
            }
        }
    }

}
