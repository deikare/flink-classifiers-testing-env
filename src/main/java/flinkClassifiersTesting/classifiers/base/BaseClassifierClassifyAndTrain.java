package flinkClassifiersTesting.classifiers.base;

import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.java.tuple.Tuple3;
import flinkClassifiersTesting.classifiers.helpers.Helpers;
import flinkClassifiersTesting.inputs.Example;

import java.time.Instant;
import java.util.ArrayList;

public abstract class BaseClassifierClassifyAndTrain extends BaseClassifier {
    public ArrayList<Tuple2<String, Long>> train(Example example, int predictedClass, ArrayList<Tuple2<String, Long>> performances) {
        Instant start = Instant.now();
        ArrayList<Tuple2<String, Long>> trainingPerformance = trainImplementation(example, predictedClass, performances);
        trainingPerformance.add(Tuple2.of(BaseClassifierFields.TRAINING_DURATION, Helpers.toNow(start)));
        return trainingPerformance;
    }

    public Tuple3<String, Integer, ArrayList<Tuple2<String, Long>>> classify(Example example) {
        Instant start = Instant.now();
        Tuple2<Integer, ArrayList<Tuple2<String, Long>>> predictionResult = classifyImplementation(example);
        predictionResult.f1.add(Tuple2.of(BaseClassifierFields.CLASSIFICATION_DURATION, Helpers.toNow(start)));
        return new Tuple3<>(Helpers.timestampTrailingZeros(start), predictionResult.f0, predictionResult.f1);
    }

    protected abstract ArrayList<Tuple2<String, Long>> trainImplementation(Example example, int predictedClass, ArrayList<Tuple2<String, Long>> performances);

    protected abstract Tuple2<Integer, ArrayList<Tuple2<String, Long>>> classifyImplementation(Example example);
}
