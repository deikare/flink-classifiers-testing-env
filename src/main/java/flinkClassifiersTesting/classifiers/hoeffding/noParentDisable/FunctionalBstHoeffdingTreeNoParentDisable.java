package flinkClassifiersTesting.classifiers.hoeffding.noParentDisable;

import flinkClassifiersTesting.classifiers.bstHoeffding.functional.FunctionalBstHoeffdingTree;
import flinkClassifiersTesting.classifiers.bstHoeffding.functional.NaiveBayesNodeStatistics;
import flinkClassifiersTesting.classifiers.hoeffding.StatisticsBuilderInterface;

public abstract class FunctionalBstHoeffdingTreeNoParentDisable<N_S extends NaiveBayesNodeStatistics, B extends StatisticsBuilderInterface<N_S>> extends FunctionalBstHoeffdingTree<N_S, B> {
    public FunctionalBstHoeffdingTreeNoParentDisable(int classesNumber, double delta, int attributesNumber, double tau, long nMin, B statisticsBuilder) {
        super(classesNumber, delta, attributesNumber, tau, nMin, statisticsBuilder);
    }

    @Override
    protected boolean disableAttribute() {
        return false;
    }
}
