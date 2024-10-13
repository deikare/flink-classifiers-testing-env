package flinkClassifiersTesting.classifiers.bstHoeffding.standard;

import org.apache.flink.api.java.tuple.Tuple2;
import flinkClassifiersTesting.classifiers.bstHoeffding.bstStatistics.BstHoeffdingTreeStatistics;
import flinkClassifiersTesting.classifiers.hoeffding.*;
import flinkClassifiersTesting.inputs.Example;

import java.util.ArrayList;

public abstract class BstHoeffdingTree<N_S extends NodeStatistics, B extends StatisticsBuilderInterface<N_S>> extends HoeffdingTree<N_S, B> {
    protected final BstHoeffdingTreeStatistics totalStatistics;

    public BstHoeffdingTree(int classesNumber, double delta, int attributesNumber, double tau, long nMin, B statisticsBuilder) {
        super(classesNumber, delta, attributesNumber, tau, nMin, statisticsBuilder);
        totalStatistics = new BstHoeffdingTreeStatistics(classesNumber, attributesNumber);
    }

    @Override
    protected void updateLeaf(Example example, Node<N_S, B> leaf, ArrayList<Tuple2<String, Long>> performances) {
        leaf.updateStatistics(example);
        totalStatistics.updateStatistics(example);

        if (leaf.getN() > nMin) {
            leaf.resetN();
            double eps = getEpsilon();

            HighestHeuristicPOJO pojo = new HighestHeuristicPOJO(leaf);

            if (pojo.xA == null) {
                String msg = "Hoeffding test showed no attributes";
                logger.info(msg);
                throw new RuntimeException(msg);
            } else if (pojo.hXa != null && pojo.hXb != null && (pojo.hXa - pojo.hXb > eps)) {
                logger.info("Heuristic value is correspondingly higher, splitting");
                findSplittingValueAndSplit(example, leaf, pojo.xA);
                performances.add(Tuple2.of(HoeffdingTreeFields.LEAF_SPLIT, 1L));
                treeSize += 2L;
            } else if (eps < tau) {
                logger.info("Epsilon is lower than tau, splitting");
                findSplittingValueAndSplit(example, leaf, pojo.xA);
                performances.add(Tuple2.of(HoeffdingTreeFields.LEAF_SPLIT, 2L));
                treeSize += 2L;
            } else {
                logger.info("No split");
                performances.add(Tuple2.of(HoeffdingTreeFields.LEAF_SPLIT, 0L));
            }
        } else {
            logger.info("Not enough samples to test splits");
            performances.add(Tuple2.of(HoeffdingTreeFields.LEAF_SPLIT, -1L));
        }

        performances.add(Tuple2.of(HoeffdingTreeFields.TREE_SIZE, treeSize));
    }

    private void findSplittingValueAndSplit(Example example, Node<N_S, B> leaf, int splittingAttributeIndex) {
        double splittingValue = totalStatistics.getSplittingValue(splittingAttributeIndex, n);
        leaf.split(splittingAttributeIndex, splittingValue, statisticsBuilder, example, disableAttribute());
    }
}
