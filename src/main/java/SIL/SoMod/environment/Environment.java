package SIL.SoMod.environment;

import java.awt.geom.Line2D;
import java.util.ArrayList;
import java.util.List;

import com.vividsolutions.jts.geom.Envelope;

public class Environment {
	private List<ReflectionSegment> reflection_elements;
	private Envelope bbox;
	
	public Environment() {
		this.reflection_elements = new ArrayList<ReflectionSegment>();
	}
	
	public List<ReflectionSegment> getReflectionSegments() {
		return this.reflection_elements;
	}
	
	public boolean setReflectionSegments(List<ReflectionSegment> list) {
		this.reflection_elements = list;
		return true;
	}
	
	public boolean addReflectionSegment(ReflectionSegment w) {
		return this.reflection_elements.add(w);
	}
	
	public Envelope getBoundingBox() {
		return this.bbox;
	}
	
	public void setBoundingBox(Envelope e) {
		this.bbox = e;
	}
	
	public double getMaximumSpan() {
		return Math.sqrt(Math.pow(this.getBoundingBox().getHeight(),2) + Math.pow(this.getBoundingBox().getWidth(),2));
	}
}
