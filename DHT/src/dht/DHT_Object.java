package dht;

import java.io.Serializable;
import java.util.HashMap;

public class DHT_Object implements Serializable {
	private DHTUserInterface DHTTable;
	private String src_member;
	private int position;
	
	public DHT_Object() {}
	
	public DHT_Object(String src_member) {		
		this.setSrc_member(src_member);
	}
	
	public int getPosition() {
		return position;
	}
	
	public void setPosition(int position)
	{
		this.position = position;
	}
	
	public DHTUserInterface getDHTTable() {
		return DHTTable;
	}

	public void setDHTTable(DHTUserInterface dHTTable) {
		DHTTable = dHTTable;
	}

	public String getSrc_member() {
		return src_member;
	}

	public void setSrc_member(String src_member) {
		this.src_member = src_member;
	}
}
