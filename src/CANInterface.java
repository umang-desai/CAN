import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Map;


public interface CANInterface extends Remote{
	
    public void insert(String key, Object value, double[] coordinate, String route, ConnectionInfo requestNode) throws RemoteException, NotBoundException;

    public void search(String keyword, double[] coordinate, String route, ConnectionInfo requestNode) throws RemoteException, NotBoundException;

    public void join(ConnectionInfo con, double[] coordinate) throws RemoteException, NotBoundException;

 /*   public String view(String peer) throws RemoteException;

    public String view() throws RemoteException;
*/
    public String getPeerInfo() throws RemoteException;
    
    public void printFinalRoute(String str) throws RemoteException;
    
	public void init() throws RemoteException;

	public void setZone(Zone zone) throws RemoteException;
    
	public void setData(Map<String, Object> data) throws RemoteException;
	
	public void addNeighbor(ConnectionInfo con, Zone zone) throws RemoteException, NotBoundException;
	
    public void updateZone(int x1, int y1, int x2, int y2) throws RemoteException;
    
    public void updateData(Map<String, Object> data) throws RemoteException;
}
