import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

public class Peer extends UnicastRemoteObject implements CANInterface {
	private static final long serialVersionUID = 1L;
	private ConnectionInfo con;
	private Map<ConnectionInfo, Zone> neighbors;
	private Map<String, Object> data;
	private Zone zone;

	public Peer(String id, int port) throws RemoteException {
		con = new ConnectionInfo(id, getLocalIp(), port);
		init();
		// TODO Auto-generated constructor stub
	}

	private InetAddress getLocalIp() {
		try {
			return InetAddress.getLocalHost();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		return null;
	}

	// DEBUG
	private void joinNotice() {
		System.out
				.println("You have been accepted to the network. You will split zone/data with "
						+ this.con.getId() + ".");
	}

	private void inviteNotice(ConnectionInfo coninfo) {
		System.out.println("Node: " + coninfo.getId() + " has contacted me("
				+ this.con.getId()
				+ ") to join the network.\n Initiating join process....");
	}

	private void routeNotice(ConnectionInfo route) {
		print("Process will not end in " + this.con.getId()
				+ ". Routing to node: " + route.getId());
	}

	private Map<String, Object> splitData() {
		Map<String, Object> newMap = new HashMap<String, Object>();

		Set<String> keys = data.keySet();
		int size = data.size();
		int count = 0;
		List<String> keysToRemove = new ArrayList<String>();
		Iterator<String> iter = keys.iterator();
		while (iter.hasNext()) {
			if (count == ((size / 2) - 1))
				break;
			String key = iter.next();
			keysToRemove.add(key);
			newMap.put(key, data.get(key));
			count++;
		}

		// Remove the data which was split.
		for (int i = 0; i < keysToRemove.size(); i++) {
			data.remove(keysToRemove.get(i));
		}

		return newMap;
	}

	private Zone splitZone() {
		// TODO-DONE check if formula is correct or not.
		if (!zone.isSquare()) {
			// Split horizontally.

			double x1 = zone.x1;
			double y1 = (zone.y2 + zone.y1) / 2;
			double x2 = zone.x2;
			double y2 = zone.y2;

			zone.y2 = y1;

			return new Zone(x1, y1, x2, y2);
		} else {
			// Split vertically.
			double x1 = (zone.x2 + zone.x1) / 2;
			double y1 = zone.y1;
			double x2 = zone.x2;
			double y2 = zone.y2;

			zone.x2 = x1;

			return new Zone(x1, y1, x2, y2);
		}

	}

	// Helper method to find random coordinate set.
	private static double[] randomCoordinate() {
		double x = Math.random() * 10 + 0;
		double y = Math.random() * 10 + 0;

		return new double[] { x, y };
	}

	public ConnectionInfo getConInfo() {
		return con;
	}

	public void updateNeighbors(ConnectionInfo self_con, Zone self_z, ConnectionInfo con, Zone z)
			throws RemoteException, NotBoundException {
		Iterator<ConnectionInfo> iter = neighbors.keySet().iterator();
		while (iter.hasNext()) {
			try {
				ConnectionInfo key = iter.next();
				CANInterface neighbor = fetchPeer(key.getId(), key.getPort());
				neighbor.addNeighbor(self_con, z);
				neighbor.addNeighbor(con, z);
			} catch (ConcurrentModificationException ex) {
				System.out.println("ERROR: " + ex.getMessage());
			}
		}
		
	}

	public void sendFinalRouteToNode(ConnectionInfo coninfo, String route)
			throws RemoteException, NotBoundException {
		CANInterface node = fetchPeer(coninfo.getId(), coninfo.getPort());
		node.printFinalRoute(route);
		// node.
	}

	@Override
	public void printFinalRoute(String route) throws RemoteException {
		System.out.println(route);
	}

	@Override
	public void setZone(Zone zone) throws RemoteException {
		this.zone = zone;
		print("Setting zone for: " + this.con.getId() + ". Success.");
		zone.printZone();
	}

	@Override
	public void setData(Map<String, Object> data) throws RemoteException {
		// TODO Auto-generated method stub
		this.data = data;
		print("Setting data for: " + this.con.getId() + ". Success.");
	}

	@Override
	public void init() {
		neighbors = new HashMap<ConnectionInfo, Zone>();
		data = new HashMap<String, Object>();
		// TODO Possibly initialize zone here.
	}

	@Override
	public void insert(String key, Object value, double[] coordinate,
			String route, ConnectionInfo requestNode) throws RemoteException,
			NotBoundException {
		if (zone.isInZone(coordinate)) {
			data.put(key, value);
			route += " --> " + "IP: " + con.getIp().getHostAddress() + ":"
					+ con.getPort();
			String finalRoute = "Final Desination: " + con.getId() + ".\n"
					+ route + "\n";
			sendFinalRouteToNode(requestNode, finalRoute);
//			print(finalRoute);
		} else {
			double min = 10;
			ConnectionInfo con_key = null;
			Iterator<ConnectionInfo> iter = neighbors.keySet().iterator();
			while (iter.hasNext()) {
				ConnectionInfo neighbor_key = iter.next();
				Zone zone = neighbors.get(neighbor_key);
				double neighbor_dist = zone.getDistanceToCoordinate(zone,
						coordinate);
				if (neighbor_dist < min) {
					con_key = neighbor_key;
					min = neighbor_dist;
				}
			}
			// Now we have neighbor which is closest to the coordinate,
			// neighbor_key & min.
			CANInterface peer = fetchPeer(con_key.getId(), con_key.getPort());
			// Give routing responsibility to that neighbor.
			routeNotice(con_key);
			route += " --> " + "IP: " + con.getIp().getHostAddress();
			peer.insert(key, value, coordinate, route, requestNode);
		}
	}

	@Override
	public void search(String key, double[] coordinate, String route,
			ConnectionInfo requestNode) throws RemoteException,
			NotBoundException {
		if (zone.isInZone(coordinate)) {
			route += " --> " + "IP: " + con.getIp().getHostAddress() + ":"
					+ con.getPort();
			String finalRoute = "Final Desination: " + con.getId() + ".\n"
					+ route + "\n";
			sendFinalRouteToNode(requestNode, finalRoute);
	//		print(finalRoute);
		} else {
			double min = 10;
			ConnectionInfo con_key = null;
			Iterator<ConnectionInfo> iter = neighbors.keySet().iterator();
			while (iter.hasNext()) {
				ConnectionInfo neighbor_key = iter.next();
				Zone zone = neighbors.get(neighbor_key);
				double neighbor_dist = zone.getDistanceToCoordinate(zone,
						coordinate);
				if (neighbor_dist < min) {
					con_key = neighbor_key;
					min = neighbor_dist;
				}
			}
			// Now we have neighbor which is closest to the coordinate,
			// neighbor_key & min.
			CANInterface peer = fetchPeer(con_key.getId(), con_key.getPort());
			// Give routing responsibility to that neighbor.
			routeNotice(con_key);
			route += " --> " + "IP: " + con.getIp().getHostAddress();
			peer.search(key, coordinate, route, requestNode);
		}
	}

	@Override
	public void join(ConnectionInfo con, double[] coordinate)
			throws RemoteException, NotBoundException {
		inviteNotice(con);

		Zone z;
		Map<String, Object> newMap;
		if (zone.isInZone(coordinate)) {
			joinNotice();
			z = splitZone();
			// Get data. Split it into two Maps. Assign one to self, the other
			// to join node map.
			newMap = splitData();
			// Send Zone and Data;
			// Connect to join node and send the zone and data.
			Registry reg = LocateRegistry.getRegistry(con.getPort());
			CANInterface newNode;
			try {
				newNode = (CANInterface) reg.lookup(con.getId());
				newNode.setZone(z);
				newNode.setData(newMap);
				newNode.addNeighbor(this.con, this.zone);
				this.zone.printZone();
				BootInterface boot = fetchBootstrap();
				boot.addPeer(con);
				// Sending self zone update to neighbors
				updateNeighbors(this.con, this.zone, con, z);
				// Sending new node info to neighbors
				neighbors.put(con, z);
			} catch (NotBoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			// Route to neighbor and check.
			// Check neighbors zones
			double min = 10;
			ConnectionInfo key = null;
			Iterator<ConnectionInfo> iter = neighbors.keySet().iterator();
			while (iter.hasNext()) {
				ConnectionInfo neighbor_key = iter.next();
				Zone zone = neighbors.get(neighbor_key);
				double neighbor_dist = zone.getDistanceToCoordinate(zone,
						coordinate);
				if (neighbor_dist < min) {
					key = neighbor_key;
					min = neighbor_dist;
				}
			}
			// Now we have neighbor which is closest to the coordinate,
			// neighbor_key & min.
			CANInterface peer = fetchPeer(key.getId(), key.getPort());
			// Give routing responsibility to that neighbor.
			routeNotice(key);
			peer.join(con, coordinate);
		}
	}

	/**
	 * @throws NotBoundException
	 * @throws RemoteException
	 * @Override public String view(String peer) throws RemoteException {
	 * 
	 *           return null; }
	 * @Override public String view() throws RemoteException { // TODO
	 *           Auto-generated method stub return null; }
	 */

	public void leave() throws NotBoundException, RemoteException {
		BootInterface bootnode = fetchBootstrap();
		bootnode.removePeer(con);

		// alert neighbors about leave. and merge with one of them.
	}

	@Override
	public String getPeerInfo() throws RemoteException {
		// Fetch id, ip, zone coordinates, neighbors(peername), data
		String newline = "\n";
		StringBuilder str = new StringBuilder();
		str.append("Peer Name: " + con.getId());
		str.append(newline);
		str.append("IP: " + con.getIp().getHostAddress());
		str.append(newline);
		str.append("Coordinates:[" + "x1:" + zone.x1 + ", y1:" + zone.y1
				+ ", x2:" + zone.x2 + ", y2:" + zone.y2 + "]");
		str.append(newline);

		Iterator<ConnectionInfo> iter = neighbors.keySet().iterator();
		str.append("Neighbors:[");
		if (iter.hasNext())
			str.append(iter.next().getId());
		while (iter.hasNext()) {
			ConnectionInfo neighborCon = iter.next();
			str.append(", ");
			str.append(neighborCon.getId());
		}
		str.append("]");
		str.append(newline);

		str.append("Data:[");
		Iterator<String> iter_data = data.keySet().iterator();
		if (iter_data.hasNext())
			str.append(iter_data.next());
		while (iter_data.hasNext()) {
			String key = iter_data.next();
			str.append(", ");
			str.append(key);
		}
		str.append("]");
		str.append(newline);

		return str.toString();
	}

	@Override
	public void addNeighbor(ConnectionInfo con, Zone zone)
			throws RemoteException, NotBoundException {
		if (this.zone.isNeighbor(zone)){
			neighbors.put(con, zone);	
		}
	}

	@Override
	public void updateZone(int x1, int y1, int x2, int y2)
			throws RemoteException {
		this.zone = new Zone(x1, y1, x2, y2);
		// TODO Auto-generated method stub
	}

	@Override
	public void updateData(Map<String, Object> data) throws RemoteException {
		// TODO Auto-generated method stub
		this.data.putAll(data);
	}

	// -------------End of RMI Methods--------------//

	public static void main(String[] args) throws RemoteException,
			MalformedURLException, AlreadyBoundException, NotBoundException {
		// Register RMI of this node.
		String self_id = args[0];
		int self_port = Integer.valueOf(args[1]);
		Peer self_peer = new Peer(self_id, self_port);
		CANInterface self_peer_int = self_peer;
		registerRMI(self_id, self_port, self_peer_int);
		ConnectionInfo self_peer_con = self_peer.getConInfo();
		announceRMI(self_peer_con); // DEBUG

		print("Sending request to bootstrap node.");// DEBUG
		BootInterface bootnode = fetchBootstrap();
		print("bootstrap node object fetched successfully.");

		// For usage across main by all sections.
		CANInterface peer;
		ConnectionInfo con;
		double[] coordinate;

		String menu = "Please enter from 5 options." +
					"\n insert <keyword> <location> \n search <keyword> \n join \n leave \n view [all] , all command is optional.\n";
		
		while (true) {
			// Menu
			print(menu);
			Scanner sc = new Scanner(System.in);
			String option = sc.next();
			print("\n");

			if (option.equals("insert")) {
				// Scan filename and connection info
				String key = sc.next();
				String value = sc.next();
				coordinate = hash(key);
				con = bootnode.getRandomPeer(self_peer_con);
				peer = fetchPeer(con.getId(), con.getPort());
				String route = "IP Route: "
						+ self_peer_con.getIp().getHostAddress();
				peer.insert(key, value, coordinate, route, self_peer_con);
				// Initiate process
			} else if (option.equals("search")) {
				String key = sc.next();
				coordinate = hash(key);
				con = bootnode.getRandomPeer(self_peer_con);
				peer = fetchPeer(con.getId(), con.getPort());
				String route = "IP Route: "
						+ self_peer_con.getIp().getHostAddress() + ":"
								+ self_peer_con.getPort();
				peer.search(key, coordinate, route, self_peer_con);
				// Scan filename/key, get coordinates and
			} else if (option.equals("view")) {
				String peerinfo = null;
				if (sc.hasNext()) {
					String argument = sc.next();
					if (argument.equals("all"))
						peerinfo = bootnode.getPeerInfo();
					else
						peerinfo = bootnode.getPeerInfo(argument);
				}
				print(peerinfo);
			}
			// View all or view peer(by id)
			// Fetch id, ip, zone coordinates, neighbors(peername), data
			// items(keys)
			else if (option.equals("join")) {

				print("Fetch random peer now.");
				ConnectionInfo nodeToJoin = bootnode
						.getRandomPeer(self_peer_con);
				if (nodeToJoin == null) {
					// If no node exists in the network. Make this the first
					// node.
					print("This is the first node of the network.");
					Zone zone = new Zone(0, 0, 10, 10);
					self_peer_int.setZone(zone);
				} else {
					print("Received connection info of peer("
							+ nodeToJoin.getId()
							+ ") which will help us join the network.");
					InetAddress ip = nodeToJoin.getIp();
					String id = nodeToJoin.getId();
					int port = nodeToJoin.getPort();
					// Connect to ParentNode using connInfo
					CANInterface pnode = fetchPeer(id, port);
					// Now, the pnode will find a route, and tell us which node
					// to
					// contact and take space from.
					print("Connection request to " + id + " has been sent.");
					coordinate = randomCoordinate();
					print("We are looking for coordinate set [" + coordinate[0]
							+ "," + coordinate[1] + "]");
					pnode.join(self_peer_con, coordinate);
					// Route to the location where we need to go, once
					// ParentNode
					// decides where it will be.
					// Connect to that zone, receive new zone and data.
					// Do I update PeerList after the node has been given
					// zone/data or
					// when it returns ParentNode connInfo?
				}
				print("JOIN Process Complete.");
			} else if (option.equals("leave")) {
				print("NOT IMPLEMENTED YET.");
				// Implement leave
			}
		}
	}

	// ------------------RMI Helper methods-------------------//
	public static void registerRMI(String id, int port, CANInterface peer)
			throws RemoteException, AlreadyBoundException {
		Registry peerReg = LocateRegistry.createRegistry(port);
		peerReg.bind(id, peer);
	}

	public static BootInterface fetchBootstrap() throws RemoteException,
			NotBoundException {
		Registry reg = LocateRegistry.getRegistry(1080);
		BootInterface bootnode = (BootInterface) reg.lookup("bootstrap");
		return bootnode;
	}

	public static CANInterface fetchPeer(String id, int port)
			throws RemoteException, NotBoundException {
		Registry reg = LocateRegistry.getRegistry(port);
		CANInterface pnode = (CANInterface) reg.lookup(id);
		return pnode;
	}

	public static void announceRMI(ConnectionInfo con) {
		print("" + con.getId()
				+ " has establised its RMI connection over port: "
				+ con.getPort());
	}

	// ------------Helper Methods--------------//
	private static double[] hash(String keyword) {
		int odd = 0, even = 0;
		for (int i = 0; i < keyword.length(); i++) {
			if (i % 2 == 0) {
				even += keyword.charAt(i);
			} else {
				odd += keyword.charAt(i);
			}
		}
		return new double[] { even % 10, odd % 10 };
	}

	public static void print(String str) {
		System.out.println(str);
	}

}
