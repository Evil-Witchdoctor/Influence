package Evil_Code_Influence;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

import Evil_Code_Influence.commands.CommandManager;
import Evil_Code_Influence.commands.CommandUtils;
import Evil_Code_Influence.listeners.Listener_PlayerAction;
import Evil_Code_Influence.listeners.Listener_PlayerChat;
import Evil_Code_Influence.master.Master;
import Evil_Code_Influence.servant.AbilityConfig;
import Evil_Code_Influence.servant.Servant;
import Evil_Code_Influence.servant.AbilityConfig.Ability;

public final class Influence extends JavaPlugin{
	private static Influence plugin;
	private ConfigSettings config;
	public final static String prefix = "�8[�2Ifl�8] �f";
	protected Map<UUID, Master> masterList = new HashMap<UUID, Master>();
	final int projectID=0;//= ???; TODO: Need to find this out when uploaded to bukkit.com!
	
	@Override public void onEnable(){
		getLogger().info("Loading " + getDescription().getFullName());
		//this= plugin instance
		//projectID = bukkit id
		//this.getFile() = the file to replace/update
		//UpdateType = update type
		//boolean = whether or not to log update progress %s to console
		//TODO: upload to bukkit and then uncomment this
//		new Updater(this, projectID, this.getFile(), Updater.UpdateType.DEFAULT, true);
		plugin = this;
		config = new ConfigSettings(this);
		
		loadFiles();
		new InfluenceAPI();
		registerCommands();
		if(!masterList.isEmpty()) registerEvents();
	}
	@Override public void onDisable(){
//		for(Master master : masterList.values()) InfluenceAPI.saveMasterPreferences(master.getPlayerUUID(), master.getPreferences());
		saveFiles();
	}
	
	private void registerCommands(){
		new CommandUtils();
		new CommandManager();
	}
	private void registerEvents(){
		new Listener_PlayerAction();
		new Listener_PlayerChat();
	}
	private void unregisterEvents(){
		HandlerList.unregisterAll(this);
	}
 	
	private void loadFiles(){
//		getConfig().options().copyDefaults(true);
		BufferedReader reader = null;
		try{reader = new BufferedReader(new FileReader("./plugins/EvFolder/masters-servants.txt"));}
		catch(FileNotFoundException e){
			//Create Directory
			File dir = new File("./plugins/EvFolder");
			if(!dir.exists())dir.mkdir();
			return;
		}
		
		String line = null;
		Master master = null;
		UUID mUUID=null, sUUID=null;
		try{
			while((line = reader.readLine()) != null){
				line = line.replace(" ", "").toLowerCase();
				if(line.startsWith("m|")){
					
					// In the event that the last master "scanned in" had no servants attributed to them
					if(master != null){
						if(master.getServants().size() == 0) InfluenceAPI.saveEmptyMasterPreferences(mUUID, master.getPreferences());
						else masterList.put(mUUID, master);
					}
					
					try{
						mUUID = UUID.fromString(line.split("\\|")[1]);
					}catch(IllegalArgumentException ex1){
						getLogger().warning("ERROR: Could not load the masterlist, please check the file for errors");
						continue;
					}
					catch(ArrayIndexOutOfBoundsException ex2){
						getLogger().warning("ERROR: Could not load the masterlist, please check the file for errors");
						continue;
					}
					
					AbilityConfig preferences = null;
					// If they have custom AbilityConfig preferences for servants, load it & add them with it
					if(line.contains("{") && line.contains("}")){
						preferences = new AbilityConfig(false);
						line = ','+line.substring(line.indexOf("{")+1, line.indexOf("}"))+',';
						
						for(Ability ability : AbilityConfig.Ability.values()){
							preferences.setAbility(ability, line.contains(','+ability.name()+','));
						}
					}
					master = new Master(mUUID, preferences);
				}
				else if(master != null && line.startsWith("s|")){
					sUUID = null;
					try{
						sUUID = UUID.fromString(line.split("\\|")[1]);
					}catch(IllegalArgumentException ex1){
						getLogger().warning("ERROR: Could not load the masterlist, please check the file for errors");
						continue;
					}
					catch(ArrayIndexOutOfBoundsException ex2){
						getLogger().warning("ERROR: Could not load the masterlist, please check the file for errors");
						continue;
					}
					
					// If they have a custom AbilityConfig, load it & add them with it
					if(line.contains("{") && line.contains("}")){
						line = ','+line.substring(line.indexOf("{")+1, line.indexOf("}"))+',';
						
						AbilityConfig abilities = new AbilityConfig(false);
						for(Ability ability : AbilityConfig.Ability.values()){
							abilities.setAbility(ability, line.contains(','+ability.name()+','));
						}
						master.addServant(new Servant(sUUID, mUUID, abilities), true);//force=true to skip rank checking
					}
					else master.addServant(sUUID, true);//force=true to skip rank checking
				}
			}
			reader.close();
			if(master != null){
				if(master.getServants().size() == 0) InfluenceAPI.saveEmptyMasterPreferences(mUUID, master.getPreferences());
				else masterList.put(mUUID, master);
			}
		}
		catch(IOException e){}
		getLogger().info("Enable complete!  ["+masterList.size()+"] master profiles loaded");
    }
	
