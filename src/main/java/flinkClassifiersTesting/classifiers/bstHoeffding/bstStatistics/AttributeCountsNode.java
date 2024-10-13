package flinkClassifiersTesting.classifiers.bstHoeffding.bstStatistics;

public class AttributeCountsNode {
    final double value;

    long[] ve;
    long[] vh;

    AttributeCountsNode leftChild;
    AttributeCountsNode rightChild;

    public AttributeCountsNode(double value, int classIndex, int classNumber) {
        this.value = value;

        this.ve = new long[classNumber];
        this.ve[classIndex]++;
        this.vh = new long[classNumber];

        this.leftChild = null;
        this.rightChild = null;
    }

    public long[] getVe() {
        return ve;
    }
}
