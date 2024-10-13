package flinkClassifiersTesting.classifiers.hoeffding;

public class SimpleNodeStatisticsBuilder implements StatisticsBuilderInterface<SimpleNodeStatistics> {
    private final int classNumber;
    private final int attributesNumber;

    public SimpleNodeStatisticsBuilder(int classNumber, int attributesNumber) {
        this.classNumber = classNumber;
        this.attributesNumber = attributesNumber;
    }

    @Override
    public SimpleNodeStatistics build() {
        return new SimpleNodeStatistics(classNumber, attributesNumber);
    }
}
