package flinkClassifiersTesting.processors.dwm;

import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.api.common.typeinfo.TypeHint;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import flinkClassifiersTesting.classifiers.dwm.classic.ClassicDynamicWeightedMajority;
import flinkClassifiersTesting.classifiers.dwm.classifiers.bayes.naive.GaussianNaiveBayesClassifier;

import java.util.Map;

public abstract class ClassicDwmProcessFunction extends BaseDwmProcessFunction<ClassicDynamicWeightedMajority<GaussianNaiveBayesClassifier>> {
    public ClassicDwmProcessFunction(String name, String dataset, long bootstrapSamplesLimit, Map<String, Integer> encoder) {
        super(name, dataset, bootstrapSamplesLimit, encoder);
    }

    @Override
    protected void registerClassifier() {
        TypeInformation<ClassicDynamicWeightedMajority<GaussianNaiveBayesClassifier>> classifierInfo = TypeInformation.of(new TypeHint<>() {
        });

        classifierState = getRuntimeContext().getState(new ValueStateDescriptor<>("classicDwmClassifier", classifierInfo));
    }
}
