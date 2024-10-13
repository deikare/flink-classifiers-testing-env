package flinkClassifiersTesting.processors.factory.dwm;

import flinkClassifiersTesting.processors.factory.ClassifierParamsInterface;

public class DwmClassifierParams implements ClassifierParamsInterface {
    public double beta;
    public double threshold;
    public int updateClassifiersEachSamples;

    public DwmClassifierParams() {
    }

    public DwmClassifierParams(double beta, double threshold, int updateClassifiersEachSamples) {
        this.beta = beta;
        this.threshold = threshold;
        this.updateClassifiersEachSamples = updateClassifiersEachSamples;
    }

    @Override
    public String toString() {
        return "{" +
                "beta=" + beta +
                ", threshold=" + threshold +
                ", updateClassifiersEachSamples=" + updateClassifiersEachSamples +
                '}';
    }

    @Override
    public String directoryName() {
        return "beta" + beta + "_threshold" + threshold + "_updateClassifiersEachSamples" + updateClassifiersEachSamples;
    }
}
