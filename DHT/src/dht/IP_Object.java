package dht;

import java.io.Serializable;

public class IP_Object implements Serializable {
	private String member;
	private String ip;
	
	public IP_Object(String member, String ip) {
		this.setMember(member);
		this.setIP(ip);
	}

	public String getIP() {
		return ip;
	}

	public void setIP(String ip) {
		this.ip = ip;
	}

	public String getMember() {
		return member;
	}

	public void setMember(String member) {
		this.member = member;
	}
	

}
