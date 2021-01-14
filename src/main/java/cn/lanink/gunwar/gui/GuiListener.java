package cn.lanink.gunwar.gui;

import cn.lanink.gamecore.utils.Language;
import cn.lanink.gunwar.GunWar;
import cn.lanink.gunwar.item.ItemManage;
import cn.lanink.gunwar.item.weapon.GunWeapon;
import cn.lanink.gunwar.item.weapon.MeleeWeapon;
import cn.lanink.gunwar.item.weapon.ProjectileWeapon;
import cn.lanink.gunwar.utils.exception.item.weapon.ProjectileWeaponLoadException;
import cn.lanink.gunwar.utils.gamerecord.RecordType;
import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.player.PlayerFormRespondedEvent;
import cn.nukkit.form.window.FormWindowCustom;
import cn.nukkit.form.window.FormWindowModal;
import cn.nukkit.form.window.FormWindowSimple;
import cn.nukkit.utils.Config;

import java.io.File;
import java.util.LinkedList;

/**
 * @author lt_name
 */
public class GuiListener implements Listener {

    private final GunWar gunWar;
    private final Language language;

    public GuiListener(GunWar gunWar) {
        this.gunWar = gunWar;
        this.language = gunWar.getLanguage();
    }

    /**
     * 玩家操作ui事件
     * @param event 事件
     */
    @EventHandler
    public void onPlayerFormResponded(PlayerFormRespondedEvent event) {
        Player player = event.getPlayer();
        if (player == null || event.getWindow() == null) {
            return;
        }
        GuiType guiType = GuiCreate.UI_CACHE.containsKey(player) ? GuiCreate.UI_CACHE.get(player).get(event.getFormID()) : null;
        if (guiType == null) {
            return;
        }
        GuiCreate.UI_CACHE.get(player).remove(event.getFormID());
        if (event.getResponse() == null) {
            return;
        }
        if (event.getWindow() instanceof FormWindowSimple) {
            this.onClick(player, (FormWindowSimple) event.getWindow(), guiType);
        }else if (event.getWindow() instanceof FormWindowCustom) {
            this.onClick(player, (FormWindowCustom) event.getWindow(), guiType);
        }else if (event.getWindow() instanceof FormWindowModal) {
            this.onClick(player, (FormWindowModal) event.getWindow(), guiType);
        }
    }

    private void onClick(Player player, FormWindowSimple simple, GuiType guiType) {
        switch (guiType) {
            case USER_MENU:
                switch (simple.getResponse().getClickedButtonId()) {
                    case 0:
                        GunWar.getInstance().getServer().dispatchCommand(player, this.gunWar.getCmdUser() + " join");
                        break;
                    case 1:
                        GunWar.getInstance().getServer().dispatchCommand(player, this.gunWar.getCmdUser() + " quit");
                        break;
                    case 2:
                        GuiCreate.sendRoomListMenu(player);
                        break;
                    case 3:
                        GuiCreate.sendRecordList(player);
                        break;
                }
                break;
            case ROOM_LIST_MENU:
                if (simple.getResponse().getClickedButton().getText().equals(language.translateString("buttonReturn"))) {
                    GuiCreate.sendUserMenu(player);
                }else {
                    GuiCreate.sendRoomJoinOkMenu(player, simple.getResponse().getClickedButton().getText().split("\n")[0]);
                }
                break;
            case RECORD_LIST:
                switch (simple.getResponse().getClickedButtonId()) {
                    case 0:
                        GuiCreate.sendGameRecord(player);
                        break;
                    case 1:
                        GuiCreate.sendRankingList(player, RecordType.KILLS);
                        break;
                    case 2:
                        GuiCreate.sendRankingList(player, RecordType.DEATHS);
                        break;
                    case 3:
                        GuiCreate.sendRankingList(player, RecordType.VICTORY);
                        break;
                    case 4:
                        GuiCreate.sendRankingList(player, RecordType.DEFEAT);
                        break;
                    case 5:
                        GuiCreate.sendUserMenu(player);
                        break;
                }
                break;
            case ADMIN_MENU:
                switch (simple.getResponse().getClickedButtonId()) {
                    case 0:
                        Server.getInstance().dispatchCommand(player, this.gunWar.getCmdAdmin() + " CreateRoom");
                        break;
                    case 1:
                        Server.getInstance().dispatchCommand(player, this.gunWar.getCmdAdmin() + " SetRoom");
                        break;
                    case 2:
                        Server.getInstance().dispatchCommand(player, this.gunWar.getCmdAdmin() + " reloadroom");
                        break;
                    case 3:
                        Server.getInstance().dispatchCommand(player, this.gunWar.getCmdAdmin() + " unloadroom");
                        break;
                }
                break;
            case ADMIN_CREATE_ROOM_MENU:
                Server.getInstance().dispatchCommand(player,
                        this.gunWar.getCmdAdmin() + " CreateRoom " +
                                simple.getResponse().getClickedButton().getText());
                break;
            case ADMIN_SET_ROOM_MENU:
                Server.getInstance().dispatchCommand(player,
                        this.gunWar.getCmdAdmin() + " SetRoom " +
                                simple.getResponse().getClickedButton().getText());
                break;
            case  ADMIN_ITEM_ADD_WEAPON:
                switch (simple.getResponse().getClickedButtonId()) {
                    case 0:
                        GuiCreate.sendAdminItemAddWeaponMeleeMenu(player);
                        break;
                    case 1:
                        GuiCreate.sendAdminItemAddWeaponProjectileMenu(player);
                        break;
                    case 2:
                        GuiCreate.sendAdminItemAddWeaponGunMenu(player);
                        break;
                }
                break;
        }
    }

