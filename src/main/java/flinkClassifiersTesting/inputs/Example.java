package flinkClassifiersTesting.inputs;

import java.util.Arrays;

public class Example extends BaseExample {
    private int mappedClass;

    public Example(int mappedClass, double[] attributes) {
        this.mappedClass = mappedClass;
        this.attributes = attributes;
    }

    public int getMappedClass() {
        return mappedClass;
    }


    @Override
    public String toString() {
        return "Example{" +
                "mappedClass='" + mappedClass + '\'' +
                ", attributes=" + Arrays.toString(attributes) +
                '}';
    }

}
