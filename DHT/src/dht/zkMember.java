package dht;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.util.Random;
import java.util.Set;
import java.util.logging.ConsoleHandler;

// Import log4j classes.
import org.apache.log4j.Logger;
import org.apache.log4j.Level;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.PropertyConfigurator;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;




/**
 * This is a simple application for detecting the correct processes using ZK.
 * Several instances of this code can be created. Each of them detects the valid
 * numbers.
 *
 * Two watchers are used: - cwatcher: wait until the session is created. -
 * watcherMember: notified when the number of members is updated
 * 
 * the method process has to be created for implement Watcher. However this
 * process should never be invoked, as the "this" watcher is used
 */
public class zkMember implements Watcher {
	private static final int SESSION_TIMEOUT = 5000;

	private static String rootMembers = "/members";
	private static String aMember = "/member-";
	private static String rootOperations = "/operations";
	private static String rootIPAddresses = "/ip_addresses";
	private static String anIP = "/ip_address-";
	private String myId;
	private String opsId;
	private String ipID;
	private int processedReplicas;
	private boolean ipznode;
	private String localIP;
	private List<DHT_Object> dhtobjlist;
	private MainUI ui;

	Integer mutexBarrier = -1;

	
	String[] hosts = {};

	private ZooKeeper zk;

	
	private java.util.logging.Logger LOGGER = MainUI.LOGGER;
	
