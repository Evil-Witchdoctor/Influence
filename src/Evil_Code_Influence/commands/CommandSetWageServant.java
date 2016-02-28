package Evil_Code_Influence.commands;

import java.util.Set;

import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import Evil_Code_Influence.Influence;
import Evil_Code_Influence.InfluenceAPI;
import Evil_Code_Influence.master.Master;

public class CommandSetWageServant implements CommandExecutor{
	private Influence plugin;
	
	public CommandSetWageServant(){
		plugin = Influence.getPlugin();
		plugin.getCommand("setwageservant").setExecutor(this);
	}

	@SuppressWarnings("deprecation")
	public boolean onCommand(CommandSender sender, Command command, String label, String args[]){
		//cmd:   /setwageservant <Name/all>
		if(args.length < 1){
			sender.sendMessage("�cToo few arguments!");
			return false;
		}
		
		Set<OfflinePlayer> targetP;
		if(sender instanceof Player){
			OfflinePlayer p = plugin.getServer().getOfflinePlayer(args[0]);
			if(p != null && InfluenceAPI.checkIsMaster(((Player)sender).getUniqueId(), p.getUniqueId()) == false){
				sender.sendMessage("�cYou are not the master of "+p.getName());
				return true;
			}
			
			Master master = InfluenceAPI.getMasterByUUID(((Player)sender).getUniqueId());
			if(master == null){
				sender.sendMessage("�4ERROR: �cYou do not own any servants");
				return true;
			}
			targetP = CommandUtils.getTargetServants(master, args[0], true);
		}
		else targetP = CommandUtils.getTargetServants(sender, args[0], true);
		
		if(targetP.isEmpty()){
			sender.sendMessage("�cPlayer[Servant] not found!");
			return true;
		}
		
		double newWage = 0;
		try{newWage = Double.parseDouble(args[2]);}
		catch(NumberFormatException ex){}
		if(newWage < Influence.minWage()){
			sender.sendMessage("�cInvalid wage! Number must be a positive value" +
					((Influence.minWage() > 0) ? " above or equal to the minimum wage (�7"+Influence.minWage()+"�c)." : ""));
			return false;
		}
		
		for(OfflinePlayer player : targetP){
			InfluenceAPI.getServant(player.getUniqueId()).setWage(newWage);
		}
		
		return true;
	}
}