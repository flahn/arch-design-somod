package SIL.SoMod;

import java.util.ArrayList;
import java.util.List;

import SIL.SoMod.emission.EqualRayCasting2D;
import SIL.SoMod.emission.PreciseBoundary2D;
import SIL.SoMod.emission.SoundEmissionModel;
import SIL.SoMod.environment.Environment;
import SIL.SoMod.environment.ReflectionPoint2D;
import SIL.SoMod.environment.ReflectionSegment;
import SIL.SoMod.environment.SoundSource;

import com.vividsolutions.jts.algorithm.Angle;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineSegment;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.simplify.DouglasPeuckerSimplifier;

public class SoundModel {
	private List<SoundSource> sources = null;
	private Environment environment = null;
	private SoundEmissionModel emissionModel = null;
	private int bounceLevel;
	private boolean hasRun;
	private boolean hasChanged;
	private List<List<LineString>> propagationPaths;
	private double volumeThreshold;
	
	public enum EmissionModel {
		EQUAL_RAY_TRACING_2D, PRECISE_BOUNDARY_2D
	}
	
	public SoundModel(EmissionModel type) {
		
	}
	
	public SoundModel(EmissionModel type,int bounces) {
		this.init();
		this.bounceLevel = bounces;
		this.emissionModel = this.createModel(type,bounces);
	}
	
	private SoundEmissionModel createModel(EmissionModel type) {
		switch (type) {
		case EQUAL_RAY_TRACING_2D:
			return new EqualRayCasting2D();
		case PRECISE_BOUNDARY_2D:
			return new PreciseBoundary2D();
		}
		return null;
	}
	
	private SoundEmissionModel createModel(EmissionModel type,int bounces){
		switch(type) {
		case EQUAL_RAY_TRACING_2D: 
			return new EqualRayCasting2D(bounces);
		case PRECISE_BOUNDARY_2D:
			return new PreciseBoundary2D();
		}
		
		return null;
	}
	private void init() {
		this.sources = new ArrayList<SoundSource>();
		this.environment = new Environment();
		this.hasRun = false;
		this.hasChanged = true;
	}
	
	public boolean addSource(SoundSource s) {
		this.hasChanged = true;
		return this.sources.add(s);
	}
	
	public boolean run() {
		if (this.hasChanged) {
			//feed the same objects here into the eModel
			this.emissionModel.setEnvironment(this.environment);
			this.emissionModel.setSources(this.sources);
			this.emissionModel.bounce();
			this.propagationPaths = this.emissionModel.getPropagationPaths();
			this.hasChanged = false;
			this.hasRun = true;
		}
		//TODO catch here all thrown exceptions and return  false
		return true;
	}
	
	public MultiPolygon getBouncePolygon(int step, boolean cumulative, SoundSource s) {
		if (!cumulative) {
			List<LineSegment> pathSubset = this.getBounceLineSegments(step, s);
			if (step == 0) {
				return merge_0_bounce(pathSubset, s);
			} else {
				MultiPolygon mp = merge_upper_bounce(pathSubset); //might be null
				return mp;
			}
		} else {
			List<MultiPolygon> list = new ArrayList<MultiPolygon>();
			if (step < 0) {
				//this means we want to use all line segments but not by bounces, but by threshold
				//now look for the maximum number of line segments
				int max = 0;
				for (List<LineString> l : this.propagationPaths) {
					for (LineString li : l) {
						int bounces = li.getNumPoints()-2;
						if (bounces > max) max=bounces;
					}
				}
				step = max;
			}
			for (int i = 0; i <= step; i++) {
				List<LineSegment> pathSubset = this.getBounceLineSegments(i, s);
				if (i == 0) {
					list.add(merge_0_bounce(pathSubset, s));
				} else {
					MultiPolygon mp = merge_upper_bounce(pathSubset);
					if (mp != null) {
						list.add(mp);
					}
				}
			}
			
			Geometry all = list.get(0);
			for (int i = 1; i < list.size(); i++) {
				all = all.union(list.get(i));
			}
			
			if (all instanceof Polygon) {
				GeometryFactory gf = new GeometryFactory();
				Polygon[] polygons = {(Polygon)all};
				all = gf.createMultiPolygon(polygons);
			}
			return (MultiPolygon)all;
		}
	}
	
	private MultiPolygon merge_0_bounce(List<LineSegment> pathSubset, SoundSource s) {
		int numberOfPoints = pathSubset.size()+1; //one for the same endpoint as start
		int index = 0;
		Coordinate[] outerRing;
		if(Angle.toDegrees(s.getHorizontalAngle()) != 360.0) {
			numberOfPoints++;
			outerRing = new Coordinate[numberOfPoints];
			outerRing[index++] = pathSubset.get(0).p0;
		} else {
			outerRing = new Coordinate[numberOfPoints];
		}
		
		for (LineSegment l : pathSubset) {
			outerRing[index++] = l.p1;
		}
		outerRing[index] = outerRing[0];
		GeometryFactory geometryFactory = new GeometryFactory();
		Polygon p = geometryFactory.createPolygon(outerRing);
		Polygon[] polys = {p};
		return geometryFactory.createMultiPolygon(polys);
	}
	
