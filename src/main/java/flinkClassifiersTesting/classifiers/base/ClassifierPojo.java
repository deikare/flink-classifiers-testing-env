package flinkClassifiersTesting.classifiers.base;

import org.apache.flink.api.java.tuple.Tuple2;
import flinkClassifiersTesting.classifiers.dwm.classic.ClassifierInterface;
import flinkClassifiersTesting.inputs.Example;

import java.util.ArrayList;

public class ClassifierPojo<C extends ClassifierInterface> {
    private final C classifier;
    private double weight;
    private final long sampleNumber;

    public ClassifierPojo(C classifier, long sampleNumber) {
        this.classifier = classifier;
        this.sampleNumber = sampleNumber;
        this.weight = 1.0;
    }

    public double getWeight() {
        return weight;
    }

    public void lowerWeight(double beta) {
        weight *= beta;
    }

    public long getSampleNumber() {
        return sampleNumber;
    }

    public C getClassifier() {
        return classifier;
    }

    public ArrayList<Tuple2<String, Long>> train(Example example) {
        return classifier.train(example);
    }

    public Tuple2<Integer, ArrayList<Tuple2<String, Long>>> classify(Example example) {
        return classifier.classify(example);
    }

    public void normalizeWeight(double maxWeight) {
        weight /= maxWeight;
    }
}