package flinkClassifiersTesting.classifiers.dwm.classic;

import org.apache.flink.api.java.tuple.Tuple2;
import flinkClassifiersTesting.inputs.Example;

import java.util.ArrayList;

public interface ClassifierInterface {
    Tuple2<Integer, ArrayList<Tuple2<String, Long>>> classify(Example example); // each classifier should return same metrics

    ArrayList<Tuple2<String, Long>> train(Example example);
}
