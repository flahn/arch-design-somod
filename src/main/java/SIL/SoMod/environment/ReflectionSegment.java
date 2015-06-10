package SIL.SoMod.environment;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.LineSegment;

public class ReflectionSegment extends LineSegment {
	private double reflection_coefficient;
	
	/**
	 * Constructor of the Wall element
	 * @param reflection_coeff
	 */
	public ReflectionSegment (double reflection_coeff, Coordinate c0, Coordinate c1) {
		super(c0, c1);
		this.reflection_coefficient = reflection_coeff;
	}
	
	public double getReflectionCoefficient() {
		return this.reflection_coefficient;
	}
	
	public void setReflectionCoefficient(double value) {
		this.reflection_coefficient = value;
	}
}
