package SIL.SoMod.environment;

import com.vividsolutions.jts.geom.Coordinate;

public abstract class PropagationPathPoint extends Coordinate{
	protected double distanceFromSource;
	protected double incomingVolume;
	protected double outgoingVolume;
	
	PropagationPathPoint() {
		super();
	}
	PropagationPathPoint(Coordinate c) {
		super(c.x,c.y);
	}
	PropagationPathPoint (double x, double y) {
		super(x,y);
	}

	public double getDistanceFromSource() {
		return distanceFromSource;
	}

	public void setDistanceFromSource(double distanceFromSource) {
		this.distanceFromSource = distanceFromSource;
	}
	
	public double getIncomingVolume() {
		return this.incomingVolume;
	}

	abstract public void setIncomingVolume(double incomingVolume);

	abstract public double getOutgoingVolume();
}
