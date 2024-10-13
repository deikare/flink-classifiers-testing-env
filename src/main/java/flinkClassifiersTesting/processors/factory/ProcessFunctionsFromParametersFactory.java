package flinkClassifiersTesting.processors.factory;

import flinkClassifiersTesting.classifiers.base.BaseClassifier;
import flinkClassifiersTesting.processors.base.BaseProcessFunction;

import java.util.List;
import java.util.Map;

public abstract class ProcessFunctionsFromParametersFactory<T, C extends BaseClassifier, P extends BaseProcessFunction<C>> {
    private final String name;

    private final List<T> parameters;

    public ProcessFunctionsFromParametersFactory(String name, List<T> parameters) {
        this.name = name;
        this.parameters = parameters;
    }

    public String getName() {
        return name;
    }

    public List<T> getParameters() {
        return parameters;
    }

    public abstract P createProcessFunction(T params, int classNumber, int attributesNumber, String dataset, long samplesLimit, Map<String, Integer> classEncoder);
}