    private void onClick(Player player, FormWindowCustom custom, GuiType guiType) {
        switch (guiType) {
            case ADMIN_TIME_MENU:
                try {
                    int waitTime = Integer.parseInt(custom.getResponse().getInputResponse(0));
                    int gameTime = Integer.parseInt(custom.getResponse().getInputResponse(1));
                    int victoryScore = Integer.parseInt(custom.getResponse().getInputResponse(2));
                    if (waitTime < 1 || gameTime < 1 || victoryScore < 1) {
                        throw new Exception("");
                    }
                    Config config = this.gunWar.getRoomConfig(player.getLevel());
                    config.set("waitTime", waitTime);
                    config.set("gameTime", gameTime);
                    config.set("victoryScore", victoryScore);
                    config.save();
                    player.sendMessage(this.language.translateString("adminSetWaitTime", waitTime));
                    player.sendMessage(this.language.translateString("adminSetGameTime", gameTime));
                    player.sendMessage(this.language.translateString("adminSetVictoryScore", victoryScore));
                } catch (Exception e) {
                    player.sendMessage(this.language.translateString("adminNotNumber"));
                }
                break;
            case ADMIN_PLAYERS_MENU:
                try {
                    int minPlayers = Integer.parseInt(custom.getResponse().getInputResponse(0));
                    int maxPlayers = Integer.parseInt(custom.getResponse().getInputResponse(1));
                    if (minPlayers < 1 || maxPlayers < 2) {
                        throw new Exception("");
                    }
                    Config config = this.gunWar.getRoomConfig(player.getLevel());
                    config.set("minPlayers", minPlayers);
                    config.set("maxPlayers", maxPlayers);
                    config.save();
                    player.sendMessage(this.language.translateString("adminSetMinPlayers", minPlayers));
                    player.sendMessage(this.language.translateString("adminSetMaxPlayers", maxPlayers));
                } catch (Exception e) {
                    player.sendMessage(this.language.translateString("adminNotNumber"));
                }
                break;
            case ADMIN_MODE_MENU:
                String gameMode = custom.getResponse().getDropdownResponse(0).getElementContent();
                if (GunWar.getRoomClass().containsKey(gameMode)) {
                    Config config = this.gunWar.getRoomConfig(player.getLevel());
                    config.set("gameMode", gameMode);
                    config.save();
                    player.sendMessage(this.language.translateString("adminSetGameMode", gameMode));
                }else {
                    player.sendMessage(this.language.translateString("gameMode_NotFound", gameMode));
                }
                break;
            case ADMIN_ITEM_ADD_WEAPON_MELEE:
                this.adminItemAddWeaponMelee(player, custom);
                break;
            case ADMIN_ITEM_ADD_WEAPON_PROJECTILE:
                this.adminItemAddWeaponProjectile(player, custom);
                break;
            case ADMIN_ITEM_ADD_WEAPON_GUN:
                this.adminItemAddWeaponGun(player, custom);
                break;
        }
    }

    private void onClick(Player player, FormWindowModal modal, GuiType guiType) {
        switch (guiType) {
            case ROOM_JOIN_OK:
                if (modal.getResponse().getClickedButtonId() == 0 && !modal.getButton1().equals(language.translateString("buttonReturn"))) {
                    String[] s = modal.getContent().split("\"");
                    GunWar.getInstance().getServer().dispatchCommand(
                            player, this.gunWar.getCmdUser() + " join " + s[1].replace("§e", "").trim());
                }else {
                    GuiCreate.sendRoomListMenu(player);
                }
                break;
            case GAME_RECORD:
            case RANKING_LIST:
                if (modal.getResponse().getClickedButtonId() == 1) {
                    GuiCreate.sendRecordList(player);
                }
                break;
        }
    }

