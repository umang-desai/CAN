import java.io.Serializable;
import java.net.InetAddress;


public class ConnectionInfo implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String id;
	private InetAddress ip;
	private int port;
	
	public ConnectionInfo(String id, InetAddress ip, int port){
		this.id = id;
		this.ip = ip;
		this.port = port;
	}

	public String getId(){
		return id;
	}
	
	public InetAddress getIp() {
		return ip;
	}

	public int getPort() {
		return port;
	}
	
	public boolean equals(ConnectionInfo con){
		if(con instanceof ConnectionInfo){
			return (id.equals(con.getId()) && ip.equals(con.getIp()) && port == con.getPort());
		}
		return false;
	}
}
