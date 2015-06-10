package SIL.SoMod.attenuation;

import SIL.SoMod.environment.ReflectionPoint2D;

import com.vividsolutions.jts.geom.Coordinate;

public interface IAttenuationModel {
	public double reflection(ReflectionPoint2D reflection);
	
	/*
	 * This is a rather simple approach we neglect air pressure, temperature and stuff for now
	 */
	public double atmospheric_attenuation(ReflectionPoint2D start, Coordinate end);
	
	public Coordinate calculateVolumeTheshold(ReflectionPoint2D start, ReflectionPoint2D end, double threshold);
}
