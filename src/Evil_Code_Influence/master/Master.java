package Evil_Code_Influence.master;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import Evil_Code_Influence.InfluenceAPI;
import Evil_Code_Influence.servant.AbilityConfig;
import Evil_Code_Influence.servant.Servant;

public class Master {
	private UUID masterUUID;
	private Map<UUID, Servant> servants = new HashMap<UUID, Servant>();
	private AbilityConfig preferences;//the default perms this master's servants get
	private double startingWage;//the default wage this master's servants get
	
//	public Master(){
//	}
	
	public Master(UUID playerUUID, AbilityConfig masterPreferences, double wages){
		masterUUID = playerUUID;
		preferences = masterPreferences;
		startingWage = wages;
	}
	
	public Collection<Servant> getServants(){return servants.values();}
	public boolean hasServant(UUID playerUUID){return servants.containsKey(playerUUID);}
	
	public boolean addServant(UUID playerUUID, boolean force){
		if(force == false){
			if(servants.containsKey(playerUUID) || playerUUID.equals(masterUUID)) return false;
			
			// Don't add a player as a servant if that player is this master's master
			if(InfluenceAPI.isServant(masterUUID) && InfluenceAPI.checkIsMasterOrAboveMaster(playerUUID, masterUUID)) return false;
		}
		
		if(preferences != null) servants.put(playerUUID, new Servant(playerUUID, masterUUID, preferences, startingWage));
		else servants.put(playerUUID, new Servant(playerUUID, masterUUID, new AbilityConfig(true), startingWage));
		return true;
	}
	
	public boolean addServant(Servant servant, boolean force){
		if(force == false){
			if(servants.containsValue(servant) || servant.getPlayerUUID().equals(masterUUID)) return false;
			if(InfluenceAPI.isServant(masterUUID) &&
					InfluenceAPI.checkIsMasterOrAboveMaster(servant.getPlayerUUID(), masterUUID)) return false;
		}
		
		servants.put(servant.getPlayerUUID(), servant);
		return true;
	}
	
	public boolean removeServant(UUID playerUUID){
		if(!servants.containsKey(playerUUID) || playerUUID == masterUUID) return false;
		
		else{
			servants.remove(playerUUID);
			//TODO: Think of other things that happen when a servant is freed
		}
		return true;
	}
	
	public Servant getServant(UUID playerUUID){
		if(servants.containsKey(playerUUID)) return servants.get(playerUUID);
		else return null;
	}
	
	public AbilityConfig getPreferences(){
		return preferences;
	}
	
	public double getStartingWage(){return startingWage;}
	
	public UUID getPlayerUUID(){
		return masterUUID;
	}
}
