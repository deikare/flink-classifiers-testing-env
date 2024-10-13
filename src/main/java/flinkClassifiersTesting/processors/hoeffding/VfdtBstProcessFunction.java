package flinkClassifiersTesting.processors.hoeffding;

import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.api.common.typeinfo.TypeHint;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import flinkClassifiersTesting.classifiers.bstHoeffding.standard.BstHoeffdingTree;
import flinkClassifiersTesting.classifiers.hoeffding.SimpleNodeStatistics;
import flinkClassifiersTesting.classifiers.hoeffding.SimpleNodeStatisticsBuilder;

import java.util.Map;

public abstract class VfdtBstProcessFunction extends BaseVfdtProcessFunction<BstHoeffdingTree<SimpleNodeStatistics, SimpleNodeStatisticsBuilder>> {

    public VfdtBstProcessFunction(String name, String dataset, long bootstrapSamplesLimit, Map<String, Integer> encoder) {
        super(name, dataset, bootstrapSamplesLimit, encoder);
    }

    @Override
    protected void registerClassifier() {
        TypeInformation<BstHoeffdingTree<SimpleNodeStatistics, SimpleNodeStatisticsBuilder>> classifierInfo = TypeInformation.of(new TypeHint<>() {
        });
        classifierState = getRuntimeContext().getState(new ValueStateDescriptor<>("vfdtBstClassifier", classifierInfo));
    }
}
