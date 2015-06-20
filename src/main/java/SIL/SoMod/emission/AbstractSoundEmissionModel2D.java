package SIL.SoMod.emission;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import SIL.SoMod.CalculationUtils;
import SIL.SoMod.attenuation.AttenuationModel;
import SIL.SoMod.environment.Environment;
import SIL.SoMod.environment.PropagationPathPoint;
import SIL.SoMod.environment.ReflectionPoint2D;
import SIL.SoMod.environment.SoundSource;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineSegment;
import com.vividsolutions.jts.geom.LineString;

public abstract class AbstractSoundEmissionModel2D implements SoundEmissionModel{
	protected List<SoundSource> sources;
	protected Environment environment;
	protected int bounce_level;
	protected HashMap<SoundSource,List<LineString>> propagationPaths;
	protected double audioThreshold;
	
	public abstract List<LineSegment> calculateInitialPropagationPaths(SoundSource s);
	
	public void propagate() {
		if (this.propagationPaths == null) {
			this.propagationPaths = new HashMap<SoundSource,List<LineString>>();
		} else {
			this.propagationPaths.clear();
		}
		
		for(SoundSource s : this.sources) {
			List<LineSegment> inits = this.calculateInitialPropagationPaths(s); //start with zero bounds
			List<LineString> paths = new ArrayList<LineString>();
			GeometryFactory factory = new GeometryFactory();
			
			//create LineString from initial LineSegments and trace them until threshold is reached
			for(LineSegment initLine : inits) {
				LineString initString = initLine.toGeometry(factory);
				paths.add(this.traceRay(initString));
			}
			
			/*
			for (int i = 1; i <= this.bounce_level; i++) {
				for (LineString path: paths) {
					ReflectionPoint2D lastIntersect = (ReflectionPoint2D) path.getEndPoint().getCoordinate();
					LineSegment incomingRay = lastIntersect.getIncoming();
					LineSegment untrimmed = CalculationUtils.reflect(incomingRay, this.environment.getMaximumSpan());
					LineSegment trimmed = CalculationUtils.trim(untrimmed, this.environment).get(0);
					ArrayList<Coordinate> coordinates = new ArrayList<Coordinate>(Arrays.asList(path.getCoordinates()));
					coordinates.add(trimmed.p1); //adds next intersection
					Coordinate[] c = new Coordinate[coordinates.size()];
					paths.set(paths.indexOf(path), factory.createLineString(coordinates.toArray(c)));
				}
			}
			*/
			this.propagationPaths.put(s, paths);
		}
	}
	
	private LineString traceRay(LineString initialRay) {
		SoundSource source = (SoundSource)initialRay.getStartPoint().getCoordinate();
		double dist2source = initialRay.getLength();
		((PropagationPathPoint)initialRay.getEndPoint().getCoordinate()).setDistanceFromSource(dist2source);
		GeometryFactory factory = new GeometryFactory();
		ArrayList<Coordinate> path = new ArrayList<Coordinate>();
		//dissolve LineString into its coordinates
		path.add(initialRay.getStartPoint().getCoordinate());
		path.add(initialRay.getEndPoint().getCoordinate());
		
		ReflectionPoint2D end = (ReflectionPoint2D) path.get(path.size()-1); //get last intersection
		//as long as the volume is higher than the threshold continue reflecting at the
		// walls
		int numberOfBounces = 1;
		while(end.getIncomingVolume() > this.getAudioThreshold()) {
			LineSegment incomingRay = end.getIncoming();
			ReflectionPoint2D intersection = (ReflectionPoint2D)incomingRay.p1;
			LineSegment reflectedRay = intersection.calculateOutgoing(this.environment);
//			if (reflectedRay.getLength() == 21.0 ||reflectedRay.getLength() == 12.0) break;
			double newVolume = CalculationUtils.calculateVolume(source,(PropagationPathPoint)reflectedRay.p0, (PropagationPathPoint)reflectedRay.p1);
			((ReflectionPoint2D)reflectedRay.p1).setIncomingVolume(newVolume);
			numberOfBounces++;
			if (newVolume < this.getAudioThreshold()) {
				//now calculate the endpoint that is not a ReflectionPoint2D,
				// but a simple SoundPoint2D
				Coordinate endp = AttenuationModel.calculateVolumeThreshold(reflectedRay.p0, 
						(ReflectionPoint2D)reflectedRay.p1, 
						this.audioThreshold);
				((PropagationPathPoint)endp).setDistanceFromSource(((ReflectionPoint2D)reflectedRay.p0).getDistanceFromSource()+((ReflectionPoint2D)reflectedRay.p1).distance(endp));
				path.add(endp);
				break; //end loop at this point
			} else {
				
				//if not endpoint, then get the next reflection point
				end = (ReflectionPoint2D) reflectedRay.p1;
				path.add(end);
			}
			
//			LineSegment untrimmed = CalculationUtils.reflect(incomingRay, this.environment.getMaximumSpan());
//			LineSegment trimmed = CalculationUtils.trim(untrimmed, this.environment);
			
			
		}
//		end = (ReflectionPoint2D) path.remove(path.size()-1);
		//after this the threshold is reached, now calculate coordinate at threshold
		System.out.println(numberOfBounces);
		
		
		
		Coordinate[] arr = path.toArray(new Coordinate[path.size()]);
		return factory.createLineString(arr);
	}
	
	public List<List<LineString>> getPropagationPaths() {
		if (this.propagationPaths == null || this.propagationPaths.isEmpty()) {
			this.propagate();
		}
		return new ArrayList<List<LineString>>(this.propagationPaths.values());
	}
	
	public List<LineString> getPropagationPaths(SoundSource s) {
		return this.propagationPaths.get(s);
	}
	
	public void addSoundSource(SoundSource s) {
		this.sources.add(s);
	}

	public void setEnvironment(Environment e) {
		this.environment = e;
	}
	public Environment getEnvironment() {
		return this.environment;
	}
	
	public List<SoundSource> getSources() {
		return sources;
	}

	public void setSources(List<SoundSource> sources) {
		this.sources = sources;
	}
	
	public void setBounceLevel(int level) {
		this.bounce_level = level;
	}

	public double getAudioThreshold() {
		return audioThreshold;
	}

	public void setAudioThreshold(double audioThreshold) {
		this.audioThreshold = audioThreshold;
	}
}