	private void saveFiles(){
		/** Example File:
		 * 
		 * MasterList:
		 *    m|134534-143f1-134rf134|Setteal:
		 *          s|134134-13414-134143-134143|FoofPuss
		 *          s|q34134-13413-134543-3434f4|DiamondBlocks{break_blocks,sleep,eat,attack}
		 *          s|345345-asdgg-34f43f-ah34t3|pwu1
		 *    
		 *    m|asgg5g-5234v-34t53535|lekrosa{place_blocks,break_blocks}:
		 *          s|344334-34f4f-13g5hh-5234f4|Evil_Witchdoctor
		 * 
		*/
		StringBuilder file = new StringBuilder("Master List: \n");
		List<Master> masters = new ArrayList<Master>(); masters.addAll(masterList.values());
		
		masters.sort(new Comparator<Master>() {
			@Override
			public int compare(final Master m1, final Master m2) {
				return (m1.getPlayerUUID().toString().compareTo(m2.getPlayerUUID().toString()));
			}
		});
		
		for(Master master : masters){
			//Write master
			file.append("  m|"); file.append(master.getPlayerUUID().toString());
			file.append('|'); file.append(getServer().getOfflinePlayer(master.getPlayerUUID()).getName());
			if(master.getPreferences() != null){
//				// the uuid of the master and the abilities he/she allows servants to use by default
//				 *  
//				 *  134sdf-34f32-3fec3rc-fl35b{break_blocks,eat,sleep,teleport,attack_monsters,attack_animals}
				file.append('{');
				boolean hasAny = false;
				for(Ability ability : Ability.values()){
					if(master.getPreferences().hasAbility(ability)){
						if(hasAny) file.append(',');
						else hasAny = true;
						file.append(ability.name());
					}
				}
				file.append('}');
			}
			file.append(":\n");
			
			for(Servant servant : master.getServants()){
				// Write servant
				file.append("      s|"); file.append(servant.getPlayerUUID().toString());
				file.append('|'); file.append(getServer().getOfflinePlayer(servant.getPlayerUUID()).getName());
				
				if(servant.getAbilityConfig().equals(master.getPreferences()) == false){
					file.append('{');
					boolean hasAny = false;
					for(Ability ability : Ability.values()){
						if(servant.getAbilityConfig().hasAbility(ability)){
							if(hasAny) file.append(',');
							else hasAny = true;
							file.append(ability.name());
						}
					}
					file.append('}');
				}
				file.append('\n');
			}
			file.append('\n');
		}
		FileIO.saveFile("masters-servants.txt", file.toString());
	}
	
	// Methods called by other classes
	public boolean isServant(UUID playerUUID){
		for(Master master : masterList.values()) if(master.hasServant(playerUUID)) return true;
		return false;
	}
	
	public Servant getServant(UUID playerUUID){
		for(Master master : masterList.values()) if(master.hasServant(playerUUID)) return master.getServant(playerUUID);
		return null;
	}
	
	public boolean hasServants(UUID playerUUID){
		return (masterList.containsKey(playerUUID) && masterList.get(playerUUID).getServants().size() > 0);
	}
	
	public void addMaster(UUID masterUUID, Master master){
		if(masterList.size() == 0) registerEvents();
		masterList.put(masterUUID, master);
	}
	
	public void removeMaster(UUID masterUUID){
		masterList.remove(masterUUID);
		if(masterList.size() == 0) unregisterEvents();
	}
	
	public ConfigSettings getConfigSettings(){return config;}
	
	public static Influence getPlugin(){return plugin;}
}