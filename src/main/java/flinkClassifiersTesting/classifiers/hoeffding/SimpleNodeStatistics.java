package flinkClassifiersTesting.classifiers.hoeffding;

import flinkClassifiersTesting.inputs.Example;

import java.util.*;

public class SimpleNodeStatistics extends NodeStatistics {
    protected final List<List<Map<Double, Long>>> attributeValueCounts;

    public SimpleNodeStatistics(int classNumber, int attributesNumber) {
        super(classNumber);

        attributeValueCounts = new ArrayList<>(attributesNumber);

        for (int attributeIndex = 0; attributeIndex < attributesNumber; attributeIndex++) {
            List<Map<Double, Long>> attributeValuesForEachClass = new ArrayList<>(classNumber);
            for (int classIndex = 0; classIndex < classNumber; classIndex++) {
                attributeValuesForEachClass.add(new HashMap<>());
            }
            attributeValueCounts.add(attributeValuesForEachClass);
        }
    }

    @Override
    protected void updateAttributeStatistics(Example example, Integer disabledAttributeIndex) {
        if (disabledAttributeIndex == null)
            incrementAttributeCounts(example, 0, attributeValueCounts.size(), 1L);
        else {
            incrementAttributeCounts(example, 0, disabledAttributeIndex, 1L);
            incrementAttributeCounts(example, disabledAttributeIndex + 1, attributeValueCounts.size(), 1L);
        }
    }

    private void incrementAttributeCounts(Example example, int start, int end, long increment) {
        double[] exampleAttributes = example.getAttributes();
        int exampleClass = example.getMappedClass();
        for (int attributeIndex = start; attributeIndex < end; attributeIndex++) {
            attributeValueCounts.get(attributeIndex).get(exampleClass).compute(exampleAttributes[attributeIndex], (key, value) -> (value == null) ? 1L : value + increment);
        }
    }

    @Override
    public double getSplittingValue(int attributeNumber) throws RuntimeException {
        int majorityClass = getMajorityClass();
        return Collections.max(attributeValueCounts.get(attributeNumber).get(majorityClass).entrySet(), Map.Entry.comparingByValue()).getKey();
    }

    public List<List<Map<Double, Long>>> getAttributeValueCounts() {
        return attributeValueCounts;
    }

    @Override
    public String toString() {
        return "NodeWithAttributeValueCountsStatistics{" +
                "attributeValueCounts=" + attributeValueCounts +
                "} " + super.toString();
    }
}
