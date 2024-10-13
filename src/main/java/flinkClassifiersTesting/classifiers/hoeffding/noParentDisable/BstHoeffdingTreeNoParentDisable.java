package flinkClassifiersTesting.classifiers.hoeffding.noParentDisable;

import flinkClassifiersTesting.classifiers.bstHoeffding.standard.BstHoeffdingTree;
import flinkClassifiersTesting.classifiers.hoeffding.NodeStatistics;
import flinkClassifiersTesting.classifiers.hoeffding.StatisticsBuilderInterface;

public abstract class BstHoeffdingTreeNoParentDisable<N_S extends NodeStatistics, B extends StatisticsBuilderInterface<N_S>> extends BstHoeffdingTree<N_S, B> {
    public BstHoeffdingTreeNoParentDisable(int classesNumber, double delta, int attributesNumber, double tau, long nMin, B statisticsBuilder) {
        super(classesNumber, delta, attributesNumber, tau, nMin, statisticsBuilder);
    }

    @Override
    protected boolean disableAttribute() {
        return false;
    }
}
