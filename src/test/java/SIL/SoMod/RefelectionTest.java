package SIL.SoMod;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;
import SIL.SoMod.environment.Environment;
import SIL.SoMod.environment.ReflectionSegment;
import SIL.SoMod.environment.SoundSource;

import com.vividsolutions.jts.algorithm.Angle;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.LineSegment;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;

public class RefelectionTest extends TestCase {
	Environment environment;
	SoundSource source;

	protected void setUp() throws Exception {
		super.setUp();
		//Corners
    	List<Coordinate> clist = new ArrayList<Coordinate>();
    	clist.add(new Coordinate(0.0,0.0));
    	clist.add(new Coordinate(clist.get(clist.size()-1).x+12.0,clist.get(clist.size()-1).y));
    	clist.add(new Coordinate(clist.get(clist.size()-1).x,clist.get(clist.size()-1).y+5.0));
    	clist.add(new Coordinate(clist.get(clist.size()-1).x+3.0, clist.get(clist.size()-1).y));
    	clist.add(new Coordinate(clist.get(clist.size()-1).x, clist.get(clist.size()-1).y-3.0));
    	clist.add(new Coordinate(clist.get(clist.size()-1).x+6.0, clist.get(clist.size()-1).y));
    	clist.add(new Coordinate(clist.get(clist.size()-1).x, clist.get(clist.size()-1).y+10.0));
    	clist.add(new Coordinate(clist.get(clist.size()-1).x-6.0, clist.get(clist.size()-1).y));
    	clist.add(new Coordinate(clist.get(clist.size()-1).x, clist.get(clist.size()-1).y-5.0));
    	clist.add(new Coordinate(clist.get(clist.size()-1).x-3.0, clist.get(clist.size()-1).y));
    	clist.add(new Coordinate(clist.get(clist.size()-1).x, clist.get(clist.size()-1).y+5.0));
    	clist.add(new Coordinate(clist.get(clist.size()-1).x-12.0, clist.get(clist.size()-1).y));
    	
    	Envelope env = new Envelope();
    	for(Coordinate c : clist) {
    		env.expandToInclude(c);
    	}
    	
    	//Wall segments
    	List<ReflectionSegment> walls = new ArrayList<ReflectionSegment>();
    	for(int i = 0; i < clist.size(); i++) {
    		walls.add(new ReflectionSegment(1.0,clist.get(i), clist.get((i+1)%clist.size())));
    	}
    	
    	Environment e = new Environment();
    	e.setReflectionSegments(walls);
    	e.setBoundingBox(env);
    	
    	this.environment = e;
    	
    	SoundSource src = new SoundSource(9.0,5.5,Angle.toRadians(180),Angle.toRadians(360),0,180);
    	this.source = src;
	}
	
	public void testReflection() {
		WKTReader wr = new WKTReader();
		try {
			LineString line = (LineString) wr.read("LINESTRING( 9.0 5.5, 5.247223250267434 12.0)");
			LineString wall = (LineString) wr.read("LINESTRING( 10.0 12.0, 0.0 12.0)");
			LineString wall2 = (LineString) wr.read("LINESTRING( 0.0 12.0, 0.0 0.0)");
			Coordinate[] cs = line.getCoordinates();
			Coordinate[] wcs = wall.getCoordinates();
			Coordinate[] wcs2 = wall2.getCoordinates();
			LineSegment ls = new LineSegment(cs[0],cs[1]);
			LineSegment ws = new LineSegment(wcs[0],wcs[1]);
			LineSegment ws2 = new LineSegment(wcs2[0],wcs2[1]);
			//System.out.println(CalculationUtils.orientation(new Coordinate(5,11), ls));
			//System.out.println(CalculationUtils.orientation(new Coordinate(6,13), ls));
			Coordinate image1 = CalculationUtils.mirror(source, ws);
			System.out.println(image1);
			System.out.println(CalculationUtils.mirror(source, ws2));
			//find the reflecting wall
			
			
			
			assertTrue(cs[0].x == 9.0 && cs[0].y == 5.5);
		} catch (ParseException e) {
			assertTrue(false);
			e.printStackTrace();
		}
	}
}
