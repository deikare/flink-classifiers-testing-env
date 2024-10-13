package flinkClassifiersTesting.inputs;

abstract class BaseExample {
    protected double[] attributes;

    public BaseExample() {
    }

    public double[] getAttributes() {
        return attributes;
    }

    public void setAttributes(double[] attributes) {
        this.attributes = attributes;
    }

    public long getId() {
        return 0L;
    }
}
