package flinkClassifiersTesting.classifiers.hoeffding;

import org.apache.flink.api.java.tuple.Tuple2;
import flinkClassifiersTesting.inputs.Example;

import java.util.ArrayList;
import java.util.List;

public class GaussianNaiveBayesStatistics extends SimpleNodeStatistics {
    private final List<List<Tuple2<Double, Double>>> attributeValueSums;

    public GaussianNaiveBayesStatistics(int classNumber, int attributeNumber) {
        super(classNumber, attributeNumber);

        attributeValueSums = new ArrayList<>(classNumber);
        for (int classIndex = 0; classIndex < classNumber; classIndex++) {
            List<Tuple2<Double, Double>> attributeSums = new ArrayList<>(attributeNumber);
            for (int attribute = 0; attribute < attributeNumber; attribute++) {
                attributeSums.add(Tuple2.of(0.0, 0.0));
            }
            attributeValueSums.add(attributeSums);
        }
    }

    @Override
    protected void updateAttributeStatistics(Example example, Integer disabledAttributeIndex) {
        super.updateAttributeStatistics(example, disabledAttributeIndex);

        List<Tuple2<Double, Double>> attributeSumsOfClass = attributeValueSums.get(example.getMappedClass());
        double[] attributes = example.getAttributes();
        for (int i = 0; i < attributes.length; i++) {
            double attribute = attributes[i];
            Tuple2<Double, Double> sums = attributeSumsOfClass.get(i);
            sums.f0 += attribute;
            sums.f1 += Math.pow(attribute, 2.0);
        }
    }

    @Override
    public int getMajorityClass(Example example) {
        int predictedClass = 0;
        double maxProbability = 0.0;

        double[] attributes = example.getAttributes();

        for (int classNumber = 0; classNumber < classCounts.length; classNumber++) {
            double classProbability = ((double) (classCounts[(classNumber)])) / ((double) (n));

            List<Tuple2<Double, Double>> attributeSums = attributeValueSums.get(classNumber);

            double attributesProbability = 1.0;
            for (int attributeNumber = 0; attributeNumber < attributeSums.size(); attributeNumber++) {
                Tuple2<Double, Double> sums = attributeSums.get(attributeNumber);
                double mean = sums.f0 / n;
                double varianceNumerator = sums.f1 - 2 * mean * sums.f0 + Math.pow(mean, 2.0) * n;
                double variance = (n == 1) ? varianceNumerator : varianceNumerator / ((double) (n - 1L));
                attributesProbability *= Math.exp(-Math.pow(attributes[attributeNumber] - mean, 2.0) / (2.0 * variance)) / (Math.sqrt(2 * Math.PI * variance));
            }

            double probability = classProbability * attributesProbability;
            if (probability > maxProbability) {
                predictedClass = classNumber;
                maxProbability = probability;
            }
        }

        return predictedClass;
    }
}
