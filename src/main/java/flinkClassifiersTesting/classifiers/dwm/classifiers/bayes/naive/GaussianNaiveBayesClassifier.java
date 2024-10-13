package flinkClassifiersTesting.classifiers.dwm.classifiers.bayes.naive;

import org.apache.flink.api.java.tuple.Tuple2;
import flinkClassifiersTesting.inputs.Example;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;

import static flinkClassifiersTesting.classifiers.helpers.Helpers.toNow;

public class GaussianNaiveBayesClassifier extends NaiveBayesClassifier {
    ArrayList<ArrayList<Tuple2<Double, Double>>> attributeSumsForEachClass;


    public GaussianNaiveBayesClassifier(int classesCount, int attributesCount) {
        super(classesCount);

        attributeSumsForEachClass = new ArrayList<>(classesCount);
        for (int classNumber = 0; classNumber < classesCount; classNumber++) {
            ArrayList<Tuple2<Double, Double>> attributeSums = new ArrayList<>(attributesCount);
            for (int attributeNumber = 0; attributeNumber < attributesCount; attributeNumber++) {
                attributeSums.add(Tuple2.of(0.0, 0.0));
            }
            attributeSumsForEachClass.add(attributeSums);
        }
    }

    @Override
    protected double attributeProbability(int classNumber, int attributeNumber, double[] attributes) {
        Tuple2<Double, Double> attributeSums = attributeSumsForEachClass.get(classNumber).get(attributeNumber);
        double attribute = attributes[attributeNumber];
        double sum = attributeSums.f0 + attribute;
        double mean = sum / sampleNumber;
        double sumSquared = attributeSums.f1 + Math.pow(attribute, 2.0);
        double varianceNumerator = sumSquared - 2 * mean * sum + Math.pow(mean, 2.0) * sampleNumber;
        double variance = (sampleNumber == 1) ? varianceNumerator : varianceNumerator / ((double) (sampleNumber - 1L)); // Beissel correction
        return Math.exp(-Math.pow(attribute - mean, 2.0) / (2.0 * variance)) / (Math.sqrt(2 * Math.PI * variance));
    }

    @Override
    protected ArrayList<Tuple2<String, Long>> trainImplementation(Example example, Instant start) {
        int classNumber = example.getMappedClass();
        ArrayList<Tuple2<Double, Double>> attributeSums = attributeSumsForEachClass.get(classNumber);

        double[] attributes = example.getAttributes();
        for (int i = 0; i < attributes.length; i++) {
            double attributeValue = attributes[i];
            Tuple2<Double, Double> attributeValueSums = attributeSums.get(i);
            attributeValueSums.f0 += attributeValue;
            attributeValueSums.f1 += Math.pow(attributeValue, 2.0);
        }

        return new ArrayList<>(Collections.singletonList(Tuple2.of(NaiveBayesFields.AVG_TRAIN_DURATION, toNow(start))));
    }
}
