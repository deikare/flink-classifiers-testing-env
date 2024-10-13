package flinkClassifiersTesting.processors.hoeffding;

import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.api.common.typeinfo.TypeHint;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import flinkClassifiersTesting.classifiers.hoeffding.GaussianNaiveBayesStatistics;
import flinkClassifiersTesting.classifiers.hoeffding.GaussianNaiveBayesStatisticsBuilder;
import flinkClassifiersTesting.classifiers.hoeffding.HoeffdingTree;

import java.util.Map;

public abstract class VfdtGaussianNaiveBayesProcessFunction extends BaseVfdtProcessFunction<HoeffdingTree<GaussianNaiveBayesStatistics, GaussianNaiveBayesStatisticsBuilder>> {
    public VfdtGaussianNaiveBayesProcessFunction(String name, String dataset, long bootstrapSamplesLimit, Map<String, Integer> encoder) {
        super(name, dataset, bootstrapSamplesLimit, encoder);
    }

    @Override
    protected void registerClassifier() {
        TypeInformation<HoeffdingTree<GaussianNaiveBayesStatistics, GaussianNaiveBayesStatisticsBuilder>> classifierInfo = TypeInformation.of(new TypeHint<>() {
        });
        classifierState = getRuntimeContext().getState(new ValueStateDescriptor<>("vfdtGaussNbClassifier", classifierInfo));
    }
}
