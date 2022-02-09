package dht;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.net.Socket;
import org.apache.log4j.Logger;

public class ReceiveDHTTable  {
	Socket ClientSoc;

	ObjectInputStream din;
	DataOutputStream dout;
	DHT_Object dhtobj;
	private java.util.logging.Logger LOGGER = MainUI.LOGGER;

	ReceiveDHTTable(Socket soc) {
		try {
			ClientSoc = soc;
			din = new ObjectInputStream(ClientSoc.getInputStream());
			dout = new DataOutputStream(ClientSoc.getOutputStream());
			System.out.println("Client Connected ...");
			ReceiveObject();
			//start();

		} catch (Exception ex) {
		}
	}
	
	public DHT_Object getDHTObject() {
		return this.dhtobj;
	}

	void SendFile() throws Exception {
		String filename = din.readUTF();
		File f = new File(filename);
		if (!f.exists()) {
			dout.writeUTF("File Not Found");
			return;
		} else {
			dout.writeUTF("READY");
			FileInputStream fin = new FileInputStream(f);
			int ch;
			do {
				ch = fin.read();
				dout.writeUTF(String.valueOf(ch));
			} while (ch != -1);
			fin.close();
			dout.writeUTF("File Receive Successfully");
		}
	}

	void ReceiveObject() throws Exception {		
		Object genobj = (Object) din.readObject();
		this.dhtobj= (DHT_Object) genobj;
		LOGGER.fine("Received object from member: " + dhtobj.getSrc_member());
	}

	
}


