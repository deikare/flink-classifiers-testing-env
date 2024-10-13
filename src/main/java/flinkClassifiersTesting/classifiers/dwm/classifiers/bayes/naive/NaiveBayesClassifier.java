package flinkClassifiersTesting.classifiers.dwm.classifiers.bayes.naive;

import org.apache.flink.api.java.tuple.Tuple2;
import flinkClassifiersTesting.classifiers.dwm.classic.ClassifierInterface;
import flinkClassifiersTesting.inputs.Example;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;

import static flinkClassifiersTesting.classifiers.helpers.Helpers.toNow;

public abstract class NaiveBayesClassifier implements ClassifierInterface {
    protected long sampleNumber;

    ArrayList<Long> classCounts;

    public NaiveBayesClassifier(int classesAmount) {
        sampleNumber = 0L;
        classCounts = new ArrayList<>(classesAmount);
        for (int classNumber = 0; classNumber < classesAmount; classNumber++)
            classCounts.add(0L);
    }

    @Override
    public Tuple2<Integer, ArrayList<Tuple2<String, Long>>> classify(Example example) {
        Instant start = Instant.now();

        sampleNumber++;

        int predictedClass = 0;
        double maxProbability = 0.0;

        double[] attributes = example.getAttributes();

        for (int classNumber = 0; classNumber < classCounts.size(); classNumber++) {
            double classProbability = ((double) (classCounts.get(classNumber))) / ((double) (sampleNumber));

            double attributesProbability = 1.0;

            for (int attributeNumber = 0; attributeNumber < attributes.length; attributeNumber++) {
                double probability = attributeProbability(classNumber, attributeNumber, attributes);
                attributesProbability *= probability;
            }

            double probability = classProbability * attributesProbability;
            if (probability > maxProbability) {
                predictedClass = classNumber;
                maxProbability = probability;
            }
        }

        return Tuple2.of(predictedClass, new ArrayList<>(Collections.singletonList(Tuple2.of(NaiveBayesFields.AVG_CLASSIFY_DURATION, toNow(start)))));
    }

    protected abstract double attributeProbability(int classNumber, int attributeNumber, double[] attributes);

    @Override
    public ArrayList<Tuple2<String, Long>> train(Example example) {
        Instant start = Instant.now();

        int exampleClass = example.getMappedClass();

        classCounts.set(exampleClass, classCounts.get(exampleClass) + 1L);

        return trainImplementation(example, start);
    }

    protected abstract ArrayList<Tuple2<String, Long>> trainImplementation(Example example, Instant start);
}
