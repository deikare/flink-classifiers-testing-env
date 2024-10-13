package flinkClassifiersTesting.processors.dto;

import org.apache.flink.api.java.tuple.Tuple2;

import java.io.Serializable;
import java.util.ArrayList;

public class ClassificationResult implements Serializable {
    public String timestamp;

    public String exampleClass;
    public String predicted;


    public String params;
    public ArrayList<Tuple2<String, Long>> performances;

    public ClassificationResult() {
    }

    public ClassificationResult(String timestamp, String exampleClass, String predicted, String params, ArrayList<Tuple2<String, Long>> performances) {
        this.timestamp = timestamp;
        this.exampleClass = exampleClass;
        this.predicted = predicted;
        this.params = params;
        this.performances = performances;
    }

}
