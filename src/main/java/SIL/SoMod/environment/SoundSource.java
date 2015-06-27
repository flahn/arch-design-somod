package SIL.SoMod.environment;


public class SoundSource extends PropagationPathPoint {
	private double orientation; //angle from x-axis counterclockwise in radians
	private double hAngle;
	private double vAngle;
	private int rays;
	private double threshold;
	
	public SoundSource(double x, double y, 
			double orientation, double hAngle, 
			double vAngle, int rays) {
		super(x,y);
		this.orientation = orientation;
		this.hAngle = hAngle;
		this.vAngle = vAngle;
		this.rays = rays;
		this.distanceFromSource = 0.0;
	}
	
	public double getHorizontalAngle() {
		return this.hAngle;
	}
	
	public double getOrientation() {
		return this.orientation;
	}
	
	@Override
	public double getOutgoingVolume() {
		return this.outgoingVolume;
	}
	
	@Override
	public double getIncomingVolume() {
		return this.outgoingVolume;
	}
	
	public int getRayAmount() {
		return this.rays;
	}
	
	
	public void setVolume(double vol) {
		this.outgoingVolume = vol;
	}
	/**
	 * return horizontal angles left and right from orientation based on the horizontal angle 
	 * of sight.
	 * @return double array containing the left and right angle of the horizontal sight in radians
	 */
	public double[] getHorizontalRange() {
		double halfAngle = this.hAngle/2.0;
		double left = this.orientation+halfAngle;
		double right = this.orientation -halfAngle;
		double[] result = {left, right};
		return result;
	}
	public double calculateEmissionVolume() {
		//TODO change this due to the higher sound intensity at a limited soundwave surface
		// but leave this for now
		return this.outgoingVolume;
	}

	@Override
	public void setIncomingVolume(double incomingVolume) {
		this.setVolume(incomingVolume);
	}

	public double getThreshold() {
		return threshold;
	}

	public void setThreshold(double threshold) {
		this.threshold = threshold;
	}
}
