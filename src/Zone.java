import java.io.Serializable;

public class Zone implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	double x1, y1;// top left
	double x2, y2;// bottom left
	double dx, dy;

	public Zone(double x1, double y1, double x2, double y2) {
		super();
		this.x1 = x1;
		this.y1 = y1;
		this.x2 = x2;
		this.y2 = y2;

		this.dx = (x1 + x2) / 2;
		this.dy = (y1 + y2) / 2;
	}

	public boolean isInZone(double[] coordinate) {
		double x = coordinate[0];
		double y = coordinate[1];

		if (x >= x1 && x <= x2)
			if (y >= y1 && y <= y2) {
				return true;
			}
		return false;
	}

	public boolean isSquare() {
		if ((x1 - x2) == (y1 - y2)) {
			return true;
		}
		return false;
	}

	public boolean isNeighbor(Zone zone) {
		if (checkneighborOnX(x1, x2, zone.x1, zone.x2)
				|| checkneighborOnY(y1, y2, zone.y1, zone.y2))
			return true;
		return false;
	}

	public double getDistanceToCoordinate(Zone zone, double[] coordinate) {
		double distance = 0;
		double x = coordinate[0];
		double y = coordinate[1];

		if (zone.dx == x)
			distance = y - zone.dy;
		else if (zone.dy == y)
			distance = x - zone.dx;
		else {
			double x_sqr = x * x;
			double y_sqr = y * y;
			distance = (int) Math.sqrt(x_sqr + y_sqr);
		}
		return distance;
	}

	public boolean isCloserThan(Zone zone, double[] coordinate) {
		double selfDistance = getDistanceToCoordinate(this, coordinate);
		double otherDistance = getDistanceToCoordinate(zone, coordinate);

		if (selfDistance < otherDistance)
			return true;
		else
			return false;
	}

	private boolean checkneighborOnY(double y1, double y2, double remote_y1,
			double remote_y2) {
		if (y1 >= remote_y1 && y2 <= remote_y2)
			return true;
		else if (remote_y1 >= y1 && remote_y2 <= y2)
			return true;
		return false;
	}

	private boolean checkneighborOnX(double x1, double x2, double remote_x1,
			double remote_x2) {
		if (x1 >= remote_x1 && x2 <= remote_x2)
			return true;
		else if (remote_x1 >= x1 && remote_x2 <= x2)
			return true;
		return false;
	}
	
	public void printZone(){
		System.out.println("Coordinates. " + "x1:" + x1 + ", y1:" + y1 + ", x2:" + x2
				+ ", y2:" + y2);
	}
}
