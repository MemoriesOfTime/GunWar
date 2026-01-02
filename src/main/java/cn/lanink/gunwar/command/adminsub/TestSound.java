package cn.lanink.gunwar.command.adminsub;

import cn.lanink.gunwar.command.base.BaseSubCommand;
import cn.lanink.gunwar.utils.Tools;
import cn.nukkit.Player;
import cn.nukkit.command.CommandSender;
import cn.nukkit.command.data.CommandParamType;
import cn.nukkit.command.data.CommandParameter;

/**
 * Test custom resource pack sounds.
 */
public class TestSound extends BaseSubCommand {

    private static final String DEFAULT_SOUND = "gunwar.kill";

    public TestSound(String name) {
        super(name);
    }

    @Override
    public boolean canUser(CommandSender sender) {
        return sender.isPlayer() && sender.isOp();
    }

    @Override
    public String[] getAliases() {
        return new String[]{"testsound", "soundtest"};
    }

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        Player player = (Player) sender;
        String soundName = DEFAULT_SOUND;
        if (args.length > 1 && args[1] != null && !args[1].trim().isEmpty()) {
            soundName = args[1].trim();
        }

        Tools.playSound(player, soundName);

        sender.sendMessage("§a已播放音效: §e" + soundName);
        return true;
    }

    @Override
    public CommandParameter[] getParameters() {
        return new CommandParameter[]{
                CommandParameter.newType("sound", true, CommandParamType.STRING)
        };
    }

}
