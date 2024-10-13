package flinkClassifiersTesting.processors.factory.vfdt;

import flinkClassifiersTesting.processors.factory.ClassifierParamsInterface;

import java.util.Objects;

public class VfdtClassifierParams implements ClassifierParamsInterface {
    public double delta;
    public double tau;
    public long nMin;

    public VfdtClassifierParams() {
    }

    public VfdtClassifierParams(double delta, double tau, long nMin) {
        this.delta = delta;
        this.tau = tau;
        this.nMin = nMin;
    }

    @Override
    public String toString() {
        return "{" +
                "delta=" + delta +
                ", tau=" + tau +
                ", nMin=" + nMin +
                '}';
    }

    @Override
    public String directoryName() {
        return "delta" + delta + "_tau" + tau + "_nMin" + nMin;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VfdtClassifierParams that = (VfdtClassifierParams) o;
        return Double.compare(delta, that.delta) == 0 && Double.compare(tau, that.tau) == 0 && nMin == that.nMin;
    }

    @Override
    public int hashCode() {
        return Objects.hash(delta, tau, nMin);
    }
}
