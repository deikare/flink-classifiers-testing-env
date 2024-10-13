package flinkClassifiersTesting.processors.base;

import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.java.tuple.Tuple4;
import flinkClassifiersTesting.classifiers.base.BaseClassifierTrainAndClassify;
import flinkClassifiersTesting.inputs.Example;

import java.util.ArrayList;
import java.util.Map;

public abstract class BaseProcessFunctionTrainAndClassify<C extends BaseClassifierTrainAndClassify> extends BaseProcessFunction<C> {
    public BaseProcessFunctionTrainAndClassify(String name, String dataset, long bootstrapSamplesLimit, Map<String, Integer> encoder) {
        super(name, dataset, bootstrapSamplesLimit, encoder);
    }

    @Override
    protected Tuple4<String, Integer, ArrayList<Tuple2<String, Long>>, C> processExample(Example example, C classifier) {
        Tuple2<String, ArrayList<Tuple2<String, Long>>> trainingResult = classifier.train(example);
        Tuple2<Integer, ArrayList<Tuple2<String, Long>>> classifyResult = classifier.classify(example, trainingResult.f1);
        return new Tuple4<>(trainingResult.f0, classifyResult.f0, classifyResult.f1, classifier);
    }
}
