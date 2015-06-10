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
    	int max_bounce_lvl = 2;
    	int visual_bounce_area = 0;
    	double threshold = 35.0; // the perceived dB threshold we are interested in
    	
    	int bounce_lvl = 1;
    	boolean cumulative = true;
    	/********************************************
    	 ********************************************
    	 ** Environment 1
    	 ********************************************
    	 ********************************************/
    	SoundModel model1 = App.createModel1(bounce_lvl);

    	model1.setVolumeThreshold(threshold);
    	
    	String dir_e1 = "./img/environment1/";
    	
    	
//    	App.performBounceLevelModel(model1, bounce_lvl, 
//    			cumulative,dir_e1);
    	App.performAudioThresholdModel(model1, threshold, dir_e1);
    	
    	
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
    }
    public static void performAudioThresholdModel(SoundModel model, double threshold, String dir) throws IOException {
    	boolean cumulative = true;
    	dir += (!dir.endsWith("/")) ? "/" : "";
    	model.setVolumeThreshold(threshold);
    	String file_name = dir+"threshold-"+threshold;
    	file_name += (cumulative)? "-cum.svg" : ".svg";
    	
    	model.run();
    	
//    	for (LineString ls : model.getPropagationPaths(model.getSources().get(0))) {
//    		System.out.println(ls);
//    	}
    	
    	//handle the visualization of the model
    	ModelVisualization mv = new ModelVisualization(model);
    	mv.visualizeAudibleArea(true);
    	mv.visualizePaths(true);
    	mv.exportSVG(file_name);
    }
    
    public static void performBounceLevelModel(SoundModel model, int bounces, boolean cumulative,String dir) throws IOException {
    	dir += (!dir.endsWith("/")) ? "/" : "";
    	
    	String file_name = dir+"bounces"+bounces;
    	file_name += (cumulative)? "-cum.svg" : ".svg";
    	
    	model.run();
    	
    	ModelVisualization mv = new ModelVisualization(model);
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
    			0,200);
    	src1.setVolume(70.0);
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
    	model.addSource(src2);
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
    	double globalEnvironmentReflectionFactor = 1.0;
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
}