    private void adminItemAddWeaponMelee(Player player, FormWindowCustom custom) {
        String name = custom.getResponse().getInputResponse(0);
        File file = new File(this.gunWar.getItemManage().getMeleeWeaponFolder() + "/" + name + ".yml");
        if (file.exists()) {
            player.sendMessage(this.language.translateString("gui_admin_item_add_error_exist", name));
            return;
        }
        Config config = new Config(Config.YAML);
        config.set("showName", custom.getResponse().getInputResponse(1));
        try {
            String stringID = custom.getResponse().getInputResponse(2);
            String[] split = stringID.split(":");
            if (split.length == 2) {
                Integer.parseInt(split[0]);
                Integer.parseInt(split[1]);
            }else {
                Integer.parseInt(stringID);
            }
            config.set("id", stringID);
        } catch (Exception e) {
            player.sendMessage(this.language.translateString("gui_admin_item_add_error_var",
                            name, this.language.translateString("gui_admin_item_id")));
            return;
        }
        config.set("lore", custom.getResponse().getInputResponse(3));
        try {
            String minDamage = custom.getResponse().getInputResponse(4);
            String maxDamage = custom.getResponse().getInputResponse(5);
            Integer.parseInt(minDamage);
            Integer.parseInt(maxDamage);
            config.set("damage", minDamage + "-" + maxDamage);
        } catch (NumberFormatException e) {
            player.sendMessage(this.language.translateString("gui_admin_item_add_error_var",
                    name, this.language.translateString("gui_admin_item_weapon_minDamage") + "/" +
                            this.language.translateString("gui_admin_item_weapon_maxDamage")));
            return;
        }
        config.set("effect", new LinkedList<>());
        try {
            config.set("attackCooldown", Integer.parseInt(custom.getResponse().getInputResponse(6)));
        } catch (NumberFormatException e) {
            player.sendMessage(this.language.translateString("gui_admin_item_add_error_var",
                    name, this.language.translateString("gui_admin_item_weapon_attackCooldown")));
            return;
        }
        try {
            config.set("knockBack", Double.parseDouble(custom.getResponse().getInputResponse(7)));
        } catch (NumberFormatException e) {
            player.sendMessage(this.language.translateString("gui_admin_item_add_error_var",
                    name, this.language.translateString("gui_admin_item_weapon_knockBack"))
                    .replace("%name%", name));
            return;
        }
        config.set("infiniteDurability", custom.getResponse().getToggleResponse(8));
        config.set("enchantment", new LinkedList<>());
        config.set("killMessage", custom.getResponse().getInputResponse(9));
        config.save(file, true);
        ItemManage.getMeleeWeaponMap().put(name, new MeleeWeapon(name, config));
        player.sendMessage(this.language.translateString("gui_admin_item_add_success", name));
    }

    private void adminItemAddWeaponProjectile(Player player, FormWindowCustom custom) {
        String name = custom.getResponse().getInputResponse(0);
        File file = new File(this.gunWar.getItemManage().getProjectileWeaponFolder() + "/" + name + ".yml");
        if (file.exists()) {
            player.sendMessage(this.language.translateString("gui_admin_item_add_error_exist", name));
            return;
        }
        Config config = new Config(Config.YAML);
        config.set("showName", custom.getResponse().getInputResponse(1));
        try {
            String stringID = custom.getResponse().getInputResponse(2);
            String[] split = stringID.split(":");
            if (split.length == 2) {
                Integer.parseInt(split[0]);
                Integer.parseInt(split[1]);
            }else {
                Integer.parseInt(stringID);
            }
            config.set("id", stringID);
        } catch (Exception e) {
            player.sendMessage(this.language.translateString("gui_admin_item_add_error_var",
                    name, this.language.translateString("gui_admin_item_id"))
                    .replace("%name%", name));
            return;
        }
        config.set("lore", custom.getResponse().getInputResponse(3));
        try {
            String minDamage = custom.getResponse().getInputResponse(4);
            String maxDamage = custom.getResponse().getInputResponse(5);
            Integer.parseInt(minDamage);
            Integer.parseInt(maxDamage);
            config.set("damage", minDamage + "-" + maxDamage);
        } catch (NumberFormatException e) {
            player.sendMessage(this.language.translateString("gui_admin_item_add_error_var",
                    name, this.language.translateString("gui_admin_item_weapon_minDamage") + "/" +
                            this.language.translateString("gui_admin_item_weapon_maxDamage")));
            return;
        }
        config.set("effect", new LinkedList<>());
        config.set("particle", custom.getResponse().getInputResponse(6));
        try {
            config.set("attackCooldown", Integer.parseInt(custom.getResponse().getInputResponse(7)));
        } catch (NumberFormatException e) {
            player.sendMessage(this.language.translateString("gui_admin_item_add_error_var",
                    name, this.language.translateString("gui_admin_item_weapon_attackCooldown")));
            return;
        }
        try {
            config.set("range", Double.parseDouble(custom.getResponse().getInputResponse(8)));
        } catch (NumberFormatException e) {
            player.sendMessage(this.language.translateString("gui_admin_item_add_error_var",
                    name, this.language.translateString("gui_admin_item_weapon_range")));
            return;
        }
        config.set("enchantment", new LinkedList<>());
        config.set("killMessage", custom.getResponse().getInputResponse(9));
        config.save(file, true);
        try {
            ItemManage.getProjectileWeaponMap().put(name, new ProjectileWeapon(name, config));
        } catch (ProjectileWeaponLoadException e) {
            e.printStackTrace();
        }
        player.sendMessage(this.language.translateString("gui_admin_item_add_success", name));
    }

