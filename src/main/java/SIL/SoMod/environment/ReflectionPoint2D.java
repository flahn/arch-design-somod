package SIL.SoMod.environment;

import SIL.SoMod.CalculationUtils;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.LineSegment;

public class ReflectionPoint2D extends PropagationPathPoint{
	protected ReflectionSegment surface;
	protected LineSegment incoming;
	protected LineSegment outgoing;
	protected Coordinate source_image;
	
	
	public ReflectionPoint2D(ReflectionSegment w, LineSegment in) {
		super();
		this.surface = w;
		this.setIncoming(in);
	}
	
	public void setIncoming(LineSegment in) {
		this.incoming = in;
		
		//the end point of the incoming ray is the intersection (this point)
		this.x = this.incoming.p1.x;
		this.y = this.incoming.p1.y;
		//put this at the endpoint
		this.incoming.p1 = this;
	}
	
	public LineSegment getIncoming() {
		return this.incoming;
	}
	
	public void setReflector(ReflectionSegment re) {
		this.surface = re;
	}
	
	public ReflectionSegment getReflector() {
		return this.surface;
	}
	
	public double getReflectionCoefficient() {
		return this.surface.getReflectionCoefficient();
	}
	
	public LineSegment calculateOutgoing(Environment e) {
		if(this.outgoing == null) {
			Coordinate source = this.incoming.p0;
			this.source_image = CalculationUtils.mirror(source, this.surface);
			double maxDistance = e.getMaximumSpan();
			LineSegment tempOutRay = CalculationUtils.reflect(this.incoming, maxDistance);
			//replace coordinate with this one
			tempOutRay.p0 = this; //this ray starts at this point and is directed in the reflection direction
			
			this.outgoing = CalculationUtils.trim(tempOutRay, e);
			
		}
		
		return this.outgoing;
		
	}
	
	@Override
	public void setIncomingVolume(double incomingVolume) {
		this.incomingVolume = incomingVolume;
		this.outgoingVolume = this.incomingVolume * this.surface.getReflectionCoefficient();
	}
	
	@Override
	public double getOutgoingVolume() {
		return this.outgoingVolume;
	}
}
