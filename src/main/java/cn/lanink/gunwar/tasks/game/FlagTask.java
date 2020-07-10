package cn.lanink.gunwar.tasks.game;

import cn.lanink.gunwar.GunWar;
import cn.lanink.gunwar.entity.EntityFlag;
import cn.lanink.gunwar.entity.EntityFlagStand;
import cn.lanink.gunwar.room.Room;
import cn.nukkit.Player;
import cn.nukkit.entity.data.Skin;
import cn.nukkit.math.Vector3;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.scheduler.PluginTask;

/**
 * @author lt_name
 */
public class FlagTask extends PluginTask<GunWar> {

    private final Room room;

    public FlagTask(GunWar owner, Room room) {
        super(owner);
        owner.taskList.add(this.getTaskId());
        this.room = room;
    }

    @Override
    public void onRun(int i) {
        if (this.room.getMode() != 2) {
            this.cancel();
            return;
        }
        //红方底座
        if (room.redFlagStand == null || room.redFlagStand.isClosed()) {
            Skin skin = owner.getFlagSkin(0);
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
            Skin skin = owner.getFlagSkin(11);
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
            Skin skin = owner.getFlagSkin(0);
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
            Skin skin = owner.getFlagSkin(12);
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
        Player p;
        if (this.room.haveRedFlag != null) {
            p = this.room.haveRedFlag;
            this.room.redFlag.setPosition(new Vector3(p.getX(),
                    p.getY() + p.getEyeHeight() + 0.5, p.getZ()));
        }
        if (this.room.haveBlueFlag != null) {
            p = this.room.haveBlueFlag;
            this.room.blueFlag.setPosition(new Vector3(p.getX(),
                    p.getY() + p.getEyeHeight() + 0.5, p.getZ()));
        }
    }

    @Override
    public void cancel() {
        while (owner.taskList.contains(this.getTaskId())) {
            owner.taskList.remove(this.getTaskId());
        }
        super.cancel();
    }

}
