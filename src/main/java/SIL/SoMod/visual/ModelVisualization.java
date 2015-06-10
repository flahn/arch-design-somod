package SIL.SoMod.visual;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Random;

import org.apache.batik.dom.svg.SVGDOMImplementation;
import org.apache.batik.ext.awt.RenderingHintsKeyExt;
import org.apache.batik.ext.awt.geom.Polygon2D;
import org.apache.batik.svggen.SVGGraphics2D;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Element;
import org.w3c.dom.svg.SVGDocument;

import SIL.SoMod.SoundModel;
import SIL.SoMod.environment.Environment;
import SIL.SoMod.environment.SoundSource;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.LineSegment;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;

public class ModelVisualization {
	private SoundModel model;
	private boolean cumulative;
	private boolean propagationPaths;
	private int bounce_lvl;
	private int propagationPathsForBounce;
	private boolean audibilityAreas;
	
	public ModelVisualization(SoundModel sm) {
		this.model = sm;
		this.cumulative = true;
		this.bounce_lvl = this.model.getBounceLevel();
		this.propagationPaths = false;
		this.propagationPathsForBounce = -1;
		this.audibilityAreas = false;
	}
	
	
	public void exportSVG(String filename) throws IOException {
		
		
	    DOMImplementation domImpl = SVGDOMImplementation.getDOMImplementation();

	    // Create an instance of org.w3c.dom.Document.
	    String svgNS = SVGDOMImplementation.SVG_NAMESPACE_URI;
	    SVGDocument document = (SVGDocument)domImpl.createDocument(svgNS, "svg", null);
	    
	    Environment environment = this.model.getEnvironment();
		Envelope e = environment.getBoundingBox();
	    int height = 500;
	    int width = 800;
	    double view_width = e.getWidth();
	    double view_height = e.getHeight();

	    SVGGraphics2D g = new SVGGraphics2D(document);
	    
	    AffineTransform flip = AffineTransform.getScaleInstance(1, -1);

	    flip.translate(0.0, -view_height);
	    g.transform(flip);
	    
	    if (this.audibilityAreas) {
	    	for (SoundSource s: this.model.getSources()) {
		    	MultiPolygon p = null;
	    		p = this.model.getBouncePolygon(this.bounce_lvl,this.cumulative, s);
	    		if (p == null) {
	    			continue;
	    		}

			    SVGGraphics2D bounceArea = new SVGGraphics2D(g);
			    Random rand = new Random();
		        float r = rand.nextFloat();
		        float gr = rand.nextFloat();
		        float b = rand.nextFloat();
			    bounceArea.setColor(new Color(r,gr,b));
			    bounceArea.setStroke(new BasicStroke(0.2f));
			    bounceArea.setBackground(new Color(r,gr,b));
			    this.drawMultiPolygon(bounceArea, p);
		    }
	    }
	    
	    
	    if (this.propagationPaths) {
	    	//PropagationPaths are enabled
	    	SVGGraphics2D propagationPaths = new SVGGraphics2D(g);
		    propagationPaths.setStroke(new BasicStroke(0.02f));
		    
		    if (this.propagationPathsForBounce < 0) {
		    	this.drawPropagationPaths(propagationPaths); //draw complete path
		    } else {
		    	this.drawLineSegments(propagationPaths, this.model.getBounceLineSegments(this.propagationPathsForBounce, this.model.getSources().get(0)));
		    }
	    }
	    
	    //Draw envionment and sources every time
	    //Environment
	    SVGGraphics2D environmentElements = new SVGGraphics2D(g);
	    environmentElements.setStroke(new BasicStroke(0.05f));
		this.drawEnvironment(environmentElements);
        
        //Sources
        SVGGraphics2D sources = new SVGGraphics2D(g);
        sources.setColor(new Color(0,128,128));
        this.drawSources(sources);
        
        Element root = g.getRoot();
        root.setAttributeNS(null, "viewBox", "0 0 "+ view_width +" "+view_height);
	    root.setAttributeNS(null, "width", Integer.toString(width));
	    root.setAttributeNS(null, "height", Integer.toString(height));
        
	    File output = new File(filename);
	    output.getParentFile().mkdir();
	    
        PrintWriter savefile = new PrintWriter(new FileWriter(output));
        g.stream(root,savefile, true,false);
        savefile.close(); 
	}
	
	private void drawSources(SVGGraphics2D element) {
		for (SoundSource s : this.model.getSources()) {
        	this.drawPoint(element, s, 0.2);
        }
	}
	
	private void drawEnvironment(SVGGraphics2D element) {
		List<? extends LineSegment> walls = this.model.getEnvironment().getReflectionSegments();
		this.drawLineSegments(element, walls);
	}
	
	private void drawPropagationPaths(SVGGraphics2D element) {
		for (SoundSource s : this.model.getSources()) {
	    	Random rand = new Random();
	        float r = rand.nextFloat();
	        float gr = rand.nextFloat();
	        float b = rand.nextFloat();
		    
		    element.setColor(new Color(r,gr,b));
			this.drawLineString(element, this.model.getPropagationPaths(s));
	    }
	}
	
	private void drawPoint(SVGGraphics2D g, Coordinate c,double radius) {
		Ellipse2D.Double point = new Ellipse2D.Double();
		point.setFrame(c.x-radius, c.y-radius, 2*radius, 2*radius);
		g.fill(point);
	}
	
	private void drawLineSegments(SVGGraphics2D g, List<? extends LineSegment> list) {
		for(LineSegment rs : list) {
			g.draw(new Line2D.Double(rs.p0.x, rs.p0.y, rs.p1.x, rs.p1.y));
		}
	}
	
	private void drawLineString(SVGGraphics2D g, List<LineString> list) {
		
		for(LineString path : list) {
			Coordinate[] coords = path.getCoordinates();
			for(int i = 0; i < coords.length-1; i++) {
				g.draw(new Line2D.Double(coords[i].x, coords[i].y, coords[i+1].x, coords[i+1].y));
			}
		}
	}
	
	private void drawMultiPolygon(SVGGraphics2D g, MultiPolygon mp){
		 for(int i = 0; i < mp.getNumGeometries(); i++) {
			 Polygon p = (Polygon)mp.getGeometryN(i);
			 this.drawPolygon(g, p);
		 }
	}
	
	private void drawPolygon(SVGGraphics2D g, Polygon p) {
		Polygon2D pnew = new Polygon2D();
		Coordinate[] coords = p.getCoordinates();
		for (int i = 0; i < coords.length; i++) {
			pnew.addPoint((float)coords[i].x, (float)coords[i].y);
		}
		g.fill(pnew);
	}
	
	
	public void visualizePaths(boolean prop) {
		this.propagationPaths = prop;
	}
	
	public void visualizeAudibleArea(boolean area) {
		this.audibilityAreas = area;
		this.bounce_lvl = -1;
		this.cumulative = true;
	}
	
	public void visualizeArea(boolean area, boolean cumulative, int bounce_lvl) {
		this.audibilityAreas = area;
		if(this.audibilityAreas) {
			if (bounce_lvl <= this.model.getBounceLevel()) {
				this.cumulative = cumulative;
				if (! this.cumulative) {
					this.bounce_lvl = bounce_lvl;
				} else {
					this.bounce_lvl = this.model.getBounceLevel();
				}
			} else {
				
			}
		}
	}
	
	public void visualizePathsForBounce(int bounce){
		this.propagationPathsForBounce = bounce;
	}
}
