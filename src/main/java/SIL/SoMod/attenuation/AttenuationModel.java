package SIL.SoMod.attenuation;

import SIL.SoMod.environment.ReflectionPoint2D;
import SIL.SoMod.environment.SoundPoint2D;
import SIL.SoMod.environment.SoundSource;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.LineSegment;

public class AttenuationModel {
	public static final double REFERENCE_SOUND_POWER = Math.pow(10, -12); //also called reference sound power
	//http://en.wikipedia.org/wiki/Density_of_air
	public static final double SPEED_OF_SOUND_DRY_AIR = 331.3; // m/s at 0 °C 
	public static final double DRY_AIR_DENSITY = 1.2754; // kg/m^3 at 0 °C / 273.15 °K / 1000hPa
	
	
	public static double soundSpeedDryAir(double temperature) {
		return (AttenuationModel.SPEED_OF_SOUND_DRY_AIR+0.606 * temperature);
	}
	
	private static double soundPower2soundPressureLevel (double power) {
		return 10*Math.log10(power/AttenuationModel.REFERENCE_SOUND_POWER);
	}
	private static double soundPressureLevel2soundPower(double pressureLevel) {
		return Math.pow(10, (pressureLevel/10))*AttenuationModel.REFERENCE_SOUND_POWER;
	}

	public static double atmospheric_attenuation(Coordinate start, Coordinate end) {
		double distance = start.distance(end);
		if (distance == 0) throw new IllegalArgumentException("coordinates are identical");
		double originVolume;
		double soundPressure;
		if (start instanceof SoundSource) {
			originVolume = ((SoundSource)start).calculateEmissionVolume();
		} else if (start instanceof ReflectionPoint2D) {
			originVolume = ((ReflectionPoint2D)start).getOutgoingVolume() ;
		} else {
			throw new IllegalArgumentException("The coordinate cannot be transformed into ReflectionPoint2D or SoundSource");
		}
		soundPressure = originVolume - Math.abs(20.0*Math.log10(1.0/distance));
//		System.out.println("origin: "+originVolume+", Distance: "+distance+", soundPressure: "+soundPressure);
		
		//calculating the attenuation by using
		// inverse square law for sound pressure (for every doubling distance from
		// noise the sound pressure level will decrease by approx. 6dB)
		// all db values for a source are considered to be perceived at 1 m distance
		
		return soundPressure;
	}
	
	private static double distance2Threshold(double soundPressureStart, double threshold) {
		return (Math.pow(10, ((soundPressureStart-threshold)/20)));
	}

	public static SoundPoint2D calculateVolumeThreshold(Coordinate start,
			ReflectionPoint2D end, double threshold) {
		double dist = 0;
		double outgoingVolume;
		if (start instanceof ReflectionPoint2D) {
			outgoingVolume =((ReflectionPoint2D)start).getOutgoingVolume();
		}else if (start instanceof SoundSource) {
			outgoingVolume = ((SoundSource)start).calculateEmissionVolume();
		} else {
			throw new IllegalArgumentException("The coordinate cannot be transformed into ReflectionPoint2D or SoundSource");
		}
		dist = AttenuationModel.distance2Threshold(outgoingVolume, threshold);
		LineSegment ls = new LineSegment(start, end);
		SoundPoint2D sp = new SoundPoint2D(ls.pointAlong(dist/ls.getLength()));
//		System.out.println(start+" || "+sp+" || "+end);
		sp.setVolume(threshold);
		return sp;
	}
	
}
