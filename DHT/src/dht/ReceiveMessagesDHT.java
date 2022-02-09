package dht;

import org.apache.zookeeper.ZooKeeper;


public class ReceiveMessagesDHT {

	private java.util.logging.Logger LOGGER = MainUI.LOGGER;

	private SendMessages sendMessages;
	private DHTUserInterface  dht;
	private OperationBlocking mutex;
	//private ViewManager       viewManager;
	//private TableManager      tableManager;
	private zkMember zkM;

	
	public ReceiveMessagesDHT (zkMember zkM, 
			DHTUserInterface dht,
			OperationBlocking mutex,
			SendMessagesDHT sendMessages) {
	
		this.zkM = zkM;
		this.sendMessages = sendMessages;
		this.dht          = dht;
		this.mutex        = mutex;

	}

	
	public void handleReceiverMsg(OperationsDHT operation) {
		String sourceMember = operation.getSourceMember();
		LOGGER.fine("Operation in a message: " + operation.getOperation());
		Integer value;
		boolean status;
		switch (operation.getOperation()) {
		case PUT_MAP:
			LOGGER.fine(operation.getOperation() + " Key: " + operation.getMap().getKey());
			value = dht.putMsg(operation.getMap());
			LOGGER.fine("-----putLocal completed-----");
			if (!operation.isReplica()) {
				sendMessages.returnValue(sourceMember, value);
				LOGGER.fine("-----returnValue handled-----");
			}
			
			break;
		case GET_MAP:
			value = dht.get(operation.getKey());
			LOGGER.fine(operation.getOperation() + " Value: " + value);
			if (!operation.isReplica()) {
				sendMessages.returnValue(sourceMember, value);
			}
			break;
		case REMOVE_MAP:
			value = dht.removeMsg(operation.getKey());
			LOGGER.fine(operation.getOperation() + " Value: " + value);
			if (!operation.isReplica()) {
				// Due to previous comment CHECK IT
				sendMessages.returnValue(sourceMember, value);
			}
			break;
		case CONTAINS_KEY_MAP:
			status = dht.containsKey(operation.getKey());
			if (!operation.isReplica()) {
				sendMessages.returnStatus(sourceMember, status);
			}
			break;
		case KEY_SET_HM:			
			break;
		case RETURN_VALUE :
			mutex.receiveOperation(operation);
			break;

		case RETURN_STATUS :
			mutex.receiveOperation(operation);
			break;		
			
		case DHT_REPLICA :
			LOGGER.fine("Received servers (DHTServers):" + operation.getPosReplica());
			zkM.putDHTServers(operation.getDHTServers());
			
			break;	
		
		default:
			break;
		}
	}

}
