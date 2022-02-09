package dht;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
//import java.util.Collection;
//import java.util.Iterator;
import java.util.Set;



import java.util.logging.ConsoleHandler;
import java.util.logging.Filter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

public class OperationManager implements DHTUserInterface {

	private java.util.logging.Logger LOGGER = MainUI.LOGGER;

	private SendMessagesDHT   sendMessages;     
	private OperationBlocking mutex;
	private TableManager      tableManager;
	private MainUI ui;
	

	public OperationManager (
			SendMessagesDHT sendMessages, 
			OperationBlocking mutex,
			TableManager tableManager,
			MainUI ui) {

		this.sendMessages = sendMessages;
		this.mutex        = mutex;
		this.tableManager = tableManager;
		this.ui = ui;
		

	}

	public Integer putMsg(DHT_Map map) {
		return putLocal(map);
	}

	@Override
	public Integer put(DHT_Map map) {
	
		OperationsDHT operation; 
		LOGGER.finest("PUT: Is invoked");
		int value;
	
	
		// Create the array of nodes where map should be stored
		int nodes[] = tableManager.getNodes(map.getKey());
		
		for (int i = 1; i < nodes.length; i++) {
			if (tableManager.isDHTLocalReplica(nodes[i], map.getKey())) {
				LOGGER.fine("PUT: Local replica");
				value = putLocal(map);				
			} else {
				LOGGER.fine("PUT: Remote replica");
				sendMessages.sendPut(tableManager.DHT_ID(nodes[i]), map, true); 
				ui.updateTable();
			}
		}
		
		if (tableManager.isDHTLocal(nodes[0])) {
			LOGGER.finest("PUT: The operation is local");
			value = putLocal(map);			
		} else {
			sendMessages.sendPut(tableManager.DHT_ID(nodes[0]), map, false);	
			LOGGER.fine("SendPut and start waiting for response");
			operation = mutex.sendOperation();
			LOGGER.finest("Returned value in put: " + operation.getValue());	
			ui.updateTable();
			return operation.getValue();
		}				
		return 0;
	}
	

	private Integer putLocal(DHT_Map map) {
		DHTUserInterface  hashMap;
		hashMap = tableManager.getDHT(map.getKey());
	
		if (hashMap == null) {
			LOGGER.warning("Error: this sentence should not get here");
		}		
		
		int ret = hashMap.put(map);		
		ui.updateTable();
		return ret;
	}


	@Override
	public Integer get(String key) {

		java.util.List<String> DHTReplicas = new java.util.ArrayList<String>();
		OperationsDHT operation; 

		for (Iterator<String> iterator = DHTReplicas.iterator(); iterator.hasNext();) {
			String member_id = (String) iterator.next();
			LOGGER.finest("PUT: The operation is replicated");
			if (tableManager.isDHTLocalReplica(key, member_id)) {
				LOGGER.fine("PUT: Local replica");
				return getLocal(key);
			}
		}

		// Notify the operation to the cluster
		if (tableManager.isDHTLocal(key)) {
			LOGGER.finest("GET: The operation is local");
			return getLocal(key);
		} else {
			sendMessages.sendGet(tableManager.DHT_ID(key), key, false);			
			operation = mutex.sendOperation();
			LOGGER.fine("Returned value in get: " + operation.getValue());
			return operation.getValue();
		}
	}

	private Integer getLocal(String key) {
		DHTUserInterface  hashMap;
		hashMap = tableManager.getDHT(key);
		
		if (hashMap == null) {
			LOGGER.warning("Error: this sentence should not get here");
		}
		
		return hashMap.get(key);		
	}
	
	public Integer removeMsg(String key) {
		return removeLocal(key);
	}
	
	@Override
	public Integer remove(String key) {

		OperationsDHT operation; 
		LOGGER.finest("REMOVE: Is invoked");
		int value;
	
	
		// Create the array of nodes where map should be stored
		int nodes[] = tableManager.getNodes(key);
		
		for (int i = 1; i < nodes.length; i++) {
			if (tableManager.isDHTLocalReplica(nodes[i], key)) {
				LOGGER.fine("PUT: Local replica");
				value = removeLocal(key);				
			} else {
				LOGGER.fine("REMOVE: Remote replica");
				sendMessages.sendRemove(tableManager.DHT_ID(nodes[i]), key, true);					
			}
		}
		
		if (tableManager.isDHTLocal(nodes[0])) {
			LOGGER.finest("PUT: The operation is local");
			return removeLocal(key);			
		} else {
			sendMessages.sendRemove(tableManager.DHT_ID(nodes[0]), key, false);
			operation = mutex.sendOperation();
			LOGGER.finest("Returned value in put: " + operation.getValue());			
			return operation.getValue();
		}		
	}

	private Integer removeLocal(String key) {
		DHTUserInterface  hashMap;
		hashMap = tableManager.getDHT(key);
		
		if (hashMap == null) {
			LOGGER.warning("Error: this sentence should not get here");
		}
		
		int ret = hashMap.remove(key);	
		ui.updateTable();
		return ret;
	}
	
	@Override
	public boolean containsKey(String key) {
		Integer isContained = get(key);
		if (isContained == null) {
			return false;
		} else {
			return true;
		}
	}
	
	@Override
	public Set<String> keySet() {
		// Notify the operation to the cluster

		// Update the operation
		return null; //hashMap.keySet();
	}

	@Override
	public ArrayList<Integer> values() {
		// Notify the operation to the cluster

		// Update the operation
		return null;//hashMap.values();

	}

	@Override
	public String toString() {
		
		return tableManager.toString();

	}


}
