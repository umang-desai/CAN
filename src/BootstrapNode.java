import java.net.*;
import java.rmi.AlreadyBoundException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

//Contains bootstrap server capabilities
public class BootstrapNode extends UnicastRemoteObject implements BootInterface {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private ConnectionInfo con;
	private List<ConnectionInfo> peerList;
	
	public BootstrapNode(String id, int port) throws RemoteException{
		con = new ConnectionInfo(id,getLocalIp(),port);
		peerList = new ArrayList<ConnectionInfo>();
	}
	
	public ConnectionInfo getConInfo(){
		return con;
	}
	
	private InetAddress getLocalIp() {
		try {
			return InetAddress.getLocalHost();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	private ConnectionInfo fetchPeerConInfo(String peername){
		for(int i = 0; i < peerList.size(); i++){
			ConnectionInfo temp = peerList.get(i);
			if(temp.getId().equalsIgnoreCase(peername))
				return temp;
		}
		return null;
	}
	
	public static CANInterface fetchPeer(String id, int port)
			throws RemoteException, NotBoundException {
		Registry reg = LocateRegistry.getRegistry(port);
		CANInterface pnode = (CANInterface) reg.lookup(id);
		return pnode;
	}
	
//----------------------RMI Methods-----------------------//
	@Override
	public ConnectionInfo getRandomPeer(ConnectionInfo con) throws RemoteException{
		System.out.println("You have contacted Bootstrap node.");
		if(peerList.isEmpty()){
			peerList.add(con);
			return null;
		}
		
		//TODO implement random peer selector and return conn info
		Random random = new Random();
		return peerList.get(random.nextInt(peerList.size()));
	}
	
	public void addPeer(ConnectionInfo coninfo) throws RemoteException{
		peerList.add(coninfo);
	}
	
	public void removePeer(ConnectionInfo coninfo){
		peerList.remove(coninfo);
	}
	
	public String getPeerInfo() throws RemoteException, NotBoundException{
		StringBuilder str = new StringBuilder();
		for(int i = 0; i < peerList.size(); i++){
			ConnectionInfo peerInfo = peerList.get(i);
			CANInterface peer = fetchPeer(peerInfo.getId(), peerInfo.getPort());
			str.append(peer.getPeerInfo());
			str.append("\n");
		}
		
		return str.toString();
	}	
	
	public String getPeerInfo(String peername) throws RemoteException, NotBoundException{
		ConnectionInfo peerInfo = fetchPeerConInfo(peername);
		if(peerInfo == null)
			return null;
		CANInterface peer = fetchPeer(peerInfo.getId(), peerInfo.getPort());
		return peer.getPeerInfo();
	}
	
//----------------------Driver----------------------//	
	public static void main(String[] args) throws RemoteException, MalformedURLException, AlreadyBoundException{
		Registry reg = LocateRegistry.createRegistry(1080);
		BootInterface node = new BootstrapNode("bootstrap", 1080);
		reg.bind("bootstrap", node);
		System.out.println("Bootstrap RMI Registered.");
	}

}
