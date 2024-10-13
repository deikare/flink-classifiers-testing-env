package flinkClassifiersTesting.classifiers.hoeffding;

public class GaussianNaiveBayesStatisticsBuilder implements StatisticsBuilderInterface<GaussianNaiveBayesStatistics> {
    private final int classNumber;
    private final int attributesNumber;

    public GaussianNaiveBayesStatisticsBuilder(int classNumber, int attributesNumber) {
        this.classNumber = classNumber;
        this.attributesNumber = attributesNumber;
    }

    @Override
    public GaussianNaiveBayesStatistics build() {
        return new GaussianNaiveBayesStatistics(classNumber, attributesNumber);
    }
}
