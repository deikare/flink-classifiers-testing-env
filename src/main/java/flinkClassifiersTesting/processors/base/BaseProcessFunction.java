package flinkClassifiersTesting.processors.base;

import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.api.common.typeinfo.TypeHint;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.java.tuple.Tuple4;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.KeyedProcessFunction;
import org.apache.flink.util.Collector;
import flinkClassifiersTesting.classifiers.base.BaseClassifier;
import flinkClassifiersTesting.inputs.Example;
import flinkClassifiersTesting.inputs.PlainExample;
import flinkClassifiersTesting.processors.dto.ClassificationResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class BaseProcessFunction<C extends BaseClassifier> extends KeyedProcessFunction<Long, PlainExample, ClassificationResult> {
    protected transient ValueState<C> classifierState;
    protected transient ValueState<Long> sampleNumberState;

    protected Map<String, Integer> encoder;
    protected Map<Integer, String> decoder;

    protected String name;
    protected String dataset;
    protected long bootstrapSamplesLimit;

    public BaseProcessFunction(String name, String dataset, long bootstrapSamplesLimit, Map<String, Integer> encoder) {
        this.name = name;
        this.dataset = dataset;
        this.bootstrapSamplesLimit = bootstrapSamplesLimit;

        this.encoder = encoder;

        decoder = new HashMap<>(encoder.size());
        encoder.forEach((plainClass, mappedClass) -> decoder.put(mappedClass, plainClass));
    }


    @Override
    public void processElement(PlainExample plainExample, KeyedProcessFunction<Long, PlainExample, ClassificationResult>.Context context, Collector<ClassificationResult> collector) throws Exception {
        Long sampleNumber = sampleNumberState.value();
        if (sampleNumber == null)
            sampleNumber = 1L;
        else sampleNumber++;

        sampleNumberState.update(sampleNumber);

        int mappedClass = encoder.get(plainExample.getPlainClass());
        Example example = new Example(mappedClass, plainExample.getAttributes());

        C classifier = classifierState.value();
        if (classifier == null)
            classifier = createClassifier();

        if (sampleNumber <= bootstrapSamplesLimit)
            classifier.bootstrapTrainImplementation(example);
        else {
            Tuple4<String, Integer, ArrayList<Tuple2<String, Long>>, C> results = processExample(example, classifier);
            collector.collect(new ClassificationResult(results.f0, plainExample.getPlainClass(), decoder.get(results.f1), classifier.generateClassifierParams(), results.f2));
        }

        classifierState.update(classifier);
    }

    protected abstract Tuple4<String, Integer, ArrayList<Tuple2<String, Long>>, C> processExample(Example example, C classifier);

    protected abstract C createClassifier();


    @Override
    public void open(Configuration parameters) throws Exception {
        registerClassifier();
        ValueStateDescriptor<Long> sampleNumberDescriptor = new ValueStateDescriptor<>("sampleNumber", TypeInformation.of(new TypeHint<>() {
        }));
        sampleNumberState = getRuntimeContext().getState(sampleNumberDescriptor);
    }

    protected abstract void registerClassifier(); //its abstract because TypeInfo cannot be templated

    public abstract List<String> csvColumnsHeader();
}
