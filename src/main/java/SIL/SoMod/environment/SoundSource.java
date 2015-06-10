package SIL.SoMod.environment;

import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;

import com.vividsolutions.jts.geom.Coordinate;

public class SoundSource extends Coordinate {
	private double orientation; //angle from x-axis counterclockwise in radians
	private double hAngle;
	private double vAngle;
	private double volume;
	private int rays;
	
	public SoundSource(double x, double y, 
			double orientation, double hAngle, 
			double vAngle, int rays) {
		super(x,y);
		this.orientation = orientation;
		this.hAngle = hAngle;
		this.vAngle = vAngle;
		this.rays = rays;
	}
	
	public double getHorizontalAngle() {
		return this.hAngle;
	}
	
	public double getOrientation() {
		return this.orientation;
	}
	
	public double getVolume() {
		return this.volume;
	}
	
	public int getRayAmount() {
		return this.rays;
	}
	
	public void setVolume(double vol) {
		this.volume = vol;
	}
	/**
	 * return horizontal angles left and right from orientation based on the horizontal angle 
	 * of sight.
	 * @return double array containing the left and right angle of the horizontal sight in radians
	 */
	public double[] getHorizontalRange() {
		double halfAngle = this.hAngle/2.0;
		double left = this.orientation+halfAngle;
		double right = this.orientation -halfAngle;
		double[] result = {left, right};
		return result;
	}
	public double calculateEmissionVolume() {
		//TODO change this due to the higher sound intensity at a limited soundwave surface
		// but leave this for now
		return this.volume;
	}
}
