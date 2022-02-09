package dht;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.ZooDefs.Ids;
//import org.jgroups.Address;
//import org.jgroups.JChannel;
//import org.jgroups.Message;

import java.util.logging.ConsoleHandler;
import java.util.logging.Filter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;


public class SendMessagesDHT implements SendMessages{

	private java.util.logging.Logger LOGGER = MainUI.LOGGER;
	//private JChannel channel;
	private ZooKeeper zk;
	private static String rootOperations = "/operations";
	private static String aOperation = "/operation-";
	private static String rootMembers = "/members";
	private String localMember;

	public SendMessagesDHT(ZooKeeper zk, String localMember)  {
		this.zk = zk;
		this.localMember = localMember;
	}
	
	
	private void sendMessage(String destinationMember, OperationsDHT operation) {

		try {
			if (destinationMember == null) {
				LOGGER.warning("Member ID is null");
				operation.setDestinationMember("cluster");
			}
			if (operation == null) LOGGER.warning("Operation is null");			
			
			operation.setSourceMember(localMember);
			
			LOGGER.fine("Sending " + operation.getOperation() + " to " + destinationMember);
						
		    ByteArrayOutputStream bos = new ByteArrayOutputStream();
		    ObjectOutputStream oos = new ObjectOutputStream(bos);
		    oos.writeObject(operation);
		    oos.flush();
		    byte [] bufferByte = bos.toByteArray();
		    bos.close();
		    oos.close();		    
			
			if (bufferByte == null) LOGGER.warning("Buffer is null");
			if (this.zk == null ) LOGGER.warning("Zookeeper object is null");
			
			String opId = zk.create(rootOperations + aOperation, bufferByte, 
							Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
			
			LOGGER.fine("A " + operation.getOperation() + " has been created in "  
							+ opId + " by " + localMember);
			
			
				
			
		} catch (Exception e) {
			System.err.println(e);
			System.out.println("Error when sending message");
			e.printStackTrace();
		}
	}

	//@Override
	public void sendPut(String destinationMember, DHT_Map map, boolean isReplica) {
		OperationsDHT operation = new OperationsDHT(OperationEnum.PUT_MAP, map, isReplica, destinationMember);
		sendMessage(destinationMember, operation);
		//TODO
	}

	//@Override
	public void sendGet(String destinationMember, String key, boolean isReplica) {
		OperationsDHT operation = new OperationsDHT(OperationEnum.GET_MAP, key, isReplica, destinationMember);
		sendMessage(destinationMember, operation);
	}

	public void sendRemove(String destinationMember, String key, boolean isReplica) {
		OperationsDHT operation = new OperationsDHT(OperationEnum.REMOVE_MAP, key, isReplica, destinationMember);
		sendMessage(destinationMember, operation);		
	}

	public void sendContainsKey(String destinationMember, String key, boolean isReplica) {
		OperationsDHT operation = new OperationsDHT(OperationEnum.CONTAINS_KEY_MAP, key, isReplica, destinationMember);
		sendMessage(destinationMember, operation);

	}

	public void sendKeySet (String destinationMember) {
		OperationsDHT operation = new OperationsDHT(OperationEnum.KEY_SET_HM, destinationMember);
		sendMessage(destinationMember, operation);
	}


	public void sendList (String destinationMember, ArrayList<String> list) {
		OperationsDHT operation = new OperationsDHT(OperationEnum.LIST_SERVERS, destinationMember);
		sendMessage(destinationMember, operation);
	}
	
	public void returnValue(String destinationMember, Integer value) {
		OperationsDHT operation = new OperationsDHT(OperationEnum.RETURN_VALUE,
											value, destinationMember);
		sendMessage(destinationMember, operation);
	}

	public void returnStatus(String destinationMember, boolean status) {
		OperationsDHT operation = new OperationsDHT(OperationEnum.RETURN_VALUE,
											status, destinationMember);
		sendMessage(destinationMember, operation);
	}
	
	
	public void sendServers(String destinationMember, HashMap<Integer, String> DHTServers) {
		LOGGER.fine("sendServers. Member ID: " + destinationMember);
		System.out.println("sendServers. Member ID: " + destinationMember);
		OperationsDHT operation = new OperationsDHT(OperationEnum.DHT_REPLICA, DHTServers, destinationMember);
		sendMessage(destinationMember, operation);		
	}	
	
	public void sendInit(String destinationMember) {
		LOGGER.fine("Send DHT Replica. Member ID: " + destinationMember);
		OperationsDHT operation = new OperationsDHT(OperationEnum.INIT, destinationMember);
		sendMessage(destinationMember, operation);		
	}
}



