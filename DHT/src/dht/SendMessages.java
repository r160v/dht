package dht;

import java.util.ArrayList;
import java.util.HashMap;



public interface SendMessages {

	/**
	 * Inform the request for adding a map.
	 * @param address The address of the new server
	 * @param map The DHT servers
	 * @param isReplica Inform whether this is a replica
	 */
	public void sendPut(String member_id, DHT_Map map, boolean isReplica);

	/**
	 * Inform the request for getting the value of a key.
	 * @param address The address of the new server
	 * @param key The key
	 * @param isReplica Inform whether this is a replica
	 */
	public void sendGet(String member_id, String key, boolean isReplica);

	/**
	 * Inform the request for remove the map of a key.
	 * @param address The address of the new server
	 * @param key The key
	 * @param isReplica Inform whether this is a replica
	 */
	public void sendRemove(String member_id, String key, boolean isReplica);

	/**
	 * Inform the request for checking whether a key is contained
	 * @param address The address of the new server
	 * @param key The key
	 * @param isReplica Inform whether this is a replica
	 */
	public void sendContainsKey(String member_id, String key, boolean isReplica);

	/**
	 * Inform the request for getting the set of the keys in the DHT
	 * @param address The address of the new server
	**/
	public void sendKeySet (String member_id);

	// It is not used
	//public void sendValues (Address address);
	
	public void sendList (String member_id, ArrayList<String> list);

	/**
	 * Return a value requested in a previous message
	 * @param address The address of the new server
	 * @param value The value in the request
	**/	
	public void returnValue(String member_id, Integer value);

	/**
	 * Return the result of check in a previous message
	 * @param address The address of the new server
	 * @param status The result of a check
	**/	
	public void returnStatus(String member_id, boolean status);

	/**
	 * Send the DHT servers to a new server.
	 * @param address The address of the new server
	 * @param DHTServers The DHT servers
	 */
	public void sendServers(String member_id, HashMap<Integer, String> DHTServers);
	
	/**
	 * Inform of the init of a new server while an initial quorum is creating
	 * @param address The address of the new server
	 */
	public void sendInit(String member_id);
}

