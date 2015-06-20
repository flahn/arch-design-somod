package SIL.SoMod.environment;

import com.vividsolutions.jts.geom.Coordinate;

public class SoundPoint2D extends PropagationPathPoint {

	public SoundPoint2D(Coordinate c) {
		super(c.x,c.y);
	}

	@Override
	public void setIncomingVolume(double incomingVolume) {
		this.incomingVolume = incomingVolume;
		this.outgoingVolume = incomingVolume;
	}

	@Override
	public double getOutgoingVolume() {
		return this.outgoingVolume;
	}	
}
