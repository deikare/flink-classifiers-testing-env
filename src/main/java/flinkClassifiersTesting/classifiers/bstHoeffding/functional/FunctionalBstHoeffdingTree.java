package flinkClassifiersTesting.classifiers.bstHoeffding.functional;

import org.apache.flink.api.java.tuple.Tuple2;
import flinkClassifiersTesting.classifiers.bstHoeffding.standard.BstHoeffdingTree;
import flinkClassifiersTesting.classifiers.hoeffding.Node;
import flinkClassifiersTesting.classifiers.hoeffding.StatisticsBuilderInterface;
import flinkClassifiersTesting.inputs.Example;

import java.util.ArrayList;

public abstract class FunctionalBstHoeffdingTree<N_S extends NaiveBayesNodeStatistics, B extends StatisticsBuilderInterface<N_S>> extends BstHoeffdingTree<N_S, B> {
    public FunctionalBstHoeffdingTree(int classesNumber, double delta, int attributesNumber, double tau, long nMin, B statisticsBuilder) {
        super(classesNumber, delta, attributesNumber, tau, nMin, statisticsBuilder);
    }

    @Override
    protected Tuple2<Integer, ArrayList<Tuple2<String, Long>>> classifyImplementation(Example example, ArrayList<Tuple2<String, Long>> performances) throws RuntimeException {
        Node<N_S, B> leaf = getLeaf(example, root);
        int predictedClass = leaf.getStatistics().getMajorityClass(example, totalStatistics.getAttributeCountsTrees());
        logger.info(example + " predicted with " + predictedClass);
        return new Tuple2<>(predictedClass, performances);
    }
}
