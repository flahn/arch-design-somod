package SIL.SoMod;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import SIL.SoMod.emission.EqualRayCasting2D;
import SIL.SoMod.emission.PreciseBoundary2D;
import SIL.SoMod.emission.SoundEmissionModel;
import SIL.SoMod.environment.Environment;
import SIL.SoMod.environment.ReflectionPoint2D;
import SIL.SoMod.environment.ReflectionSegment;
import SIL.SoMod.environment.SoundPoint2D;
import SIL.SoMod.environment.SoundSource;

import com.vividsolutions.jts.algorithm.Angle;
import com.vividsolutions.jts.algorithm.ConvexHull;
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
			this.emissionModel.bounce();
			this.propagationPaths = this.emissionModel.getPropagationPaths();
			this.createCoordinatesTable();
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
		if (step >= areas.size()) {
			throw new ArrayIndexOutOfBoundsException("The amount of steps exceeds the maximum calculated bounces by threshold.");
		} else {
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
				for (int bounce = 0; bounce+1 <= maxDeep; bounce++) {
					LineString p = paths.get(path);
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
						if (currentPolygon != null) { //if there was only a point in the current one then finish it and go on
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
					ReflectionPoint2D prior2Current = (ReflectionPoint2D)graph[branch][level-1];
					
					
					//current one either ReflectionPoint or SoundPoint
					if(graph[branch][level] instanceof SoundPoint2D) {
						SoundPoint2D current = (SoundPoint2D)graph[branch][level];
					} else {
						ReflectionPoint2D current = (ReflectionPoint2D)graph[branch][level];
					}
					
					//branch > 0
					if (graph[branch-1][level] == null) continue;
					
					if (currentPolygon == null) {
						//create the start polygon
						if (lastWasLine) {
							//create polygon with 4 points
							LineSegment priorLine = new LineSegment(graph[branch-1][level-1],graph[branch-1][level]);
							//discard intersecting line because subpolygons should not be self intersecting
							if (currentLine.intersection(priorLine) != null) continue;
							Coordinate[] coords = { currentLine.p1,currentLine.p0,priorLine.p0,priorLine.p1};
							//create convex hull from this
							currentPolygon = new ConvexHull(coords,geometryFactory).getConvexHull();
							
						} else {
							//create triangle
							Coordinate[] coords = {currentLine.p0, currentLine.p1,graph[branch-1][level-1]};
							currentPolygon = new ConvexHull(coords,geometryFactory).getConvexHull();
							
						}
						lastWasLine=true;
						lastAdded = currentLine;
						continue;
					} else {
						//we have already a starting polygon
						final double DIRECTION_TOLERANCE = Angle.toRadians(30.0); //2 degree tolerance
						if (Angle.diff(currentLine.angle(), lastAdded.angle()) > DIRECTION_TOLERANCE) { //look for direction changes
							//it intersects, then start new polygon
							sub_polygons.add((Polygon)currentPolygon);
							currentPolygon = null;
							lastAdded = null;
							lastWasLine = false;
							continue;
						} else {
							Coordinate[] c = {currentLine.p1, currentLine.p0,lastAdded.p0,lastAdded.p1};
							Polygon poly = (Polygon)new ConvexHull(c,geometryFactory).getConvexHull();
							currentPolygon = new DouglasPeuckerSimplifier(currentPolygon.union(poly)).getResultGeometry();
							lastAdded = currentLine;
							continue;
						}
					}
				}
				if (currentPolygon != null) sub_polygons.add((Polygon)currentPolygon); // if finished loop add polygon
				
				//handle reformating
				//now all sub_polygons are cerated, merge them
				Polygon[] ps = new Polygon[sub_polygons.size()];
				ps = sub_polygons.toArray(ps);

				MultiPolygon mp = geometryFactory.createMultiPolygon(ps);
				Geometry geom = mp.union();
				if (!geom.isEmpty()) {
					if (geom instanceof Polygon) {
						Polygon[] arr = new Polygon[1];
						arr[0] = (Polygon)geom;
						soundAreas.add(geometryFactory.createMultiPolygon(arr));
					}else {
						soundAreas.add((MultiPolygon)geom);
					}
				} else {
					continue;
				}
			}
		}
		this.soundAreas.put(s, soundAreas);

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
