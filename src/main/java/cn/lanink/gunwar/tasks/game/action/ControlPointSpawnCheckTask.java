package cn.lanink.gunwar.tasks.game.action;

import cn.lanink.gamecore.room.IRoomStatus;
import cn.lanink.gunwar.GunWar;
import cn.lanink.gunwar.entity.flag.EntityLongFlag;
import cn.lanink.gunwar.room.action.ActionModeRoom;
import cn.lanink.gunwar.room.base.Team;
import cn.lanink.gunwar.utils.FlagSkinType;
import cn.nukkit.entity.data.Skin;
import cn.nukkit.level.Position;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.scheduler.PluginTask;

/**
 * 控制点旗帜实体生成检查任务
 * 确保控制点的旗帜实体始终存在
 *
 * @author LT_Name
 */
public class ControlPointSpawnCheckTask extends PluginTask<GunWar> {

    private final ActionModeRoom room;

    public ControlPointSpawnCheckTask(GunWar owner, ActionModeRoom room) {
        super(owner);
        this.room = room;
    }

    @Override
    public void onRun(int i) {
        if (this.room.getStatus() != IRoomStatus.ROOM_STATUS_GAME) {
            this.cancel();
            return;
        }

        // 只为当前激活的区域生成旗帜
        ActionModeRoom.Zone currentZone = this.room.getCurrentZone();
        if (currentZone == null || currentZone.isCaptured()) {
            return;
        }

        // 检查并生成当前区域的所有控制点旗帜
        for (ActionModeRoom.ControlPoint controlPoint : currentZone.getControlPoints()) {
            this.checkAndSpawnFlag(controlPoint);
        }
    }

    /**
     * 检查并生成旗帜
     *
     * @param controlPoint 控制点
     */
    private void checkAndSpawnFlag(ActionModeRoom.ControlPoint controlPoint) {
        if (controlPoint.isCaptured()) {
            // 已占领的控制点不需要旗帜
            if (controlPoint.getFlag() != null && !controlPoint.getFlag().isClosed()) {
                controlPoint.getFlag().close();
            }
            return;
        }

        EntityLongFlag flag = controlPoint.getFlag();
        if (flag == null || flag.isClosed()) {
            // 创建新的旗帜实体
            Skin skin = GunWar.getInstance().getFlagSkin(FlagSkinType.LONG_FLAGPOLE);
            Position position = Position.fromObject(controlPoint.getPosition(), this.room.getLevel());
            CompoundTag tag = EntityLongFlag.getDefaultNBT(position);
            tag.putCompound("Skin", new CompoundTag()
                    .putByteArray("Data", skin.getSkinData().data)
                    .putString("ModelId", skin.getSkinId()));

            flag = new EntityLongFlag(position.getChunk(), tag, Team.NULL);
            flag.setSkin(skin);
            flag.spawnToAll();

            controlPoint.setFlag(flag);
        }
    }
}
