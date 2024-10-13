package flinkClassifiersTesting.classifiers.hoeffding;

import flinkClassifiersTesting.inputs.Example;

import java.util.Arrays;

import static flinkClassifiersTesting.classifiers.helpers.Helpers.getIndexOfHighestValue;

public abstract class NodeStatistics {
    protected long n;

    protected final Long[] classCounts;

    public NodeStatistics(int classNumber) {
        n = 0;
        classCounts = new Long[classNumber];
        for (int i = 0; i < classNumber; i++)
            classCounts[i] = 0L;
    }

    public void update(Example example, Integer disabledAttributeIndex) {
        n++;
        classCounts[example.getMappedClass()]++;
        updateAttributeStatistics(example, disabledAttributeIndex);
    }

    protected abstract void updateAttributeStatistics(Example example, Integer disabledAttributeIndex);

    public int getMajorityClass(Example example) {
        return getIndexOfHighestValue(classCounts);
    }

    public int getMajorityClass() {
        return getIndexOfHighestValue(classCounts);
    }

    public long getClassCount(int classIndex) {
        return classCounts[classIndex];
    }

    public abstract double getSplittingValue(int attributeNumber);

    public long getN() {
        return n;
    }

    public void resetN() {
        n = 0L;
    }

    @Override
    public String toString() {
        return "NodeStatistics{" +
                "n=" + n +
                ", classCounts=" + Arrays.toString(classCounts) +
                '}';
    }
}
