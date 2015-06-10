package SIL.SoMod;

import java.util.ArrayList;
import java.util.List;

import SIL.SoMod.attenuation.AttenuationModel;
import SIL.SoMod.environment.Environment;
import SIL.SoMod.environment.ReflectionPoint2D;
import SIL.SoMod.environment.ReflectionSegment;

import com.vividsolutions.jts.algorithm.Angle;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.LineSegment;

public class CalculationUtils {
	
	public static double[] calculateEmissionAngles(double start, double end, int divisions, boolean degree) {
		if(degree) {
			start = Angle.toRadians(start);
			end = Angle.toRadians(end);
		}
		double[] angles = new double[divisions];
		
		for(int i= 0; i < (divisions); i++) {
			angles[i] = start+(i * (end-start)/divisions);
		}
		
		return angles;
	};
	
	public static Coordinate azimuthalCalculation(Coordinate src, double angle, double distance) {
		double deast = distance * Math.cos(angle);
        double dnorth = distance * Math.sin(angle);
        
        double x = src.x + deast;
        double y = src.y + dnorth;
        
        Coordinate newC = new Coordinate(x,y);
        return newC;
	}
	public static LineSegment trim(LineSegment ray, Environment env) {
		List<LineSegment> tempIntersected = new ArrayList<LineSegment>();
		List<ReflectionSegment> walls = env.getReflectionSegments();
		
		//calculate candidates
		for (ReflectionSegment wall : walls) {
			Coordinate intersection = ray.intersection(wall);
			if ( intersection != null) {
				LineSegment ls = new LineSegment(ray.p0,intersection);
				ReflectionPoint2D ref = new ReflectionPoint2D(wall,ls); //basically creates and substitutes point
				tempIntersected.add(ls);
			}
		}

		LineSegment closestIntersection = tempIntersected.get(0);

		//select the intersection closest to the start (first intersection)
		final double CALC_ERROR = 0.00000001;
		for (int i = 1; i < tempIntersected.size(); i++) {
			LineSegment current = tempIntersected.get(i);

			if (closestIntersection.getLength() == 0.0) { //if the line is just two times the start point
				closestIntersection = current; // then take the current one
				//this can only happen at the beginning
			} else if (current.getLength() < closestIntersection.getLength() && current.getLength() >= CALC_ERROR) {
				closestIntersection = current;
			}
		}
		
		//calculate the volume at this  point
		double newVolume = AttenuationModel.atmospheric_attenuation( closestIntersection.p0, closestIntersection.p1);
		((ReflectionPoint2D)closestIntersection.p1).setIncomingVolume(newVolume);

		return closestIntersection;
	}
	
	public static List<LineSegment> trim(List<LineSegment> inits, Environment env) {
		List<LineSegment> intersected = new ArrayList<LineSegment>();
		
		//for each LineSegment of the inital rays
		for (LineSegment ray : inits) {
			intersected.add(CalculationUtils.trim(ray, env));
		}
		return intersected;
	}
	
	public static int orientation(Coordinate point, LineSegment line) {
		final int LEFT = -1;
		final int RIGHT = 1;
		double orientedAngle = Angle.toDegrees(Angle.angleBetweenOriented(point, line.p0, line.p1));
		if(orientedAngle <= 0) {
			return LEFT;
		} else return RIGHT;
	}
	
	/**
	 * Calculates the reflection line of an incoming line at a given surface and returns
	 * an untrimmed ray of the outgoing ray. The intersection will later be added by the
	 * trim() operation by replacing the end point of the segment.
	 * 
	 * @param inputRay LineSegment The incoming ray as a LineSegment.
	 * @param offsetDistance double A distance to be used to calculate an arbitrary endpoint
	 * of the ray. Usually you will use the environments maximal span.
	 * @return The outgoing ray as a LineSegment with an arbitrary endpoint along the direction.
	 */
	public static LineSegment reflect(LineSegment inputRay, double offsetDistance) {
		ReflectionPoint2D intersection = (ReflectionPoint2D)inputRay.p1; //after init every endpoint is this, except the last one which is a SoundPoint
		
		
		//outgoing volume is calculated automatically
		
		ReflectionSegment rs = intersection.getReflector();
		Coordinate imageSource = CalculationUtils.mirror(inputRay.p0, rs);
		Coordinate newInlineEndpoint = CalculationUtils.continueLine(imageSource,intersection,offsetDistance);

		return new LineSegment(intersection,newInlineEndpoint);
	}
	
	/**
	 * Caclulates a point at a certain distance of the end point continuing the inital line
	 * of start and end point.
	 * @param start Coordinate. Start point
	 * @param end Coordinate. End point
	 * @param distance double. Offset distance to be applied after the end point
	 * @return
	 */
	public static Coordinate continueLine(Coordinate start, Coordinate end, double distance) {
		double outputAngle = Angle.angle(start, end);
		Coordinate newInlineEndpoint = CalculationUtils.azimuthalCalculation(end, outputAngle, distance);
		return newInlineEndpoint;
	}
	
	public static Coordinate mirror(Coordinate source, LineSegment ls) {
		Coordinate image;
		Coordinate start = ls.p0;
		Coordinate end = ls.p1;
		double dist = start.distance(source);
		double angle = Angle.angleBetween(source, start, end);
		double nAngle = ls.angle()+CalculationUtils.orientation(source, ls)*angle;
		
		image = CalculationUtils.azimuthalCalculation(start, nAngle, dist);
		
		
		return image;
	}
}
