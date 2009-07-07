package anakata.modplay.player;

public abstract class AbstractMixer implements Mixer {

	private double amplification = 1;
    private double volume = 1;
    private double balance = 0.5;
    private double separation = 1;
    private int numberOfTracks;
	
    protected AbstractMixer(int numberOfTracks) {
    	this.numberOfTracks = numberOfTracks;
    }
    
	public double getAmplification() {
		return amplification;
	}

	public void setAmplification(double amp) {
		this.amplification = amp;
	}

	public double getBalance() {
		return balance;
	}

	public void setBalance(double balance) {
		this.balance = balance;
	}

	public double getSeparation() {
		return separation;
	}

	public void setSeparation(double separation) {
		this.separation = separation;
	}

    public double getVolume() {
        return volume;
    }

    public void setVolume(double volume) {
        if (volume > 1)
            volume = 1;
        else if (volume < 0)
            volume = 0;
        this.volume = volume;
    }

	public int getNumberOfTracks() {
		return numberOfTracks;
	}
}
