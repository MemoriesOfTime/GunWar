package cn.lanink.gunwar.tasks.game.ctf;

import cn.lanink.gamecore.room.IRoomStatus;
import cn.lanink.gunwar.GunWar;
import cn.lanink.gunwar.entity.EntityFlag;
import cn.lanink.gunwar.entity.EntityFlagStand;
import cn.lanink.gunwar.room.capturetheflag.CTFModeRoom;
import cn.lanink.gunwar.utils.FlagSkinType;
import cn.nukkit.entity.data.Skin;
import cn.nukkit.math.Vector3;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.scheduler.PluginTask;

/**
 * 夺旗模式 旗帜生成检查 位置检查
 *
 * @author lt_name
 */
public class FlagTask extends PluginTask<GunWar> {

    private final CTFModeRoom room;

    public FlagTask(GunWar owner, CTFModeRoom room) {
        super(owner);
        this.room = room;
    }

    @Override
    public void onRun(int i) {
        if (this.room.getStatus() != IRoomStatus.ROOM_STATUS_GAME) {
            this.cancel();
            return;
        }
        //旗帜与底座生成
        //红方底座
        if (room.redFlagStand == null || room.redFlagStand.isClosed()) {
            Skin skin = owner.getFlagSkin(FlagSkinType.FLAG_STAND_RED);
            CompoundTag nbt = EntityFlagStand.getDefaultNBT(room.getRedSpawn());
            nbt.putFloat("Scale", 1.0F);
            nbt.putCompound("Skin", new CompoundTag()
                    .putByteArray("Data", skin.getSkinData().data)
                    .putString("ModelId", skin.getSkinId()));
            nbt.putInt("GunWarTeam", 1);
            EntityFlagStand entityFlagStand = new EntityFlagStand(room.getRedSpawn().getChunk(), nbt);
            entityFlagStand.setSkin(skin);
            entityFlagStand.spawnToAll();
            room.redFlagStand = entityFlagStand;
        }
        //红方旗帜
        if (room.redFlag == null || room.redFlag.isClosed()) {
            Skin skin = owner.getFlagSkin(FlagSkinType.FLAG_RED);
            CompoundTag nbt = EntityFlag.getDefaultNBT(new Vector3(room.getRedSpawn().getX(),
                    room.getRedSpawn().getY() + 0.3D,
                    room.getRedSpawn().getZ()));
            nbt.putCompound("Skin", new CompoundTag()
                    .putByteArray("Data", skin.getSkinData().data)
                    .putString("ModelId", skin.getSkinId()));
            nbt.putFloat("Scale", 1.0F);
            nbt.putInt("GunWarTeam", 11);
            EntityFlag entityFlag = new EntityFlag(room.getRedSpawn().getChunk(), nbt);
            entityFlag.setSkin(skin);
            entityFlag.spawnToAll();
            room.redFlag = entityFlag;
        }
        //蓝方底座
        if (room.blueFlagStand == null || room.blueFlagStand.isClosed()) {
            Skin skin = owner.getFlagSkin(FlagSkinType.FLAG_STAND_BLUE);
            CompoundTag nbt = EntityFlagStand.getDefaultNBT(room.getBlueSpawn());
            nbt.putFloat("Scale", 1.0F);
            nbt.putCompound("Skin", new CompoundTag()
                    .putByteArray("Data", skin.getSkinData().data)
                    .putString("ModelId", skin.getSkinId()));
            nbt.putInt("GunWarTeam", 2);
            EntityFlagStand entityFlagStand = new EntityFlagStand(room.getRedSpawn().getChunk(), nbt);
            entityFlagStand.setSkin(skin);
            entityFlagStand.spawnToAll();
            room.blueFlagStand = entityFlagStand;
        }
        //蓝方旗帜
        if (room.blueFlag == null || room.blueFlag.isClosed()) {
            Skin skin = owner.getFlagSkin(FlagSkinType.FLAG_BLUE);
            CompoundTag nbt = EntityFlag.getDefaultNBT(new Vector3(room.getBlueSpawn().getX(),
                    room.getBlueSpawn().getY() + 0.3D,
                    room.getBlueSpawn().getZ()));
            nbt.putCompound("Skin", new CompoundTag()
                    .putByteArray("Data", skin.getSkinData().data)
                    .putString("ModelId", skin.getSkinId()));
            nbt.putFloat("Scale", 1.0F);
            nbt.putInt("GunWarTeam", 12);
            EntityFlag entityFlag = new EntityFlag(room.getRedSpawn().getChunk(), nbt);
            entityFlag.setSkin(skin);
            entityFlag.spawnToAll();
            room.blueFlag = entityFlag;
        }

        //旗帜移动
        if (this.room.haveRedFlag != null) {
            this.room.redFlag.setPosition(this.room.haveRedFlag.add(0, this.room.haveRedFlag.getEyeHeight() + 0.5, 0));
        }
        if (this.room.haveBlueFlag != null) {
            this.room.blueFlag.setPosition(this.room.haveBlueFlag.add(0, this.room.haveBlueFlag.getEyeHeight() + 0.5, 0));
        }
    }

}
