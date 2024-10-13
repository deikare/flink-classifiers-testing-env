package flinkClassifiersTesting.classifiers.hoeffding.noParentDisable;

import flinkClassifiersTesting.classifiers.hoeffding.*;

public abstract class HoeffdingTreeNoParentDisable<N_S extends NodeStatistics, B extends StatisticsBuilderInterface<N_S>> extends HoeffdingTree<N_S, B> {
    public HoeffdingTreeNoParentDisable(int classesNumber, double delta, int attributesNumber, double tau, long nMin, B statisticsBuilder) {
        super(classesNumber, delta, attributesNumber, tau, nMin, statisticsBuilder);
    }

    @Override
    protected boolean disableAttribute() {
        return false;
    }
}
