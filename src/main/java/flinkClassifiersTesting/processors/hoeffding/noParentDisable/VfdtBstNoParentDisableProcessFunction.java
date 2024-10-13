package flinkClassifiersTesting.processors.hoeffding.noParentDisable;

import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.api.common.typeinfo.TypeHint;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import flinkClassifiersTesting.classifiers.hoeffding.SimpleNodeStatistics;
import flinkClassifiersTesting.classifiers.hoeffding.SimpleNodeStatisticsBuilder;
import flinkClassifiersTesting.classifiers.hoeffding.noParentDisable.BstHoeffdingTreeNoParentDisable;
import flinkClassifiersTesting.processors.hoeffding.BaseVfdtProcessFunction;

import java.util.Map;

public abstract class VfdtBstNoParentDisableProcessFunction extends BaseVfdtProcessFunction<BstHoeffdingTreeNoParentDisable<SimpleNodeStatistics, SimpleNodeStatisticsBuilder>> {
    public VfdtBstNoParentDisableProcessFunction(String name, String dataset, long bootstrapSamplesLimit, Map<String, Integer> encoder) {
        super(name, dataset, bootstrapSamplesLimit, encoder);
    }

    @Override
    protected void registerClassifier() {
        TypeInformation<BstHoeffdingTreeNoParentDisable<SimpleNodeStatistics, SimpleNodeStatisticsBuilder>> classifierInfo = TypeInformation.of(new TypeHint<>() {
        });
        classifierState = getRuntimeContext().getState(new ValueStateDescriptor<>("vfdtBstNoParentDisableClassifier", classifierInfo));
    }
}
