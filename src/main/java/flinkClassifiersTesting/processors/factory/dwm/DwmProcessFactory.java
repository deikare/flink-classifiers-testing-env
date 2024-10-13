package flinkClassifiersTesting.processors.factory.dwm;

import flinkClassifiersTesting.classifiers.dwm.classic.ClassicDynamicWeightedMajority;
import flinkClassifiersTesting.classifiers.dwm.classifiers.bayes.naive.GaussianNaiveBayesClassifier;
import flinkClassifiersTesting.classifiers.dwm.extended.ExtendedDynamicWeightedMajority;
import flinkClassifiersTesting.processors.dwm.ClassicDwmProcessFunction;
import flinkClassifiersTesting.processors.dwm.ExtendedDwmProcessFunction;
import flinkClassifiersTesting.processors.factory.ProcessFunctionsFromParametersFactory;

import java.util.List;
import java.util.Map;

public class DwmProcessFactory {
    public static ProcessFunctionsFromParametersFactory<DwmClassifierParams, ClassicDynamicWeightedMajority<GaussianNaiveBayesClassifier>, ClassicDwmProcessFunction> dwm(List<DwmClassifierParams> parameters) {
        String name = "dwm";

        return new ProcessFunctionsFromParametersFactory<>(name, parameters) {
            @Override
            public ClassicDwmProcessFunction createProcessFunction(DwmClassifierParams params, int classNumber, int attributesNumber, String dataset, long samplesLimit, Map<String, Integer> classEncoder) {
                return new ClassicDwmProcessFunction(name, dataset, samplesLimit, classEncoder) {
                    @Override
                    protected ClassicDynamicWeightedMajority<GaussianNaiveBayesClassifier> createClassifier() {
                        return new ClassicDynamicWeightedMajority<>(params.beta, params.threshold, classNumber, params.updateClassifiersEachSamples) {
                            @Override
                            protected GaussianNaiveBayesClassifier createClassifier() {
                                return new GaussianNaiveBayesClassifier(classNumber, attributesNumber);
                            }
                        };
                    }
                };
            }
        };
    }

    public static ProcessFunctionsFromParametersFactory<DwmClassifierParams, ExtendedDynamicWeightedMajority<GaussianNaiveBayesClassifier>, ExtendedDwmProcessFunction> extendedDwm(List<DwmClassifierParams> parameters) {
        String name = "extendedDwm";

        return new ProcessFunctionsFromParametersFactory<>(name, parameters) {
            @Override
            public ExtendedDwmProcessFunction createProcessFunction(DwmClassifierParams params, int classNumber, int attributesNumber, String dataset, long samplesLimit, Map<String, Integer> classEncoder) {
                return new ExtendedDwmProcessFunction(name, dataset, samplesLimit, classEncoder) {
                    @Override
                    protected ExtendedDynamicWeightedMajority<GaussianNaiveBayesClassifier> createClassifier() {
                        return new ExtendedDynamicWeightedMajority<>(params.beta, params.threshold, classNumber, params.updateClassifiersEachSamples) {
                            @Override
                            protected GaussianNaiveBayesClassifier createClassifier() {
                                return new GaussianNaiveBayesClassifier(classNumber, attributesNumber);
                            }
                        };
                    }
                };
            }
        };
    }

}
