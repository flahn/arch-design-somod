package SIL.SoMod.emission;

import java.util.ArrayList;
import java.util.List;

import SIL.SoMod.environment.ReflectionSegment;
import SIL.SoMod.environment.SoundSource;

import com.vividsolutions.jts.algorithm.Angle;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.LineSegment;

public class PreciseBoundary2D extends AbstractSoundEmissionModel2D{
	private class SortableVertex implements Comparable<SortableVertex> {
		private Coordinate coordinate;
		private ReflectionSegment segment;
		private double angle;
		
		public SortableVertex (Coordinate v, Coordinate vertex, ReflectionSegment rs) {
			this.coordinate = vertex;
			this.segment = rs;
			this.angle = Angle.angle(v, vertex);
		}
		
		public double getAngle() {
			return 0;
		}
		
		public int compareTo(SortableVertex o) {
			return 0;
		}
		
	}
	
	@Override
	public List<LineSegment> calculateInitialPropagationPaths(SoundSource s) {
		List<ReflectionSegment> walls = this.environment.getReflectionSegments();
		List<Coordinate> vertices = new ArrayList<Coordinate>();
		for(ReflectionSegment wall : walls) {
			
		}
		
		// TODO return the paths to the vertices of the environment
		return null;
	}

	public void setSoundSource(SoundSource s) {
		// TODO Auto-generated method stub
		
	}
	
	/*
	private List<LineSegment> calculateVisibileBounds() {
		List<LineSegment> boundaries = new ArrayList<LineSegment>();
		double[] hRange = this.source.getHorizontalRange();
		List<Coordinate> visibleVertices = new ArrayList<Coordinate>();
		List<Coordinate> invisibleVertices = new ArrayList<Coordinate>();
		//hRange[0] left border from the source orientation
		//hRange[1] the right part
		for (ReflectionSegment rs : this.environment.getReflectionSegments()) {
			Coordinate start = rs.p0;
			Coordinate end = rs.p1;
			if (!visibleVertices.contains(start) && !invisibleVertices.contains(start)) {
				LineSegment ls = new LineSegment(this.source,start);
				
				double line2src = Angle.diff(ls.angle(), this.source.getOrientation());
				double halfHorizontalFOV = Angle.diff(hRange[0], this.source.getOrientation());
				if (line2src <= halfHorizontalFOV) {
					//check if line 'ls' intersects with another line of environment (is invisible)
					int intersects = 0;
					List<ReflectionPoint2D> rps = new ArrayList<ReflectionPoint2D>();
					for (ReflectionSegment other : this.environment.getReflectionSegments()) {
						if (other == rs) continue;
						rps.clear();
						Coordinate intersection = ls.intersection(other);
						if (intersection != null) {
							intersects++;
							rps.add(new ReflectionPoint2D(other, ls));
						}
						
					}
					if (rps.size() == 0) {
						//directly visible --> add
						visibleVertices.add(start);
					} else if (rps.size() == 1){
						//either this one is completly invisible or it is blocked
						//by a transmissive element
						//TODO check for transmission
						//for now we consider this point blocked and do nothing
					} else {
						//if there is an intersection then check the border points
						//for intersection of their prolongation with one of the
						//adjacent edges
						ReflectionPoint2D farthest = rps.get(0);
						double farthestDist = farthest.getIncoming().getLength();
						for (ReflectionPoint2D intersector : rps) {
							if (intersector.getIncoming().getLength() > farthestDist) {
								farthest = intersector;
								farthestDist = farthest.getIncoming().getLength();
							}
						}
						//take the last edge that blocks visibility from the source
						ReflectionSegment element = farthest.getReflector();
						if (visibleVertices.contains(element.p0)) {
							//check if prolongation intersects with 'rs'
							//if it does add the vertex to the visible list
							//else discard it
						} 
						if (invisibleVertices.contains(element.p0)) {} //do nothing because it is invisible
						if (visibleVertices.contains(element.p1)){
							//check if prolongation intersects with 'rs'
							//if it does add the vertex to the visible list
							//else discard it
						} 
						if (invisibleVertices.contains(element.p1)) {} //do nothing because invisible
						
						
					}
					
				} else { //coordinate out of fov from source
					invisibleVertices.add(start);
				}
				
				
			}
			if (!visibleVertices.contains(end)) {
				LineSegment ls = new LineSegment(this.source,end);
			}
		}
	}
	*/
}
