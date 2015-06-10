package SIL.SoMod.environment;

import com.vividsolutions.jts.geom.Coordinate;

public class SoundPoint2D extends Coordinate{
	private double volume;

	public SoundPoint2D(Coordinate c) {
		this.x = c.x;
		this.y = c.y;
		
	}
	
	public double getVolume() {
		return volume;
	}

	public void setVolume(double volume) {
		this.volume = volume;
	}
	
	
}