    private void adminItemAddWeaponGun(Player player, FormWindowCustom custom) {
        String name = custom.getResponse().getInputResponse(0);
        File file = new File(this.gunWar.getItemManage().getGunWeaponFolder() + "/" + name + ".yml");
        if (file.exists()) {
            player.sendMessage(this.language.translateString("gui_admin_item_add_error_exist", name));
            return;
        }
        Config config = new Config(Config.YAML);
        config.set("showName", custom.getResponse().getInputResponse(1));
        try {
            String stringID = custom.getResponse().getInputResponse(2);
            String[] split = stringID.split(":");
            if (split.length == 2) {
                Integer.parseInt(split[0]);
                Integer.parseInt(split[1]);
            }else {
                Integer.parseInt(stringID);
            }
            config.set("id", stringID);
        } catch (Exception e) {
            player.sendMessage(this.language.translateString("gui_admin_item_add_error_var",
                    name, this.language.translateString("gui_admin_item_id")));
            return;
        }
        config.set("lore", custom.getResponse().getInputResponse(3));
        try {
            String minDamage = custom.getResponse().getInputResponse(4);
            String maxDamage = custom.getResponse().getInputResponse(5);
            Integer.parseInt(minDamage);
            Integer.parseInt(maxDamage);
            config.set("damage", minDamage + "-" + maxDamage);
        } catch (NumberFormatException e) {
            player.sendMessage(this.language.translateString("gui_admin_item_add_error_var",
                    name, this.language.translateString("gui_admin_item_weapon_minDamage") + "/" +
                            this.language.translateString("gui_admin_item_weapon_maxDamage")));
            return;
        }
        config.set("effect", new LinkedList<>());
        try {
            config.set("attackCooldown", Integer.parseInt(custom.getResponse().getInputResponse(6)));
        } catch (NumberFormatException e) {
            player.sendMessage(this.language.translateString("gui_admin_item_add_error_var",
                    name, this.language.translateString("gui_admin_item_weapon_attackCooldown")));
            return;
        }
        try {
            config.set("maxMagazine", Integer.parseInt(custom.getResponse().getInputResponse(7)));
        } catch (NumberFormatException e) {
            player.sendMessage(this.language.translateString("gui_admin_item_add_error_var",
                    name, this.language.translateString("gui_admin_item_weapon_maxMagazine")));
            return;
        }
        try {
            config.set("reloadTime", Integer.parseInt(custom.getResponse().getInputResponse(8)));
        } catch (NumberFormatException e) {
            player.sendMessage(this.language.translateString("gui_admin_item_add_error_var",
                    name, this.language.translateString("gui_admin_item_weapon_reloadTime")));
            return;
        }
        config.set("reloadInterrupted", custom.getResponse().getToggleResponse(9));
        try {
            config.set("gravity", Double.parseDouble(custom.getResponse().getInputResponse(10)));
        } catch (NumberFormatException e) {
            player.sendMessage(this.language.translateString("gui_admin_item_add_error_var",
                    name, this.language.translateString("gui_admin_item_weapon_bulletGravity")));
            return;
        }
        try {
            config.set("motionMultiply", Double.parseDouble(custom.getResponse().getInputResponse(11)));
        } catch (NumberFormatException e) {
            player.sendMessage(this.language.translateString("gui_admin_item_add_error_var",
                    name, this.language.translateString("gui_admin_item_weapon_bulletMotionMultiply")));
            return;
        }
        config.set("enchantment", new LinkedList<>());
        config.set("particleEffect", "");
        config.set("killMessage", custom.getResponse().getInputResponse(12));
        config.save(file, true);
        ItemManage.getGunWeaponMap().put(name, new GunWeapon(name, config));
        player.sendMessage(this.language.translateString("gui_admin_item_add_success", name));
    }

}