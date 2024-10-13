package flinkClassifiersTesting.processors.dwm;

import flinkClassifiersTesting.classifiers.base.BaseClassifierFields;
import flinkClassifiersTesting.classifiers.dwm.BaseDynamicWeightedMajority;
import flinkClassifiersTesting.classifiers.dwm.DwmClassifierFields;
import flinkClassifiersTesting.classifiers.dwm.classifiers.bayes.naive.NaiveBayesFields;
import flinkClassifiersTesting.processors.base.BaseProcessFunctionClassifyAndTrain;

import java.util.List;
import java.util.Map;

abstract class BaseDwmProcessFunction<D extends BaseDynamicWeightedMajority<?, ?>> extends BaseProcessFunctionClassifyAndTrain<D> {
    public BaseDwmProcessFunction(String name, String dataset, long bootstrapSamplesLimit, Map<String, Integer> encoder) {
        super(name, dataset, bootstrapSamplesLimit, encoder);
    }

    @Override
    public List<String> csvColumnsHeader() {
        return List.of(NaiveBayesFields.AVG_CLASSIFY_DURATION, DwmClassifierFields.WEIGHTS_LOWERING_COUNT, DwmClassifierFields.CORRECT_VOTES_COUNT, DwmClassifierFields.WRONG_VOTES_COUNT, DwmClassifierFields.CLASSIFIERS_AFTER_CLASSIFICATION_COUNT, BaseClassifierFields.CLASSIFICATION_DURATION, DwmClassifierFields.WEIGHTS_NORMALIZATION_AND_CLASSIFIER_DELETE_DURATION, DwmClassifierFields.DELETED_CLASSIFIERS_COUNT, DwmClassifierFields.AVG_CLASSIFIER_TTL, DwmClassifierFields.ADD_CLASSIFIER_DURATION, DwmClassifierFields.ADDED_CLASSIFIERS_COUNT, NaiveBayesFields.AVG_TRAIN_DURATION, DwmClassifierFields.CLASSIFIERS_AFTER_TRAIN_COUNT, BaseClassifierFields.TRAINING_DURATION);
    }
}