	private MultiPolygon merge_upper_bounce(List<LineSegment> pathSubset) {
		List<Polygon> sub_polygons = new ArrayList<Polygon>();
		GeometryFactory geometryFactory = new GeometryFactory();
		Geometry currentPolygon = null;
		LineSegment lastAdded = null;
		//create initial polygon from the first two lines that are not intersecting
		for (LineSegment currentLine : pathSubset) {
			int currentPosition = pathSubset.indexOf(currentLine);
			if (currentPosition == 0) continue;
			if (currentPolygon == null || !currentPolygon.isSimple()) { //create seed polygon
				for(int i = currentPosition; i < pathSubset.size(); i++) {
					LineSegment priorLine = pathSubset.get(i-1);
					//discard intersecting line because subpolygons should not be self intersecting
					if (currentLine.intersection(priorLine) != null) continue;
					
					//create polygon
					Coordinate[] coords = new Coordinate[5];
					coords[0] = coords[4] = priorLine.p0;
					coords[1] = priorLine.p1;
					coords[2] = currentLine.p1;
					coords[3] = currentLine.p0;
					
					currentPolygon = geometryFactory.createPolygon(coords);
					
					lastAdded = currentLine;
					currentPosition = i+1;
					break;
				}
				continue;
			} else {
				ReflectionSegment r1 = null,r2 = null;
				if (currentLine.p0 instanceof ReflectionPoint2D) {
					ReflectionPoint2D ref = (ReflectionPoint2D)currentLine.p0; //ossible because we swap those coordinates a trim-op
					r1 = ref.getReflector();
				}
				if (lastAdded.p0 instanceof ReflectionPoint2D) {
					ReflectionPoint2D ref2 = (ReflectionPoint2D)lastAdded.p0;
					r2 = ref2.getReflector();
				}
				
				if (r1 == null || r2 == null) {
					//TODO handle the case when the endpoints are not reflection points but sound points in the environment
				} else 
					if ( r1 != r2) {
					//it intersects, then start new polygon
					sub_polygons.add((Polygon)currentPolygon);
					currentPolygon = null;
					lastAdded = null;
					continue;
				} else {
					Coordinate[] c = {currentLine.p1, currentLine.p0,lastAdded.p0,lastAdded.p1,currentLine.p1};
					Polygon poly = geometryFactory.createPolygon(c);
					currentPolygon = new DouglasPeuckerSimplifier(currentPolygon.union(poly)).getResultGeometry();
					lastAdded = currentLine;
					continue;
					
				}
			}
		}
		if (currentPolygon != null) sub_polygons.add((Polygon)currentPolygon);
		
		//now all sub_polygons are cerated, merge them
		Polygon[] ps = new Polygon[sub_polygons.size()];
		ps = sub_polygons.toArray(ps);

		MultiPolygon mp = geometryFactory.createMultiPolygon(ps);
		Geometry geom = mp.union();
		if (!geom.isEmpty()) {
			if (geom instanceof Polygon) {
				Polygon[] arr = new Polygon[1];
				arr[0] = (Polygon)geom;
				return geometryFactory.createMultiPolygon(arr);
			}else {
				return (MultiPolygon)geom;
			}
		} else {
			return null;
		}
		
	}
	
	public List<LineSegment> getBounceLineSegments(int bounce,SoundSource s) {
		List<LineString> paths = this.emissionModel.getPropagationPaths(s);
		List<LineSegment> lines = new ArrayList<LineSegment>();
		for (LineString ls : paths) {
			if (bounce+2 > ls.getNumPoints()) continue;
//			if (bounce +1 > ls.getCoordinates().length) {
//				throw new IndexOutOfBoundsException("Indicated bounce level higher than calculated in propagation paths.");
//			}
			lines.add(new LineSegment(
					
					ls.getCoordinateN(bounce),
					ls.getCoordinateN(bounce+1)));
		}
		
		return lines;
	}
	
	/*
	 * getter / setters
	 */
	public List<SoundSource> getSources() {
		return sources;
	}

	public void setSources(List<SoundSource> sources) {
		this.hasChanged = true;
		this.sources = sources;
	}

	public SoundEmissionModel getEmissionModel() {
		return emissionModel;
	}

	public void setEmissionModel(SoundEmissionModel emissionModel) {
		this.hasChanged = true;
		this.emissionModel = emissionModel;
	}
	
	public boolean setEnvironment(Environment e) {
		if (e != null) {
			this.environment = e;
			return true;
		} else return false;
	}
	
	public Environment getEnvironment() {
		return environment;
	}
	
	public List<List<LineString>> getPropagationPaths() {
		if (!this.hasRun) { 
			this.run();
		}
		return this.propagationPaths;
	}
	public List<LineString> getPropagationPaths(SoundSource s) {
		if (!this.hasRun) { 
			this.run();
		}
		return this.emissionModel.getPropagationPaths(s);
	}
	
	public int getBounceLevel() {
		return this.bounceLevel;
	}

	public double getVolumeThreshold() {
		return volumeThreshold;
	}

	public void setVolumeThreshold(double volumeThreshold) {
		this.volumeThreshold = volumeThreshold;
		this.emissionModel.setAudioThreshold(volumeThreshold);
		this.hasChanged = true;
	}
	
}
