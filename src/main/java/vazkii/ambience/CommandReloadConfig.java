package vazkii.ambience;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentText;
import net.minecraft.world.World;

public class CommandReloadConfig extends CommandBase {
	@Override
	public String getCommandName() {
		return "ambience_reload";
	}

	@Override
	public String getCommandUsage(ICommandSender sender) {
		return "ambience_reload";
	}

	@Override
	public int getRequiredPermissionLevel() {
		return 0;
	}

	@Override
	public void processCommand(ICommandSender sender, String[] args) {
		World world = sender.getEntityWorld();
		if(world.isRemote) {
			Ambience.INSTANCE.reloadSongConfig();
			sender.addChatMessage(new ChatComponentText("Reloaded Ambience song configuration."));
		}
	}
}
