package flinkClassifiersTesting.processors.hoeffding.noParentDisable;

import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.api.common.typeinfo.TypeHint;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import flinkClassifiersTesting.classifiers.hoeffding.GaussianNaiveBayesStatistics;
import flinkClassifiersTesting.classifiers.hoeffding.GaussianNaiveBayesStatisticsBuilder;
import flinkClassifiersTesting.classifiers.hoeffding.noParentDisable.HoeffdingTreeNoParentDisable;
import flinkClassifiersTesting.processors.hoeffding.BaseVfdtProcessFunction;

import java.util.Map;

public abstract class VfdtGaussianNaiveBayesNoParentDisableProcessFunction extends BaseVfdtProcessFunction<HoeffdingTreeNoParentDisable<GaussianNaiveBayesStatistics, GaussianNaiveBayesStatisticsBuilder>> {
    public VfdtGaussianNaiveBayesNoParentDisableProcessFunction(String name, String dataset, long bootstrapSamplesLimit, Map<String, Integer> encoder) {
        super(name, dataset, bootstrapSamplesLimit, encoder);
    }

    @Override
    protected void registerClassifier() {
        TypeInformation<HoeffdingTreeNoParentDisable<GaussianNaiveBayesStatistics, GaussianNaiveBayesStatisticsBuilder>> classifierInfo = TypeInformation.of(new TypeHint<>() {
        });
        classifierState = getRuntimeContext().getState(new ValueStateDescriptor<>("vfdtGaussNbClassifier", classifierInfo));
    }
}