	private int nServersMax;
	private int nServers;
	private int nReplica;
	private boolean isQuorum = false;
	private boolean pendingReplica = false;
	private boolean firstQuorum = false;
	private TableManager tableManager; //
	private List<String> previousMembers = null;
	private List<String> currentMembers = null;
	private String localMember;
	private String failedServerTODO;
	private SendMessagesDHT sendMessages;
	private ReceiveMessagesDHT receiveMessages;
	private OperationBlocking mutex;
	private DHTUserInterface dht;
	private String zkEnsemble;
	
	
	public zkMember(MainUI ui, String zkEnsemble) {
		this.nServersMax = 3;
		this.nReplica = 2;
		this.nServers = 0;
		this.ipID = null;
		this.processedReplicas = 0;
		this.ipznode = false;
		this.dhtobjlist = new ArrayList<DHT_Object>();
		this.ui = ui;
		this.zkEnsemble = zkEnsemble;
		
		
		hosts = zkEnsemble.trim().split(";");

		
		Random rand = new Random();
		int i = rand.nextInt(hosts.length);

		// Create a session and wait until it is created.
		// When is created, the watcher is notified
		try {
			if (zk == null) {
				zk = new ZooKeeper(hosts[i], SESSION_TIMEOUT, cWatcher);
				try {
					// Wait for creating the session. Use the object lock
					synchronized (mutexBarrier) {
						mutexBarrier.wait();
					}
					// zk.exists("/",false);
				} catch (Exception e) {
					// System.out.println("Exception in the wait while creating the session");
					LOGGER.severe("Exception in the wait while creating the session");
				}
			}
		} catch (Exception e) {
			// System.out.println("Exception while creating the session");
			LOGGER.severe("Exception while creating the session");
	
		}

		
		if (zk != null) {
			
			try {

				this.mutex = new OperationBlocking();

				String response = new String();
				Stat s = zk.exists(rootMembers, false);
				if (s == null) {
					
					response = zk.create(rootMembers, new byte[0], Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
					
					LOGGER.fine(response);

				}
				zk.getChildren(rootMembers, watcherMember, s);

				s = zk.exists(rootOperations, false);
				if (s == null) {
					
					response = zk.create(rootOperations, new byte[0], Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
					

					LOGGER.fine(response);
				}
				zk.getChildren(rootOperations, watcherOperations, s);

				s = zk.exists(rootIPAddresses, false);
				if (s == null) {
					
					response = zk.create(rootIPAddresses, new byte[0], Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
					
					LOGGER.fine(response);
				}
				zk.getChildren(rootIPAddresses, watcherIPs, s);
				
				
				zk.create(rootMembers + aMember, new byte[0], Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);

			} catch (KeeperException e) {
				
				LOGGER.severe("The session with Zookeeper fails. Closing");
				return;
			} catch (InterruptedException e) {
				
				LOGGER.severe("InterruptedException raised");
			}

		}
	}
	

	public Integer put(DHT_Map map) {
		return dht.put(map);
	}

	public Integer get(String key) {
		return dht.get(key);
	}
	
	public HashMap<Integer, DHTUserInterface> getDHTTables(){
		return tableManager.getDHTTables();
	}

	public Integer remove(String key) {
		return dht.remove(key);
	}

	public boolean containsKey(String key) {
		return dht.containsKey(key);
	}

	public String toString() {
		return dht.toString();
	}

	public ZooKeeper getZookeeper() {
		return this.zk;
	}

	

	public String getLocalMemberId() {
		return this.localMember;
	}
	
	public int getPos()
	{
		return tableManager.getPos();
	}
	
	/**
	 * Get the address of the server/process that has failed
	 * 
	 * @param previousView the previous view
	 * @param newView      the current view
	 * @return the addres of the failed process
	 */
	public String crashedServer(List<String> prevMemb, List<String> currMemb) {

		for (int k = 0; k < currMemb.size(); k++) {
			if (prevMemb.get(k).equals(currMemb.get(k))) {
			} else {
				return prevMemb.get(k);
			}

		}

		return prevMemb.get(prevMemb.size() - 1);
	}

	public Integer deleteServer(String failed_server) {
		HashMap<Integer, String> DHTServers = tableManager.getDHTServers();

		for (int i = 0; i < nServersMax; i++) {
			if (failed_server.equals(DHTServers.get(i))) {
				DHTServers.remove(i);
				return i;
			}
		}
		LOGGER.warning("This sentence should no be run");
		return null;
	}

	public HashMap<Integer, String> addServer(String member_id) {

		HashMap<Integer, String> DHTServers = tableManager.getDHTServers();

		HashMap<Integer, DHTUserInterface> DHTTables = tableManager.getDHTTables();

		// The quorum of servers is sufficient
		if (nServers >= nServersMax) {
			LOGGER.fine("The quorum is already created. This server is not required" + nServers);
			return null;
		} else {
			// Find a hole. Selects the first free.
			// Integrate it in DHT
			for (int i = 0; i < nServersMax; i++) {
				if (DHTServers.get(i) == null) {
					DHTServers.put(i, member_id);
					if (DHTTables.get(i) == null) {
						DHTTables.put(i, new DHTHashMap());
					}
					nServers++;
					// sendMessages.sendServers(address, DHTServers);
					LOGGER.finest("Added a server. NServers: " + nServers);
					return DHTServers;
				}
			}
		}
		LOGGER.warning("Error: This sentence shound not run");
		return null;
	}

	public String newServer(List<String> members) {
		return members.get(members.size() - 1);
	}

	public boolean isQuorum() {
		return isQuorum;
	}

	/**
	 * Copy the DHT servers in the parameter in the local DHT Servers
	 * 
	 * @param newDHTServers The DHT servers
	 */
	public void putDHTServers(HashMap<Integer, String> newDHTServers) {
		HashMap<Integer, String> DHTServers = tableManager.getDHTServers();
		for (int i = 0; i < nServersMax; i++) {
			DHTServers.put(i, newDHTServers.get(i));
		}
		
		int pos = tableManager.getPos();
		LOGGER.fine(tableManager.printDHTServers());
		handleTables();
		ui.setLabel(tableManager.getPos(), localMember, currentMembers.size());
	}

	public void transferData(String sourceMember, String ip) {
		HashMap<Integer, String> DHTServers = tableManager.getDHTServers();
		HashMap<Integer, DHTUserInterface> DHTTables = tableManager.getDHTTables();
		
		DHT_Object dhtobj = new DHT_Object(localMember);
		
		Socket soc = null;
		SendDHTTable sendDHTTable = null;
		while(true) {
			try {
				soc=new Socket(ip, 5217); 
				LOGGER.fine("Connected to server");
				break;
				
			} catch (IOException e) {
				LOGGER.fine("Trying to connect to server...");
			}
		}
		
		sendDHTTable = new SendDHTTable(soc);		

		if (pendingReplica) {
			pendingReplica = false;
		} else {
			try {
				sendDHTTable.SendObject(dhtobj);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return;
		}	

		if (!sourceMember.equals(failedServerTODO)) {
			LOGGER.severe("!!!!!!! member_id != failedServerTODO");
		}

		
		// Send the DHT servers to the new address
		sendMessages.sendServers(sourceMember, DHTServers);		

		int i = 0;
		int posNew = 0;
		for (i = 0; i < nServersMax; i++) {
			// if (address.equals(DHTServers.get(i))) {
			// Address repAddres = DHTServers.get(i);
			if (sourceMember.equals(DHTServers.get(i))) {
				posNew = i;
				break;
			}
		}

		int posLocal = 0;
		for (i = 0; i < nServersMax; i++) {
			if (localMember.equals(DHTServers.get(i))) {
				posLocal = i;
				break;
			}
		}

		LOGGER.fine("Check whether sending table (-1) from " + posLocal + " to " + posNew);

		int posNext = (posNew + 1) % nServersMax;
		if (posLocal == posNext) {
			LOGGER.fine("pos: " + posNew + " local: " + posLocal + " member_id: " + sourceMember);
			
			//DHTUserInterface hashMaptest = DHTTables.get(posNew);
			dhtobj.setDHTTable(DHTTables.get(posNew));
			dhtobj.setPosition(posNew);
			try {
				sendDHTTable.SendObject(dhtobj);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		LOGGER.fine("Check whether sending table (0) from " + posLocal + " to " + posNew);
		// send the second replica of the previous
		for (int j = 1; j < nReplica; j++) {
			int posPrev = (posNew - j) % nServersMax;
			if (posPrev < 0) {
				posPrev = posPrev + nServersMax;
			}
			if (posLocal == posPrev) {
				LOGGER.fine("replica: " + posNext + "member_id: " + sourceMember);
				LOGGER.fine("replica: " + posNext + " member_id: " + sourceMember);
				
				//DHTUserInterface hashMaptest = DHTTables.get(posLocal); 
				dhtobj.setDHTTable(DHTTables.get(posLocal));
				dhtobj.setPosition(posLocal);
				try {
					sendDHTTable.SendObject(dhtobj);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

	}
	
		
	
	private void handleTables() {
		HashMap<Integer, String> DHTServers = tableManager.getDHTServers();
		HashMap<Integer, DHTUserInterface> DHTTables = tableManager.getDHTTables();
		
		DHTUserInterface hashMap = null;
		for(int k=0; k < dhtobjlist.size();k++) {
			if(dhtobjlist.get(k).getDHTTable() != null) {			
				DHTTables.put(dhtobjlist.get(k).getPosition(), dhtobjlist.get(k).getDHTTable());				
			}
		}
		ui.updateTable();
	}

	private void startServer(String ip, int nMembers) {	
		ServerSocket soc = null;
		try {
			soc = new ServerSocket();
			SocketAddress endPoint = new InetSocketAddress(InetAddress.getByName(ip), 5217);			
			soc.bind(endPoint);

			LOGGER.fine("Server Started on Port Number 5217");
			
			LOGGER.fine("Waiting for Connection ...");
			ReceiveDHTTable t = null;
			
			int i = Math.min((nMembers-1), nReplica);
			
			for(int j = 0; j <i; j++) {
				t = new ReceiveDHTTable(soc.accept());
				dhtobjlist.add(t.getDHTObject());				
			}
			
			zk.delete(ipID, -1);				
		} 
		catch (Exception ex) {
			LOGGER.severe("Unable to start server");
			
		} finally {
			
			try {
				soc.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private void createIPZnode() {
		try (final DatagramSocket socket = new DatagramSocket()) {
			ipznode= true;			
			
			Enumeration e = NetworkInterface.getNetworkInterfaces();
			while(e.hasMoreElements())
			{
			    NetworkInterface n = (NetworkInterface) e.nextElement();
			    Enumeration ee = n.getInetAddresses();
			    while (ee.hasMoreElements())
			    {
			        InetAddress i = (InetAddress) ee.nextElement();
			        if(i.isSiteLocalAddress()) {			        	
			        	localIP = i.getHostAddress();
			        	break;
			        }     			       
			    }
			}
			
			LOGGER.fine("Local IP is: " + localIP);			
			IP_Object ipo = new IP_Object(localMember, localIP);			

			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream(bos);
			oos.writeObject(ipo);
			oos.flush();
			byte[] bufferByte = bos.toByteArray();
			bos.close();
			oos.close();
			

			if (bufferByte == null)
				LOGGER.warning("Buffer is null");
			if (this.zk == null)
				LOGGER.warning("Zookeeper object is null");

			ipID = zk.create(rootIPAddresses + anIP, bufferByte, Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
			

		} catch (Exception ex) {
			LOGGER.severe("Receive tables error");
			
		}
	}

	public boolean manageMembers() {

		String member_id = null;

		
		try {
			currentMembers = zk.getChildren(rootMembers, false);

			Collections.sort(currentMembers);
			
			// There are enough servers: nServers = nServersMax
			if (currentMembers.size() > nServersMax) {
				if (currentMembers.get(currentMembers.size() - 1) == localMember) {
					LOGGER.warning("The server is exited. The quorum is already created!!!!!");
					System.exit(0);
				} else {
					return false; // TODO what does false mean??
				}
			}


			// A server has failed
			if (previousMembers != null && currentMembers.size() < previousMembers.size()) {
				LOGGER.warning("A server has failed. There is no quorum!!!!!");
				// A server has failed
				String failedServer = crashedServer(previousMembers, currentMembers);
				deleteServer(failedServer);
				nServers--;
				isQuorum = false;
				pendingReplica = true;
				previousMembers = currentMembers;				
				return false;
			}

			if (currentMembers.size() > nServers) {

				// Initial quorum. Add new servers.
				if (nServers == 0 && currentMembers.size() > 0) {
					for (Iterator<String> iterator = currentMembers.iterator(); iterator.hasNext();) {

						String itMember = (String) iterator.next();
						addServer(itMember);
						LOGGER.fine("Added a server. NServers: " + nServers + "Server: " + itMember + ".");
						if (!itMember.equals(localMember)) {							
							if(!ipznode) {
								createIPZnode();
								startServer(localIP, currentMembers.size());
							}							
						}
						if (nServers == nServersMax) {
							isQuorum = true;
							firstQuorum = true;
						}
					}
					

				} else {
					if (currentMembers.size() > nServers) {
						HashMap<Integer, String> DHTServers;
						member_id = currentMembers.get(currentMembers.size() - 1);
						addServer(member_id);
						LOGGER.fine("Added a server. NServers: " + nServers + ". Server: " + member_id);
						if (nServers == nServersMax) {
							isQuorum = true;
							// A server crashed and is a new one
							if (firstQuorum) {
								// A previous quorum existed. Then tolerate the fail
								// Add the new one in the DHTServer
								String failedServer = newServer(currentMembers);
								failedServerTODO = failedServer;
								// Add the server in DHTServer
								DHTServers = addServer(failedServer);
								if (DHTServers == null) {
									LOGGER.warning("DHTServers is null!!");
								} else {
									sendMessages.sendServers(failedServer, DHTServers);
								}
								// Send the Replicas
								// transferData(failedServer);
								pendingReplica = true;
							} else {
								firstQuorum = true;
							}
						}
					}
				}
			}
			LOGGER.fine(tableManager.printDHTServers());
			previousMembers = currentMembers;
			return true;
		} catch (KeeperException e) {
			e.printStackTrace();
			return false;
		} catch (InterruptedException e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * This variable creates a new watcher. It is fired when IP_Addresses' children
	 * change
	 */
	private Watcher watcherIPs = new Watcher() {
		public void process(WatchedEvent e) {
			LOGGER.fine("------------------Watcher New IP------------------\n");
			try {
				List<String> list = zk.getChildren(rootIPAddresses, watcherIPs);
				LOGGER.fine("-----Total number of IPs: " + list + "-----");
				Stat s = null;

				for (int i = 0; i < list.size(); i++) {
					LOGGER.fine("A message has been received");
					java.lang.Object ipObj = null;
					try {
						if (zk.exists(rootIPAddresses + "/" + list.get(i), false) != null) {
							byte[] op = zk.getData(rootIPAddresses + "/" + list.get(i), false, s);
							ByteArrayInputStream bis = new ByteArrayInputStream(op);
							ObjectInputStream in = new ObjectInputStream(bis);
							ipObj = (java.lang.Object) in.readObject();

							IP_Object ipo = (IP_Object) ipObj;

							LOGGER.fine("-----IP znode " + list.get(i) + " exists-----");

							if(ipo.getIP() != localMember)
								transferData(ipo.getMember(), ipo.getIP());

						}
					} catch (Exception ex) {
						LOGGER.warning("IP watcher exception. Message: "
								+ ex.toString());
					}
				}

			} catch (Exception ex) {
				LOGGER.severe("Exception: watcherIP");
			}
		}
	};

	/**
	 * This variable creates a new watcher. It is fired when the session is created
	 */
	private Watcher cWatcher = new Watcher() {
		public void process(WatchedEvent e) {
			// System.out.println("Created session");
			// System.out.println(e.toString());
			LOGGER.fine("Created session");
			LOGGER.fine(e.toString());
			synchronized (mutexBarrier) {
				mutexBarrier.notify();
			}
		}
	};

	/**
	 * This variable creates a new watcher. It is fired when a child of "member" is
	 * created or deleted.
	 */
	private Watcher watcherMember = new Watcher() {
		/**
		 * This method is executed whenever the watcher is fired
		 */
		public void process(WatchedEvent event) {
			
			LOGGER.fine("------------------Watcher Member------------------\n");
			try {
				LOGGER.fine("Members node has changed");
				
				currentMembers = zk.getChildren(rootMembers, watcherMember);
				Collections.sort(currentMembers);
				String lastMember = currentMembers.get(currentMembers.size() - 1).replace(rootMembers + "/", "");

				if (previousMembers == null || currentMembers.size() > previousMembers.size())
					LOGGER.fine("Zookeeper--Created member with ID: " + lastMember);
				else
					LOGGER.fine("Zookeeper--A member has failed, last member ID: " + lastMember);

				if (localMember == null) {
					LOGGER.fine("-----Local member ID: " + lastMember + "-----");
					localMember = lastMember;
					tableManager = new TableManager(localMember, nServersMax, nReplica);
					sendMessages = new SendMessagesDHT(zk, localMember);
					dht = new OperationManager(sendMessages, mutex, tableManager, ui);
					receiveMessages = new ReceiveMessagesDHT(zkMember.this, dht, mutex, sendMessages);					
				}

				manageMembers();
				ui.setLabel(tableManager.getPos(), localMember, currentMembers.size());
				ui.updateTable();

			} catch (Exception e) {

				LOGGER.severe("Exception: watcherMember" + e.getMessage());
			}
		}
	};

	private Watcher watcherOperations = new Watcher() {
		/**
		 * This method is executed whenever the Operations watcher is fired
		 */
		public void process(WatchedEvent event) {
			LOGGER.fine("------------------Watcher New Operation------------------\n");
			try {
				List<String> list = zk.getChildren(rootOperations, watcherOperations);
				LOGGER.fine("-----Total number of operations: " + list + "-----");
				Stat s = null;

				for (int i = 0; i < list.size(); i++) {
					LOGGER.fine("A message has been received");
					java.lang.Object dhtbdObj = null;
					try {
						if (zk.exists(rootOperations + "/" + list.get(i), false) != null) {
							byte[] op = zk.getData(rootOperations + "/" + list.get(i), false, s);
							ByteArrayInputStream bis = new ByteArrayInputStream(op);
							ObjectInputStream in = new ObjectInputStream(bis);
							dhtbdObj = (java.lang.Object) in.readObject();

							OperationsDHT operation = (OperationsDHT) dhtbdObj;

							LOGGER.fine("-----Operation " + list.get(i) + " exists-----");

							if (operation.getdestinationMember().equals(localMember)
									|| operation.getdestinationMember().equals("cluster")) {
								LOGGER.fine("-----Operation " + list.get(i) + " is addressed to local-----");
								receiveMessages.handleReceiverMsg(operation);

								zk.delete(rootOperations + "/" + list.get(i), -1);
								LOGGER.fine("The operation " + operation.getOperation()
										+ " has been processed and operation node " + list.get(i)
										+ " has been deleted");

							} else
								LOGGER.fine("-----Operation " + list.get(i) + " is NOT addressed to local-----");

						}
					} catch (Exception e) {
						LOGGER.warning("Controlled exception (operation processed by another member). Message: "
								+ e.toString());
					}
				}

			} catch (Exception e) {
				LOGGER.severe("Exception: watcherMember");
			}

		}
	};

	/**
	 * This method is executed whenever the watcher is fired. This process must be
	 * created
	 */
	@Override
	public void process(WatchedEvent event) {
		try {
			System.out.println("Unexpected invocated this method. Process of the object");
			List<String> list = zk.getChildren(rootMembers, watcherMember);
			printListMembers(list);
		} catch (Exception e) {
			System.out.println("Unexpected exception. Process of the object");
		}
	}

	/**
	 * Print a list
	 * 
	 * @param list The list to be printed
	 */
	private void printListMembers(List<String> list) {
		System.out.println("Remaining # members:" + list.size());
		for (Iterator<String> iterator = list.iterator(); iterator.hasNext();) {
			String string = (String) iterator.next();
			System.out.print(string + ", ");
		}
		System.out.println();

	}

	public static void main(String[] args) {
		

		try {
			Thread.sleep(300000);
		} catch (Exception e) {
			System.out.println("Exception in main");
		}
	}
}
