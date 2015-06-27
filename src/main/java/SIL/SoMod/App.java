package SIL.SoMod;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import SIL.SoMod.SoundModel.EmissionModel;
import SIL.SoMod.environment.Environment;
import SIL.SoMod.environment.ReflectionSegment;
import SIL.SoMod.environment.SoundSource;
import SIL.SoMod.visual.ModelVisualization;

import com.vividsolutions.jts.algorithm.Angle;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.LineString;



public class App 
{
    public static void main( String[] args ) throws IOException
    {
    	int max_bounce_lvl = 3;
    	int visual_bounce_area = 0;
    	double threshold = 32.0; // the perceived dB threshold we are interested in
    	
    	
    	int bounce_lvl = 3;
    	boolean cumulative = true;
    	/********************************************
    	 ********************************************
    	 ** Environment 1
    	 ********************************************
    	 ********************************************/
//    	SoundModel model1 = App.createModel1(bounce_lvl);
//
//    	model1.setVolumeThreshold(threshold);
//
//    	
//    	String dir_e1 = "./img/environment1/";
//    	
//    	App.performAudioThresholdModel(model1, threshold, cumulative,dir_e1);
    	
    	
    	/********************************************
    	 ********************************************
    	 ** Environment 2	
    	 ********************************************
    	 ********************************************/
//    	SoundModel model2 = App.createModel1(bounce_lvl);

//    	
//    	String dir_e2 = "./img/environment2/";
//    	
//    	
//    	App.performBounceLevelModel(model2, bounce_lvl, 
//    			dir_e2);
    	
    	
    	/********************************************
    	 ********************************************
    	 ** Library Model	
    	 ********************************************
    	 ********************************************/
    	SoundModel libMod = App.libraryModel(bounce_lvl);

    	libMod.setVolumeThreshold(threshold);

    	
    	String dir_lib = "./img/library/";
    	
    	
//    	App.performBounceLevelModel(model1, bounce_lvl, 
//    			cumulative,dir_e1);
//    	App.performAudioThresholdModel(libMod, threshold, cumulative,dir_lib);
    	
    	dir_lib += (!dir_lib.endsWith("/")) ? "/" : "";
    	
    	libMod.setVolumeThreshold(threshold);
    	String file_name = dir_lib+"threshold-"+threshold;
    	file_name += (cumulative)? "-cum.svg" : ".svg";
    	
    	libMod.run();
    	
    	ModelVisualization mv = new ModelVisualization(libMod,cumulative,1600,1000);
    	mv.visualizeArea(false, cumulative, -1);
    	mv.visualizePaths(true);
    	mv.exportSVG(file_name);
    }
    
    public static void performAudioThresholdModel(SoundModel model, double threshold, boolean cumulative, String dir) throws IOException {
    	dir += (!dir.endsWith("/")) ? "/" : "";
    	model.setVolumeThreshold(threshold);
    	String file_name = dir+"threshold-"+threshold;
    	file_name += (cumulative)? "-cum.svg" : ".svg";
    	
    	model.run();
    	
    	//handle the visualization of the model
    	ModelVisualization mv = new ModelVisualization(model, cumulative);
    	mv.visualizeAudibleArea(false);
    	mv.visualizePaths(true);
    	mv.exportSVG(file_name);
    }
    
    public static void performBounceLevelModel(SoundModel model, int bounces, boolean cumulative,String dir) throws IOException {
    	dir += (!dir.endsWith("/")) ? "/" : "";
    	
    	String file_name = dir+"bounces"+bounces;
    	file_name += (cumulative)? "-cum.svg" : ".svg";
    	
    	model.run();
    	
    	ModelVisualization mv = new ModelVisualization(model,cumulative);
    	mv.visualizeArea(true, cumulative, bounces);
    	mv.visualizePaths(false);
    	mv.exportSVG(file_name);
    }

    
    public static SoundModel createModel1(int bounces) {
    	SoundModel model = new SoundModel(EmissionModel.EQUAL_RAY_TRACING_2D,bounces);
    	
    	//modeling sound source as being just in the horizontal plane
    	SoundSource src1 = new SoundSource(9.0,5.5,
    			Angle.toRadians(0),
    			Angle.toRadians(360),
    			0,300);
    	src1.setVolume(60.0);
    	SoundSource src2 = new SoundSource(18.0,9.0,
    			Angle.toRadians(220),
    			Angle.toRadians(60),
    			0,80);
    	src2.setVolume(60.0);
    	SoundSource src3 = new SoundSource(20.0,10.5,
    			Angle.toRadians(60),
    			Angle.toRadians(40),
    			0,2);
    	model.addSource(src1);
//    	model.addSource(src2);
//    	model.addSource(src3);

    	Environment e = buildTestEnvironment1();
    	model.setEnvironment(e);
    	
    	return model;
    }
    
