package SIL.SoMod;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import SIL.SoMod.emission.EqualRayCasting2D;
import SIL.SoMod.emission.PreciseBoundary2D;
import SIL.SoMod.emission.SoundEmissionModel;
import SIL.SoMod.environment.Environment;
import SIL.SoMod.environment.ReflectionPoint2D;
import SIL.SoMod.environment.SoundSource;

import com.vividsolutions.jts.algorithm.Angle;
import com.vividsolutions.jts.algorithm.ConvexHull;
import com.vividsolutions.jts.algorithm.RobustCGAlgorithms;
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
	private HashMap<SoundSource,SoundGraph> table;
	private HashMap<SoundSource,List<MultiPolygon>> soundAreas;
	
	private class SoundGraph {
		public Coordinate[][] graph;
		public int branches;
		public int deepness;
		public SoundGraph(Coordinate[][] map, int b, int d) {
			this.graph = map;
			this.branches = b;
			this.deepness = d;
		}
	}
	
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
			this.emissionModel.propagate();
			this.propagationPaths = this.emissionModel.getPropagationPaths();
			this.createCoordinatesTable();
			//TODO reenable
			for(SoundSource s : this.sources){
				this.calculateAudibleAreas(s);
			}
			
			this.hasChanged = false;
			this.hasRun = true;
		}
		//TODO catch here all thrown exceptions and return  false
		return true;
	}
	
	public MultiPolygon getBouncePolygon(int step, boolean cumulative, SoundSource s) {
		List<MultiPolygon> areas = this.soundAreas.get(s);
		if (step < 0) {
			step = areas.size()-1;
		}
		if (step > areas.size()) {
			//throw new ArrayIndexOutOfBoundsException("The amount of steps exceeds the maximum calculated bounces by threshold.");
			step = areas.size()-1;
		} 
	
			if (! cumulative) {
				return areas.get(step);
			} else {
				Geometry all = areas.get(0);
				for (int i = 1; i < areas.size(); i++) {
					all = all.union(areas.get(i));
				}
				
				if (all instanceof Polygon) {
					GeometryFactory gf = new GeometryFactory();
					Polygon[] polygons = {(Polygon)all};
					all = gf.createMultiPolygon(polygons);
				}
				return (MultiPolygon)all;
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
	
	private void createCoordinatesTable() {
		this.table = new HashMap<SoundSource,SoundGraph>();
		for (SoundSource s : this.sources) {
			int maxDeep = 0;
			List<LineString> paths = this.emissionModel.getPropagationPaths(s);
			for (LineString path : paths) {
				if (path.getNumPoints()-1 > maxDeep) {
					maxDeep = path.getNumPoints()-1;
				}
			}
			Coordinate[][] graph = new Coordinate[paths.size()][maxDeep];
			for (int path = 0; path <paths.size(); path++){
				LineString p = paths.get(path);
				//TODO Debug: find the outliers and print them
//				if (p.getEndPoint().getCoordinate().y < 0) {
//					System.out.println("Path "+path+":");
//					System.out.println(p);
//					System.out.println("Reflectors:");
//					for (int i = 0; i< p.getNumPoints(); i++) {
//						if (p.getCoordinateN(i) instanceof ReflectionPoint2D) {
//							System.out.println(((ReflectionPoint2D)p.getCoordinateN(i)).getReflector());
//						}
//					}
//				}
				for (int bounce = 0; bounce+1 <= maxDeep; bounce++) {
					if (bounce+1 > p.getNumPoints()-1) break;
					graph[path][bounce] = paths.get(path).getCoordinateN(bounce+1); //bounce+1 because we skip the source
				}
			}
			this.table.put(s, new SoundGraph(graph,paths.size(),maxDeep));
		}
	}

	public void calculateAudibleAreas(SoundSource s) {
		if (this.soundAreas == null) {
			this.soundAreas = new HashMap<SoundSource,List<MultiPolygon>>();
		}
		//using this.volumeThreshold as the minimum
		SoundGraph sg = this.table.get(s);
		Coordinate[][] graph = sg.graph;
		GeometryFactory geometryFactory = new GeometryFactory();
		List<MultiPolygon> soundAreas = new ArrayList<MultiPolygon>();
		
		for (int level= 0; level < sg.deepness; level++) {
			
			if (level==0) {
				//join coordinates with source
				//if not 360° emission source has to be used as point as well
				Coordinate[] outerRing;
				int start = 0;
				int numberOfPoints = sg.branches+1; //Polygon need to have same start and end point
				if(Angle.toDegrees(s.getHorizontalAngle()) != 360.0) {
					numberOfPoints++;
					outerRing = new Coordinate[numberOfPoints]; //source needs to be included into ring
					outerRing[start++] = s; //set it to the last
				} else {
					outerRing = new Coordinate[numberOfPoints];
				}
				
				for (int branch = 0; branch < sg.branches; branch++) {
					outerRing[start+branch] = graph[branch][level];
				}
				outerRing[numberOfPoints-1] = outerRing[0];
				
				Polygon p = geometryFactory.createPolygon(outerRing);
				Polygon[] polys = {p};
				
				soundAreas.add(geometryFactory.createMultiPolygon(polys)); //first level is direct audibility
			} else {
				//create and join polygons with the coordinates of the prior level
				List<Polygon> sub_polygons = new ArrayList<Polygon>();
				Geometry currentPolygon = null;
				LineSegment lastAdded = null;
				boolean lastWasLine = false;
				//create initial polygon from the first two lines that are not intersecting
							
				for (int branch = 0; branch < sg.branches; branch++) {
					int currentPosition = branch;
					
					if (graph[branch][level] == null) {
						//this means there are other paths with more reflections, but those are
						// will be handled later (maybe just one line...)
						if (currentPolygon != null && lastWasLine) { //if there was only a point in the current one then finish it and go on
							//add make triangle
							Coordinate[] trc = {lastAdded.p0,lastAdded.p1,graph[branch][level-1],lastAdded.p0};
							Polygon triangle = geometryFactory.createPolygon(trc);

							currentPolygon = new DouglasPeuckerSimplifier(currentPolygon.union(triangle)).getResultGeometry();
							sub_polygons.add((Polygon)currentPolygon);
							currentPolygon = null;
							lastAdded = null;
						}
						
						lastWasLine = false;
						continue; 
					} else {
						lastWasLine = true;
						if (currentPosition == 0) continue;
					}
					
					
					LineSegment currentLine = new LineSegment(graph[branch][level-1],graph[branch][level]);
					//prior one will always be ReflectionPoint2D otherwise we won't be at this point
//					ReflectionPoint2D prior2Current = (ReflectionPoint2D)graph[branch][level-1];
//					
//					
//					//current one either ReflectionPoint or SoundPoint
//					if(graph[branch][level] instanceof SoundPoint2D) {
//						SoundPoint2D current = (SoundPoint2D)graph[branch][level];
//					} else {
//						ReflectionPoint2D current = (ReflectionPoint2D)graph[branch][level];
//					}
					
					//branch > 0
					if (graph[branch-1][level] == null) continue;
					
					if (currentPolygon == null) { //build the seed polygon to unionize
						// the following quadrolaterals on
						if (lastWasLine) {
							//create polygon with 4 points
							LineSegment priorLine = new LineSegment(graph[branch-1][level-1],graph[branch-1][level]);
							//discard intersecting line because subpolygons should not be self intersecting
							if (currentLine.intersection(priorLine) != null) continue;
							Coordinate[] coords = { currentLine.p0,currentLine.p1,priorLine.p1,priorLine.p0,currentLine.p0};
							//create convex hull from this
							//currentPolygon = new ConvexHull(coords,geometryFactory).getConvexHull();
							currentPolygon = geometryFactory.createPolygon(coords);
						} else {
							//create triangle
							Coordinate[] coords = {currentLine.p0, currentLine.p1,graph[branch-1][level-1]};
							currentPolygon = new ConvexHull(coords,geometryFactory).getConvexHull();
							
						}
						lastWasLine=true;
						lastAdded = currentLine;
						continue;
					} else {
						
						if (RobustCGAlgorithms.isCCW(currentPolygon.getCoordinates())) {
							currentPolygon = currentPolygon.reverse();
						}
						//we have already a starting polygon
						
						if (this.differentPolygons(currentLine, lastAdded)) { //look for direction changes
							//it intersects, then start new polygon
							sub_polygons.add((Polygon)currentPolygon);
							currentPolygon = null;
							lastAdded = null;
							lastWasLine = false;
							continue;
						} else {
							Coordinate[] c = {currentLine.p0, currentLine.p1,lastAdded.p1,lastAdded.p0,currentLine.p0};
							//Polygon poly = (Polygon)new ConvexHull(c,geometryFactory).getConvexHull();
							Polygon poly = geometryFactory.createPolygon(c);
							currentPolygon = new DouglasPeuckerSimplifier(currentPolygon.union(poly)).getResultGeometry();
							lastAdded = currentLine;
							continue;
						}
					}
				}
				if (currentPolygon != null) sub_polygons.add((Polygon)currentPolygon); // if finished loop add polygon
				
				if (sub_polygons.isEmpty()) break;
				//handle reformating
				//now all sub_polygons are cerated, merge them
				Polygon[] ps = new Polygon[sub_polygons.size()];
				ps = sub_polygons.toArray(ps);
				
//				if (level ==2) {
//					for (Polygon p : sub_polygons) {
//						
//						if (RobustCGAlgorithms.isCCW(p.getCoordinates())) {
//							sub_polygons.set(sub_polygons.indexOf(p), (Polygon)p.reverse());
//						}
//					}
//				}
				

				//TODO remove later now it is used to just list the subpolygons
				// or so called quadraleterals
				MultiPolygon mp = geometryFactory.createMultiPolygon(ps);
				if (!mp.isEmpty()) {
					soundAreas.add(mp);
				} else {
					continue;
				}
				
				//TODO debug this
//				Geometry geom = mp.union();
//				if (geom instanceof Polygon) { // if just one geometry create one polygon
//					Polygon[] arr = new Polygon[1];
//					arr[0] = (Polygon)geom;
//					soundAreas.add(geometryFactory.createMultiPolygon(arr));
//				}else {
//					soundAreas.add((MultiPolygon)geom);
//				}
			}
		}
		this.soundAreas.put(s, soundAreas);

	}
	private boolean differentPolygons(LineSegment currentLine, LineSegment lastAdded) {
		final double DIRECTION_TOLERANCE = Angle.toRadians(10.0); //10 degree tolerance
		final double MAXIMUM_DISTANCE = 3.0;
		boolean angleCondition = Angle.diff(currentLine.angle(), lastAdded.angle()) > DIRECTION_TOLERANCE;
		boolean distanceCondition = currentLine.distance(lastAdded) > 3.0;
		
		return angleCondition || distanceCondition;
	}
	
	private MultiPolygon unionPolygons(Polygon[] polys) {
		List<Polygon> mergedPolys = new ArrayList<Polygon>();
		List<Integer> merged = new ArrayList<Integer>();
		int lastAdded = -1;
		for (int i = 0; i < polys.length; i++) {
			if (!merged.contains(i)) {
				lastAdded++;
				mergedPolys.add(polys[i]);
				merged.add(i);
			} else {
				continue;
			}
			
			for (int j = i+1; j < polys.length; j++) {
				if (!merged.contains(j)) {
					if (!mergedPolys.get(lastAdded).disjoint(polys[j])) {
						mergedPolys.set(lastAdded, (Polygon)mergedPolys.get(lastAdded).union(polys[j]));
						merged.add(j);
					}
				} else {
					continue;
				}
			}
		}
		Polygon[] arr = new Polygon[mergedPolys.size()];
		return new GeometryFactory().createMultiPolygon(mergedPolys.toArray(arr));
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
