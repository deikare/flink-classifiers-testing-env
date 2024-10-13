package flinkClassifiersTesting.processors.hoeffding;

import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.api.common.typeinfo.TypeHint;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import flinkClassifiersTesting.classifiers.bstHoeffding.functional.FunctionalBstHoeffdingTree;
import flinkClassifiersTesting.classifiers.bstHoeffding.functional.NaiveBayesNodeStatistics;
import flinkClassifiersTesting.classifiers.bstHoeffding.functional.NaiveBayesNodeStatisticsBuilder;

import java.util.Map;

public abstract class FunctionalVfdtBstProcessFunction extends BaseVfdtProcessFunction<FunctionalBstHoeffdingTree<NaiveBayesNodeStatistics, NaiveBayesNodeStatisticsBuilder>> {

    public FunctionalVfdtBstProcessFunction(String name, String dataset, long bootstrapSamplesLimit, Map<String, Integer> encoder) {
        super(name, dataset, bootstrapSamplesLimit, encoder);
    }

    @Override
    protected void registerClassifier() {
        TypeInformation<FunctionalBstHoeffdingTree<NaiveBayesNodeStatistics, NaiveBayesNodeStatisticsBuilder>> classifierInfo = TypeInformation.of(new TypeHint<>() {
        });
        classifierState = getRuntimeContext().getState(new ValueStateDescriptor<>("VfdtBstNbClassifier", classifierInfo));
    }
}
