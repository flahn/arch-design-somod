package SIL.SoMod.emission;

import java.util.List;

import SIL.SoMod.environment.Environment;
import SIL.SoMod.environment.SoundSource;

import com.vividsolutions.jts.geom.LineSegment;
import com.vividsolutions.jts.geom.LineString;

public interface SoundEmissionModel {
	public List<LineSegment> calculateInitialPropagationPaths(SoundSource s);
	public void addSoundSource(SoundSource s);
	public void setEnvironment(Environment e);
	public Environment getEnvironment();
	public void propagate();
	public List<List<LineString>> getPropagationPaths();
	public List<LineString> getPropagationPaths(SoundSource s);
	public void setSources(List<SoundSource> sources);
	public List<SoundSource> getSources();
	public double getAudioThreshold();
	public void setAudioThreshold(double audioThreshold);
}
