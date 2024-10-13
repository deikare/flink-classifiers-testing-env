package flinkClassifiersTesting.processors.hoeffding.noParentDisable;

import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.api.common.typeinfo.TypeHint;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import flinkClassifiersTesting.classifiers.bstHoeffding.functional.NaiveBayesNodeStatistics;
import flinkClassifiersTesting.classifiers.bstHoeffding.functional.NaiveBayesNodeStatisticsBuilder;
import flinkClassifiersTesting.classifiers.hoeffding.noParentDisable.FunctionalBstHoeffdingTreeNoParentDisable;
import flinkClassifiersTesting.processors.hoeffding.BaseVfdtProcessFunction;

import java.util.Map;

public abstract class FunctionalVfdtBstNoParentDisableProcessFunction extends BaseVfdtProcessFunction<FunctionalBstHoeffdingTreeNoParentDisable<NaiveBayesNodeStatistics, NaiveBayesNodeStatisticsBuilder>> {
    public FunctionalVfdtBstNoParentDisableProcessFunction(String name, String dataset, long bootstrapSamplesLimit, Map<String, Integer> encoder) {
        super(name, dataset, bootstrapSamplesLimit, encoder);
    }

    @Override
    protected void registerClassifier() {
        TypeInformation<FunctionalBstHoeffdingTreeNoParentDisable<NaiveBayesNodeStatistics, NaiveBayesNodeStatisticsBuilder>> classifierInfo = TypeInformation.of(new TypeHint<>() {
        });
        classifierState = getRuntimeContext().getState(new ValueStateDescriptor<>("VfdtBstNbNoParentDisableClassifier", classifierInfo));
    }
}
