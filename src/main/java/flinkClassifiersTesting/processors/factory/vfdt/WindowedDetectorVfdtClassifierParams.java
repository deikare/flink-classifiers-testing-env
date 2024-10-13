package flinkClassifiersTesting.processors.factory.vfdt;

public class WindowedDetectorVfdtClassifierParams extends VfdtClassifierParams {
    public int windowSize;
    public double warningFrac;
    public double driftFrac;
    public int updateMaxAccuracyEachSamples;
    public int bootstrapSubstituteTraining;

    public WindowedDetectorVfdtClassifierParams() {
    }

    public WindowedDetectorVfdtClassifierParams(double delta, double tau, long nMin, int windowSize, double warningFrac, double driftFrac, int updateMaxAccuracyEachSamples, int bootstrapSubstituteTraining) {
        super(delta, tau, nMin);
        this.windowSize = windowSize;
        this.warningFrac = warningFrac;
        this.driftFrac = driftFrac;
        this.updateMaxAccuracyEachSamples = updateMaxAccuracyEachSamples;
        this.bootstrapSubstituteTraining = bootstrapSubstituteTraining;
    }

    @Override
    public String toString() {
        return "WindowedDetectorVfdtClassifierParams{" +
                "bootstrapSubstituteTraining=" + bootstrapSubstituteTraining +
                ", windowSize=" + windowSize +
                ", warningFrac=" + warningFrac +
                ", driftFrac=" + driftFrac +
                ", updateMaxAccuracyEachSamples=" + updateMaxAccuracyEachSamples +
                ", delta=" + delta +
                ", tau=" + tau +
                ", nMin=" + nMin +
                "} " + super.toString();
    }

    @Override
    public String directoryName() {
        return super.directoryName() + "_windowSize" + windowSize + "_warningFrac" + warningFrac + "_driftFrac" + driftFrac + "_updateMaxAccuracyEachSamples" + updateMaxAccuracyEachSamples + "_bootstrapSubstituteTraining" + bootstrapSubstituteTraining;
    }
}
