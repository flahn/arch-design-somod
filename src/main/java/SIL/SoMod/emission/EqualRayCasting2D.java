package SIL.SoMod.emission;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import SIL.SoMod.CalculationUtils;
import SIL.SoMod.environment.Environment;
import SIL.SoMod.environment.SoundSource;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.LineSegment;
import com.vividsolutions.jts.geom.LineString;

public class EqualRayCasting2D extends AbstractSoundEmissionModel2D {
	
	public EqualRayCasting2D() {
		this.propagationPaths = new HashMap<SoundSource,List<LineString>>();
	}
	
	public EqualRayCasting2D(double audioThreshold) {
		this.propagationPaths = new HashMap<SoundSource,List<LineString>>();
		this.audioThreshold = audioThreshold;
	}
	
	public EqualRayCasting2D(int bounces) {
		this.propagationPaths = new HashMap<SoundSource,List<LineString>>();
		this.bounce_level = bounces;
		//yolo, i don't care version
		// no actually sound model does care
	}
	
	public EqualRayCasting2D (List<SoundSource> s, Environment e) {
		this.init();
		this.sources.addAll(s);
		this.environment = e;
		this.bounce_level = 0;
	}
	
	public EqualRayCasting2D (List<SoundSource> s, Environment e, int bounces) {
		this.init();
		this.sources.addAll(s);
		this.environment = e;
		this.bounce_level = bounces;
	}
	private void init() {
		this.sources = new ArrayList<SoundSource>();
	}
	
	@Override
	public List<LineSegment> calculateInitialPropagationPaths(SoundSource s) {
		if (s.getRayAmount() > 0 && s != null && this.environment != null && !this.environment.getReflectionSegments().isEmpty()) {
			List<LineSegment> eRays = this.createEmissionRays(s);
			return eRays;
		} else {
			return null; //TODO throw Exception
		}
		
	}
	
	private List<LineSegment> createEmissionRays(SoundSource source) {
		List<LineSegment> initLines = new ArrayList<LineSegment>();
		double[] hRange = source.getHorizontalRange();
		double[] hEmissionAngles = CalculationUtils.calculateEmissionAngles(hRange[0], hRange[1], source.getRayAmount(), false); 
		double maxDistance = this.environment.getMaximumSpan();
		//the goal is to create lines that are that long that they intersect with an environment object
		for (int i = 0; i < hEmissionAngles.length; i++) {
			double angle = hEmissionAngles[i];
			Coordinate endCoordinate = CalculationUtils.azimuthalCalculation(source, angle, maxDistance);
			LineSegment newOne = new LineSegment(source, endCoordinate);
			initLines.add(newOne);
		}
		//trim the rays with the environment
		List<LineSegment> initPaths = CalculationUtils.trim(initLines, this.environment);
		return initPaths;
	}


}
