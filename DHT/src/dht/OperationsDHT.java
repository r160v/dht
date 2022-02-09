package dht;

import java.io.Serializable;
//import java.util.Set;
import java.util.HashMap;



public class OperationsDHT implements Serializable {

	private static final long serialVersionUID = 1L;
	private OperationEnum operation;
	private Integer     value         = null;       
	private String      key           = null;
	private DHT_Map     map           = null;
	private boolean     status        = false;
	private boolean     isReplica     = false;
	private int         posReplica;
	private int         posServer;
	private DHTUserInterface dht      = null;
	//private HashMap<Integer, Address> DHTServers;
	private HashMap<Integer, String> DHTServers;
	// private Set<String> 
	// private ArrayList<Integer>
	private String sourceMember;
	private String destinationMember;

	// PUT_MAP
	/**
	 * Create the put operation 
	 * @param operation The operation
	 * @param map The map
	 * @param isReplica Indicate whether this operation is associated to a replica,
	 */
	public OperationsDHT (OperationEnum operation,
			DHT_Map map, 
			boolean isReplica, String destinationMember)           {
		this.operation = operation;
		this.map       = map;
		this.isReplica = isReplica;
		this.destinationMember = destinationMember;
	}

	// GET_MAP REMOVE_MAP CONTAINS_KEY_MAP
	public OperationsDHT (OperationEnum operation,
			String key,           
			boolean isReplica, String destinationMember) {
		this.operation = operation;
		this.key       = key;
		this.isReplica = isReplica;
		this.destinationMember = destinationMember;
	}

	// KEY_SET_HM, VALUES_HM, INIT	
	public OperationsDHT (OperationEnum operation, String destinationMember) {
		this.operation = operation;
		this.destinationMember = destinationMember;
	}

	//RETURN_VALUE
	public OperationsDHT (OperationEnum operation,
			Integer value, String destinationMember)           {
		this.operation = operation;
		this.value     = value;
		this.destinationMember = destinationMember;
	}

	//RETURN_STATUS
	public OperationsDHT (OperationEnum operation,
			boolean status, String destinationMember)           {
		this.operation  = operation;
		this.status     = status;
		this.destinationMember = destinationMember;
	}

	//DATA_REPLICA
	public OperationsDHT ( OperationEnum operation, 
			DHTUserInterface dht, int posReplica, int posServer, String destinationMember) {
		this.operation   = operation;
		this.dht         = dht;
		this.posReplica  = posReplica;
		this.posServer   = posServer;
		this.destinationMember = destinationMember;
	}

	//DHT_REPLICA
	public OperationsDHT ( OperationEnum operation, 
			HashMap<Integer, String> DHTServers, String destinationMember) {
		this.operation   = operation;
		this.DHTServers  = DHTServers;
		this.destinationMember = destinationMember;
	}
	
	public OperationEnum getOperation() {
		return operation;
	}

	public void setOperation(OperationEnum operation) {
		this.operation = operation;
	}

	public Integer getValue() {
		return value;
	}

	public void setValue(Integer value) {
		this.value = value;
	}
	
	public void setSourceMember(String sourceMember) {
		this.sourceMember = sourceMember;
	}
	public String getSourceMember() {
		return this.sourceMember;
	}
	
	public void setDestinationMember(String destinationMember) {
		this.destinationMember = destinationMember;
	}
	public String getdestinationMember() {
		return this.destinationMember;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public DHT_Map getMap() {
		return map;
	}

	public void setMap(DHT_Map map) {
		this.map = map;
	}

	public boolean getStatus() {
		return status;
	}

	public void setMap(boolean status) {
		this.status = status;
	}

	public boolean isReplica() {
		return isReplica;
	}

	public void setReplica(boolean isReplica) {
		this.isReplica = isReplica;
	}

	public void setStatus(boolean status) {
		this.status = status;
	}

	public DHTUserInterface getDHT() {
		return this.dht;
	}
	
	public int getPosReplica() {
		return this.posReplica;
	}
	
	public HashMap<Integer, String> getDHTServers() {
		return this.DHTServers;
	}

	// LIST_SERVERS
	// Es posible que no sea necesario
	
	
}

