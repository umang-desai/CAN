import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;


public interface BootInterface extends Remote{

	public ConnectionInfo getRandomPeer(ConnectionInfo con) throws RemoteException;
	public void addPeer(ConnectionInfo con) throws RemoteException;
	public void removePeer(ConnectionInfo con) throws RemoteException;
	public String getPeerInfo(String peername) throws RemoteException, NotBoundException;
	public String getPeerInfo() throws RemoteException, NotBoundException;
}
