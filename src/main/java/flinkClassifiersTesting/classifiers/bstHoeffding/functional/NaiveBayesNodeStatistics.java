package flinkClassifiersTesting.classifiers.bstHoeffding.functional;

import flinkClassifiersTesting.classifiers.bstHoeffding.bstStatistics.AttributeValuesCountBst;
import flinkClassifiersTesting.classifiers.hoeffding.NodeStatistics;
import flinkClassifiersTesting.inputs.Example;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class NaiveBayesNodeStatistics extends NodeStatistics {
    private final double[] highestAttributes;
    private final double[] lowestAttributes;
    private final List<Set<Double>> differentValuesOfEachAttribute;
    private final int maxDifferentValuesCount;

    public NaiveBayesNodeStatistics(int classNumber, int attributesNumber, int maxDifferentValuesCount) {
        super(classNumber);
        highestAttributes = new double[attributesNumber];
        lowestAttributes = new double[attributesNumber];
        differentValuesOfEachAttribute = new ArrayList<>(attributesNumber);
        this.maxDifferentValuesCount = maxDifferentValuesCount;
        for (int attributeIndex = 0; attributeIndex < attributesNumber; attributeIndex++) {
            highestAttributes[attributeIndex] = -Double.MAX_VALUE;
            lowestAttributes[attributeIndex] = Double.MAX_VALUE;
            differentValuesOfEachAttribute.add(new HashSet<>(maxDifferentValuesCount));
        }

    }


    @Override
    protected void updateAttributeStatistics(Example example, Integer disabledAttributeIndex) {
        double[] attributes = example.getAttributes();
        for (int attributeIndex = 0; attributeIndex < attributes.length; attributeIndex++) {
            double attribute = attributes[attributeIndex];
            if (attribute > highestAttributes[attributeIndex])
                highestAttributes[attributeIndex] = attribute;
            if (attribute < lowestAttributes[attributeIndex])
                lowestAttributes[attributeIndex] = attribute;
            Set<Double> differentValuesOfAttribute = differentValuesOfEachAttribute.get(attributeIndex);
            if (differentValuesOfAttribute.size() < maxDifferentValuesCount)
                differentValuesOfAttribute.add(attribute);
        }
    }

    @Override
    public double getSplittingValue(int attributeNumber) {
        return -Double.MAX_VALUE;
    }

    public int getMajorityClass(Example example, List<AttributeValuesCountBst> trees) {
        int predictedClass = -1;
        double maxProbability = -1.0;

        for (int classIndex = 0; classIndex < classCounts.length; classIndex++) {
            double probability = ((double) classCounts[classIndex]) / ((double) n);

            double[] attributes = example.getAttributes();
            for (int attributeIndex = 0; attributeIndex < attributes.length; attributeIndex++) {
                probability *= attributeProbability(attributes[attributeIndex], attributeIndex, classIndex, trees.get(attributeIndex));
            }

            if (probability > maxProbability) {
                predictedClass = classIndex;
                maxProbability = probability;
            }
        }

        return predictedClass;
    }

    private double attributeProbability(double attribute, int attributeIndex, int classIndex, AttributeValuesCountBst attributeBst) {
        int intervalsNumber = Math.min(maxDifferentValuesCount, differentValuesOfEachAttribute.get(attributeIndex).size());
        double lowestAttribute = lowestAttributes[attributeIndex];
        double inc = (highestAttributes[attributeIndex] - lowestAttribute) / ((double) intervalsNumber);
        double[] counts = new double[intervalsNumber];

        for (int i = 0; i < intervalsNumber; i++) {
            double toCompare = lowestAttribute + ((double) i + 1) * inc;
            counts[i] = attributeBst.getNodeOfValueSmallerOrEqualThan(toCompare).getVe()[classIndex]; //todo null pointer exception
            if (i > 0)
                counts[i] = counts[i] - counts[i - 1];

            if (attribute <= toCompare)
                return counts[i] / ((double) n);
        }

        return counts[intervalsNumber - 1] / ((double) n);
    }
}