    public static SoundModel createModel2(int bounces) {
    	SoundModel model = new SoundModel(EmissionModel.EQUAL_RAY_TRACING_2D,bounces);
    	
    	//modeling sound source as being just in the horizontal plane
    	SoundSource src = new SoundSource(2.0,3.5,
    			Angle.toRadians(31),
    			Angle.toRadians(70),
    			0,100);

    	model.addSource(src);

    	Environment e = buildTestEnvironment2();
    	model.setEnvironment(e);
    	
    	return model;
    }
    
    public static SoundModel libraryModel(int bounces) {
    	SoundModel model = new SoundModel(EmissionModel.EQUAL_RAY_TRACING_2D,bounces);
    	
    	//modeling sound source as being just in the horizontal plane
    	SoundSource src1 = new SoundSource(5.5,13.66,
    			Angle.toRadians(0),
    			Angle.toRadians(360),
    			0,300);
    	src1.setVolume(59.0);
    	model.addSource(src1);
    	
    	SoundSource src2 = new SoundSource(29,11,
    			Angle.toRadians(0),
    			Angle.toRadians(360),
    			0,300);
    	src2.setVolume(59.0);
    	model.addSource(src2);
    	
    	SoundSource src3 = new SoundSource(46,25,
    			Angle.toRadians(0),
    			Angle.toRadians(360),
    			0,300);
    	src3.setVolume(59.0);
    	model.addSource(src3);
    	
    	SoundSource receptionist = new SoundSource(13,28,
    			Angle.toRadians(0),
    			Angle.toRadians(360),
    			0,300);
    	receptionist.setVolume(58.0);
    	model.addSource(receptionist);

    	Environment e = buildLibrary();
    	model.setEnvironment(e);
    	
    	return model;
    }
    
    public static Environment buildTestEnvironment1() {
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
    	double globalEnvironmentReflectionFactor = 0.95;
    	for(int i = 0; i < clist.size(); i++) {
    		walls.add(new ReflectionSegment(globalEnvironmentReflectionFactor,
    				clist.get(i), clist.get((i+1)%clist.size())));
    	}
    	
    	//optional wall in big room
//    	walls.add(new ReflectionSegment(1.0,
//    			new Coordinate(2.5,9.0), new Coordinate(9.5,9.0)));
    	
    	Environment e = new Environment();
    	e.setReflectionSegments(walls);
    	e.setBoundingBox(env);
    	return e;
    }
    
    public static Environment buildTestEnvironment2() {
    	//Corners
    	List<Coordinate> clist = new ArrayList<Coordinate>();
    	clist.add(new Coordinate(0.0,0.0));
    	clist.add(new Coordinate(clist.get(clist.size()-1).x+15.0,clist.get(clist.size()-1).y));
    	clist.add(new Coordinate(clist.get(clist.size()-1).x,clist.get(clist.size()-1).y+5.0));
    	clist.add(new Coordinate(clist.get(clist.size()-1).x-9.0,clist.get(clist.size()-1).y));
    	clist.add(new Coordinate(clist.get(clist.size()-1).x,clist.get(clist.size()-1).y+20.0));
    	clist.add(new Coordinate(clist.get(clist.size()-1).x-6.0,clist.get(clist.size()-1).y));
    	
    	Envelope env = new Envelope();
    	for(Coordinate c : clist) {
    		env.expandToInclude(c);
    	}
    	
    	//Wall segments
    	double globalEnvironmentReflectionFactor = 1.0;
    	List<ReflectionSegment> walls = new ArrayList<ReflectionSegment>();
    	for(int i = 0; i < clist.size(); i++) {
    		walls.add(new ReflectionSegment(globalEnvironmentReflectionFactor,
    				clist.get(i), clist.get((i+1)%clist.size())));
    	}
    	
    	Environment e = new Environment();
    	e.setReflectionSegments(walls);
    	e.setBoundingBox(env);
    	return e;
    }
    
