package flinkClassifiersTesting.processors.dwm;

import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.api.common.typeinfo.TypeHint;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import flinkClassifiersTesting.classifiers.dwm.classifiers.bayes.naive.GaussianNaiveBayesClassifier;
import flinkClassifiersTesting.classifiers.dwm.extended.ExtendedDynamicWeightedMajority;

import java.util.Map;

public abstract class ExtendedDwmProcessFunction extends BaseDwmProcessFunction<ExtendedDynamicWeightedMajority<GaussianNaiveBayesClassifier>> {

    public ExtendedDwmProcessFunction(String name, String dataset, long bootstrapSamplesLimit, Map<String, Integer> encoder) {
        super(name, dataset, bootstrapSamplesLimit, encoder);
    }

    @Override
    protected void registerClassifier() {
        TypeInformation<ExtendedDynamicWeightedMajority<GaussianNaiveBayesClassifier>> classifierInfo = TypeInformation.of(new TypeHint<>() {
        });

        classifierState = getRuntimeContext().getState(new ValueStateDescriptor<>("extendedDwmClassifier", classifierInfo));
    }
}
