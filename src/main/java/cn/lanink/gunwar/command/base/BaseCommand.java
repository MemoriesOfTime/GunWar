package cn.lanink.gunwar.command.base;

import cn.lanink.gamecore.utils.Language;
import cn.lanink.gunwar.GunWar;
import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import cn.nukkit.command.data.CommandParameter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author SmallasWater
 */
abstract public class BaseCommand extends Command {

    private final ArrayList<BaseSubCommand> subCommand = new ArrayList<>();
    private final ConcurrentHashMap<String, Integer> subCommands = new ConcurrentHashMap<>();
    protected GunWar gunWar = GunWar.getInstance();
    protected Language language = gunWar.getLanguage();

    public BaseCommand(String name, String description) {
        super(name.toLowerCase(), description);
    }

    /**
     * 判断权限
     * @param sender 玩家
     * @return 是否拥有权限
     */
    public boolean hasPermission(CommandSender sender) {
        return sender.hasPermission(this.getPermission());
    }

    @Override
    public boolean execute(CommandSender sender, String s, String[] args) {
        if(hasPermission(sender)) {
            if(args.length > 0) {
                String subCommand = args[0].toLowerCase();
                if (subCommands.containsKey(subCommand)) {
                    BaseSubCommand command = this.subCommand.get(this.subCommands.get(subCommand));
                    if (command.canUser(sender)) {
                        return command.execute(sender, s, args);
                    }else if (sender.isPlayer()) {
                        sender.sendMessage(this.language.translateString("noPermission"));
                    }else {
                        sender.sendMessage(this.language.translateString("useCmdInCon"));
                        return true;
                    }
                }else {
                    this.sendHelp(sender);
                    return true;
                }
            }else {
                if (sender.isPlayer()) {
                    this.sendUI(sender);
                }else {
                    this.sendHelp(sender);
                }
                return true;
            }
        }
        sender.sendMessage(this.language.translateString("noPermission"));
        return true;
    }

    /**
     * 发送帮助
     * @param sender 玩家
     * */
    public abstract void sendHelp(CommandSender sender);

    /**
     * 发送UI
     * @param sender 玩家
     */
    public abstract void sendUI(CommandSender sender);

    protected void addSubCommand(BaseSubCommand cmd) {
        this.subCommand.add(cmd);
        int commandId = (this.subCommand.size()) - 1;
        this.subCommands.put(cmd.getName().toLowerCase(), commandId);
        for (String alias : cmd.getAliases()) {
            this.subCommands.put(alias.toLowerCase(), commandId);
        }
    }

    protected void loadCommandBase(){
        this.commandParameters.clear();
        for(BaseSubCommand subCommand : this.subCommand){
            LinkedList<CommandParameter> parameters = new LinkedList<>();
            parameters.add(new CommandParameter(subCommand.getName(), new String[]{subCommand.getName()}));
            parameters.addAll(Arrays.asList(subCommand.getParameters()));
            this.commandParameters.put(subCommand.getName(),parameters.toArray(new CommandParameter[0]));
        }
    }

}
