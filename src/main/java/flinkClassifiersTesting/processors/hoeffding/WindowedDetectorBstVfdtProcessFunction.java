package flinkClassifiersTesting.processors.hoeffding;

import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.api.common.typeinfo.TypeHint;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import flinkClassifiersTesting.classifiers.driftDetection.WindowedDetectorBstHoeffdingTree;
import flinkClassifiersTesting.classifiers.driftDetection.WindowedDetectorHoeffdingTreeFields;
import flinkClassifiersTesting.classifiers.hoeffding.SimpleNodeStatistics;
import flinkClassifiersTesting.classifiers.hoeffding.SimpleNodeStatisticsBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public abstract class WindowedDetectorBstVfdtProcessFunction extends BaseVfdtProcessFunction<WindowedDetectorBstHoeffdingTree<SimpleNodeStatistics, SimpleNodeStatisticsBuilder>> {
    public WindowedDetectorBstVfdtProcessFunction(String name, String dataset, long bootstrapSamplesLimit, Map<String, Integer> encoder) {
        super(name, dataset, bootstrapSamplesLimit, encoder);
    }

    @Override
    protected void registerClassifier() {
        TypeInformation<WindowedDetectorBstHoeffdingTree<SimpleNodeStatistics, SimpleNodeStatisticsBuilder>> classifierInfo = TypeInformation.of(new TypeHint<>() {
        });
        classifierState = getRuntimeContext().getState(new ValueStateDescriptor<>("bstVfdtWindowedDetectorState", classifierInfo));
    }

    @Override
    public List<String> csvColumnsHeader() {
        List<String> parentResult = super.csvColumnsHeader();
        List<String> baseFields = parentResult.subList(0, parentResult.size() - 1);
        List<String> result = new ArrayList<>(baseFields);
        result.add(WindowedDetectorHoeffdingTreeFields.SUBSTITUTE_TRAINING_BEGAN);
        result.add(WindowedDetectorHoeffdingTreeFields.REPLACED_CLASSIFIER);
        result.add(parentResult.get(parentResult.size() - 1));
        return result;
    }
}