    public static Environment buildLibrary() {
    	List<Coordinate> clist = new ArrayList<Coordinate>();
    	clist.add(new Coordinate(0,0));
    	clist.add(new Coordinate(7,0));
    	clist.add(new Coordinate(14,0));
    	clist.add(new Coordinate(21,0));
    	clist.add(new Coordinate(28,0));
    	clist.add(new Coordinate(31.2789,9));
    	clist.add(new Coordinate(21,9));
    	clist.add(new Coordinate(14,9));
    	clist.add(new Coordinate(7,9));
    	clist.add(new Coordinate(0,9));
    	clist.add(new Coordinate(48,0));
    	clist.add(new Coordinate(48,20));
    	clist.add(new Coordinate(33.34,14.66));
    	clist.add(new Coordinate(48,27));
    	clist.add(new Coordinate(43.5,27));
    	clist.add(new Coordinate(39,27));
    	clist.add(new Coordinate(37,27));
    	clist.add(new Coordinate(34,27));
    	clist.add(new Coordinate(31,27));
    	clist.add(new Coordinate(28,27));
    	clist.add(new Coordinate(25,27));
    	clist.add(new Coordinate(22,27));
    	clist.add(new Coordinate(22,30));
    	clist.add(new Coordinate(0,30));
    	clist.add(new Coordinate(25,30));
    	clist.add(new Coordinate(28,30));
    	clist.add(new Coordinate(31,30));
    	clist.add(new Coordinate(34,30));
    	clist.add(new Coordinate(37,30));
    	clist.add(new Coordinate(39,30));
    	clist.add(new Coordinate(40.5,30));
    	clist.add(new Coordinate(42,30));
    	clist.add(new Coordinate(43.5,30));
    	clist.add(new Coordinate(45,30));
    	clist.add(new Coordinate(46.5,30));
    	clist.add(new Coordinate(48,30));
    	clist.add(new Coordinate(48,28.5));
    	clist.add(new Coordinate(46.5,28.5));
    	clist.add(new Coordinate(45,28.5));
    	clist.add(new Coordinate(43.5,28.5));
    	clist.add(new Coordinate(42,28.5));
    	clist.add(new Coordinate(40.5,28.5));
    	clist.add(new Coordinate(39,28.5));
    	clist.add(new Coordinate(37,28.5));
    	
    	Envelope env = new Envelope();
    	for(Coordinate c : clist) {
    		env.expandToInclude(c);
    	}
    	
    	//make the walls
    	final double ROOM_RF = 0.85;
    	final double BATH_RF = 0.95;
    	final double SHELVE_RF = 0.65;
    	List<ReflectionSegment> walls = new ArrayList<ReflectionSegment>();
    	//bottom offices
    	walls.add(new ReflectionSegment(ROOM_RF,clist.get(0),clist.get(1)));
    	walls.add(new ReflectionSegment(ROOM_RF,clist.get(1),clist.get(2)));
    	walls.add(new ReflectionSegment(ROOM_RF,clist.get(2),clist.get(3)));
    	walls.add(new ReflectionSegment(ROOM_RF,clist.get(3),clist.get(4)));
    	walls.add(new ReflectionSegment(ROOM_RF,clist.get(4),clist.get(5)));
    	walls.add(new ReflectionSegment(ROOM_RF,clist.get(5),clist.get(6)));
    	walls.add(new ReflectionSegment(ROOM_RF,clist.get(6),clist.get(3)));
    	walls.add(new ReflectionSegment(ROOM_RF,clist.get(6),clist.get(7)));
    	walls.add(new ReflectionSegment(ROOM_RF,clist.get(7),clist.get(8)));
    	walls.add(new ReflectionSegment(ROOM_RF,clist.get(7),clist.get(2)));
    	walls.add(new ReflectionSegment(ROOM_RF,clist.get(8),clist.get(9)));
    	walls.add(new ReflectionSegment(ROOM_RF,clist.get(8),clist.get(1)));
    	walls.add(new ReflectionSegment(ROOM_RF,clist.get(9),clist.get(0)));
    	
    	//lecture room
    	walls.add(new ReflectionSegment(ROOM_RF,clist.get(4),clist.get(10)));
    	walls.add(new ReflectionSegment(ROOM_RF,clist.get(10),clist.get(11)));
    	walls.add(new ReflectionSegment(ROOM_RF,clist.get(11),clist.get(12)));
    	walls.add(new ReflectionSegment(ROOM_RF,clist.get(12),clist.get(5)));
    	
    	//main room
    	walls.add(new ReflectionSegment(ROOM_RF,clist.get(11),clist.get(13)));
    	walls.add(new ReflectionSegment(ROOM_RF,clist.get(13),clist.get(14)));
    	walls.add(new ReflectionSegment(ROOM_RF,clist.get(14),clist.get(15)));
    	walls.add(new ReflectionSegment(ROOM_RF,clist.get(15),clist.get(16)));
    	walls.add(new ReflectionSegment(ROOM_RF,clist.get(16),clist.get(17)));
    	walls.add(new ReflectionSegment(ROOM_RF,clist.get(17),clist.get(18)));
    	walls.add(new ReflectionSegment(ROOM_RF,clist.get(18),clist.get(19)));
    	walls.add(new ReflectionSegment(ROOM_RF,clist.get(19),clist.get(20)));
    	walls.add(new ReflectionSegment(ROOM_RF,clist.get(20),clist.get(21)));
    	walls.add(new ReflectionSegment(ROOM_RF,clist.get(21),clist.get(22)));
    	walls.add(new ReflectionSegment(ROOM_RF,clist.get(22),clist.get(23)));
    	walls.add(new ReflectionSegment(ROOM_RF,clist.get(23),clist.get(9)));
    	
    	//single rooms
    	walls.add(new ReflectionSegment(ROOM_RF,clist.get(22),clist.get(24)));
    	walls.add(new ReflectionSegment(ROOM_RF,clist.get(24),clist.get(25)));
    	walls.add(new ReflectionSegment(ROOM_RF,clist.get(24),clist.get(20)));
    	walls.add(new ReflectionSegment(ROOM_RF,clist.get(25),clist.get(19)));
    	walls.add(new ReflectionSegment(ROOM_RF,clist.get(25),clist.get(26)));
    	walls.add(new ReflectionSegment(ROOM_RF,clist.get(26),clist.get(18)));
    	walls.add(new ReflectionSegment(ROOM_RF,clist.get(26),clist.get(27)));
    	walls.add(new ReflectionSegment(ROOM_RF,clist.get(27),clist.get(17)));
    	walls.add(new ReflectionSegment(ROOM_RF,clist.get(27),clist.get(28)));
    	walls.add(new ReflectionSegment(ROOM_RF,clist.get(28),clist.get(43)));
    	walls.add(new ReflectionSegment(ROOM_RF,clist.get(43),clist.get(16)));
    	
    	//utility room
    	walls.add(new ReflectionSegment(ROOM_RF,clist.get(28),clist.get(29)));
    	walls.add(new ReflectionSegment(ROOM_RF,clist.get(29),clist.get(42)));
    	walls.add(new ReflectionSegment(ROOM_RF,clist.get(42),clist.get(43)));
    	
    	//toilet
    	walls.add(new ReflectionSegment(BATH_RF,clist.get(29),clist.get(30)));
    	walls.add(new ReflectionSegment(BATH_RF,clist.get(30),clist.get(41)));
    	walls.add(new ReflectionSegment(BATH_RF,clist.get(41),clist.get(42)));
    	walls.add(new ReflectionSegment(BATH_RF,clist.get(30),clist.get(31)));
    	walls.add(new ReflectionSegment(BATH_RF,clist.get(31),clist.get(40)));
    	walls.add(new ReflectionSegment(BATH_RF,clist.get(40),clist.get(41)));
    	walls.add(new ReflectionSegment(BATH_RF,clist.get(31),clist.get(32)));
    	walls.add(new ReflectionSegment(BATH_RF,clist.get(32),clist.get(39)));
    	walls.add(new ReflectionSegment(BATH_RF,clist.get(39),clist.get(40)));
    	walls.add(new ReflectionSegment(BATH_RF,clist.get(39),clist.get(14)));
    	walls.add(new ReflectionSegment(BATH_RF,clist.get(32),clist.get(33)));
    	walls.add(new ReflectionSegment(BATH_RF,clist.get(33),clist.get(38)));
    	walls.add(new ReflectionSegment(BATH_RF,clist.get(38),clist.get(39)));
    	walls.add(new ReflectionSegment(BATH_RF,clist.get(33),clist.get(34)));
    	walls.add(new ReflectionSegment(BATH_RF,clist.get(34),clist.get(37)));
    	walls.add(new ReflectionSegment(BATH_RF,clist.get(37),clist.get(38)));
    	walls.add(new ReflectionSegment(BATH_RF,clist.get(34),clist.get(35)));
    	walls.add(new ReflectionSegment(BATH_RF,clist.get(35),clist.get(36)));
    	walls.add(new ReflectionSegment(BATH_RF,clist.get(36),clist.get(37)));
    	walls.add(new ReflectionSegment(BATH_RF,clist.get(36),clist.get(13)));
    	
    	//shelve col 1
    	for (int i = 0; i < 4; i++) {
    		double startx = 2;
    		double starty = 18;
    		double w = 5.0;
    		double h = 1.0;
    		double yoff = 2.0;
    		walls.addAll(placeShelve(new Coordinate(startx, starty+i*(h+yoff)),SHELVE_RF,w,h));
    	}
    	//shelve col 2
    	for (int i = 0; i < 4; i++) {
    		double startx = 9;
    		double starty = 12;
    		double w = 5.0;
    		double h = 1.0;
    		double yoff = 2.0;
    		walls.addAll(placeShelve(new Coordinate(startx, starty+i*(h+yoff)),SHELVE_RF,w,h));
    	}
    	
    	//shelve col 3
    	for (int i = 0; i < 4; i++) {
    		double startx = 16;
    		double starty = 12;
    		double w = 5.0;
    		double h = 1.0;
    		double yoff = 2.0;
    		walls.addAll(placeShelve(new Coordinate(startx, starty+i*(h+yoff)),SHELVE_RF,w,h));
    	}
    	
    	//shelve col 4
    	for (int i = 0; i < 3; i++) {
    		double startx = 23;
    		double starty = 18;
    		double w = 5.0;
    		double h = 1.0;
    		double yoff = 2.0;
    		walls.addAll(placeShelve(new Coordinate(startx, starty+i*(h+yoff)),SHELVE_RF,w,h));
    	}
    	
    	//shelve col 5
    	for (int i = 0; i < 1; i++) {
    		double startx = 30;
    		double starty = 24;
    		double w = 5.0;
    		double h = 1.0;
    		double yoff = 2.0;
    		walls.addAll(placeShelve(new Coordinate(startx, starty+i*(h+yoff)),SHELVE_RF,w,h));
    	}
    	
    	Environment e = new Environment();
    	e.setReflectionSegments(walls);
    	e.setBoundingBox(env);
    	return e;
    }
    
    private static List<ReflectionSegment> placeShelve(Coordinate ll,double reflect, double width, double height) {
    	ArrayList<ReflectionSegment> shelve = new ArrayList<ReflectionSegment>();
    	Coordinate lr = new Coordinate(ll.x+width,ll.y);
    	Coordinate ur = new Coordinate(lr.x,lr.y+height);
    	Coordinate ul = new Coordinate(ur.x-width,ur.y);
    	
    	shelve.add(new ReflectionSegment(reflect,ll,lr));
    	shelve.add(new ReflectionSegment(reflect,lr,ur));
    	shelve.add(new ReflectionSegment(reflect,ur,ul));
    	shelve.add(new ReflectionSegment(reflect,ul,ll));
    	
    	return shelve;
    }
}
