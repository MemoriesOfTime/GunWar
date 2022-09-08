package cn.lanink.gunwar.listener.defaults;

import cn.lanink.gunwar.GunWar;
import cn.lanink.gunwar.entity.EntityLongFlag;
import cn.lanink.gunwar.room.base.Team;
import cn.lanink.gunwar.utils.FlagSkinType;
import cn.nukkit.Player;
import cn.nukkit.entity.Entity;
import cn.nukkit.entity.data.Skin;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.EventPriority;
import cn.nukkit.event.Listener;
import cn.nukkit.event.player.PlayerChatEvent;
import cn.nukkit.nbt.tag.CompoundTag;

/**
 * @author LT_Name
 */
public class DebugMessageListener implements Listener {

    private GunWar gunWar;

    public DebugMessageListener(GunWar gunWar) {
        this.gunWar = gunWar;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onChat(PlayerChatEvent event) {
        Player player = event.getPlayer();
        String message = event.getMessage();
        if (player == null || message == null) {
            return;
        }

        if ("LongFlagSpawn".equalsIgnoreCase(message)) {
            Skin skin = GunWar.getInstance().getFlagSkin(FlagSkinType.LONG_FLAGPOLE);
            CompoundTag tag = EntityLongFlag.getDefaultNBT(player);
            tag.putCompound("Skin", new CompoundTag()
                    .putByteArray("Data", skin.getSkinData().data)
                    .putString("ModelId", skin.getSkinId()));
            EntityLongFlag flag = new EntityLongFlag(player.getChunk(), tag, Team.RED);
            flag.setSkin(skin);
            flag.spawnToAll();
        } else if ("LongFlag+".equalsIgnoreCase(message)) {
            for (Entity entity : player.getLevel().getEntities()) {
                if (entity instanceof EntityLongFlag) {
                    EntityLongFlag longFlag = (EntityLongFlag) entity;
                    longFlag.setFlagHeight(longFlag.getFlagHeight() + 10);
                    this.gunWar.getLogger().info("[debug] LongFlag Now Height" + longFlag.getFlagHeight());
                }
            }
        } else if ("LongFlag-".equalsIgnoreCase(message)) {
            for (Entity entity : player.getLevel().getEntities()) {
                if (entity instanceof EntityLongFlag) {
                    EntityLongFlag longFlag = (EntityLongFlag) entity;
                    longFlag.setFlagHeight(longFlag.getFlagHeight() - 10);
                    this.gunWar.getLogger().info("[debug] LongFlag Now Height" + longFlag.getFlagHeight());
                }
            }
        }
    }
}
