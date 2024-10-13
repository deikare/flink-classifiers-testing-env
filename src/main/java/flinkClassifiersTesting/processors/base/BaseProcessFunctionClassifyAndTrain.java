package flinkClassifiersTesting.processors.base;

import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.api.java.tuple.Tuple4;
import flinkClassifiersTesting.classifiers.base.BaseClassifierClassifyAndTrain;
import flinkClassifiersTesting.inputs.Example;

import java.util.ArrayList;
import java.util.Map;

public abstract class BaseProcessFunctionClassifyAndTrain<C extends BaseClassifierClassifyAndTrain> extends BaseProcessFunction<C> {
    public BaseProcessFunctionClassifyAndTrain(String name, String dataset, long bootstrapSamplesLimit, Map<String, Integer> encoder) {
        super(name, dataset, bootstrapSamplesLimit, encoder);
    }

    @Override
    protected Tuple4<String, Integer, ArrayList<Tuple2<String, Long>>, C> processExample(Example example, C classifier) {
        Tuple3<String, Integer, ArrayList<Tuple2<String, Long>>> classifyResults = classifier.classify(example);
        ArrayList<Tuple2<String, Long>> trainingResults = classifier.train(example, classifyResults.f1, classifyResults.f2);
        return new Tuple4<>(classifyResults.f0, classifyResults.f1, trainingResults, classifier);
    }
}
