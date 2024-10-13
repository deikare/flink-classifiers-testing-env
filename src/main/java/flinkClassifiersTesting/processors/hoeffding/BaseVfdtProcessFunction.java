package flinkClassifiersTesting.processors.hoeffding;

import flinkClassifiersTesting.classifiers.base.BaseClassifierFields;
import flinkClassifiersTesting.classifiers.hoeffding.HoeffdingTree;
import flinkClassifiersTesting.classifiers.hoeffding.HoeffdingTreeFields;
import flinkClassifiersTesting.processors.base.BaseProcessFunctionTrainAndClassify;

import java.util.List;
import java.util.Map;

public abstract class BaseVfdtProcessFunction<V extends HoeffdingTree<?, ?>> extends BaseProcessFunctionTrainAndClassify<V> {
    public BaseVfdtProcessFunction(String name, String dataset, long bootstrapSamplesLimit, Map<String, Integer> encoder) {
        super(name, dataset, bootstrapSamplesLimit, encoder);
    }

    @Override
    public List<String> csvColumnsHeader() {
        return List.of(HoeffdingTreeFields.NODES_DURING_TRAVERSAL_COUNT, HoeffdingTreeFields.DURING_TRAVERSAL_DURATION, HoeffdingTreeFields.LEAF_SPLIT, HoeffdingTreeFields.TREE_SIZE, BaseClassifierFields.TRAINING_DURATION, BaseClassifierFields.CLASSIFICATION_DURATION);
    }
}
