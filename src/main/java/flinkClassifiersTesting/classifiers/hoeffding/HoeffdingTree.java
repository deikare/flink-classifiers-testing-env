package flinkClassifiersTesting.classifiers.hoeffding;

import org.apache.flink.api.java.tuple.Tuple2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import flinkClassifiersTesting.classifiers.base.BaseClassifierTrainAndClassify;
import flinkClassifiersTesting.classifiers.helpers.Helpers;
import flinkClassifiersTesting.inputs.Example;

import java.time.Instant;
import java.util.*;

public abstract class HoeffdingTree<N_S extends NodeStatistics, B extends StatisticsBuilderInterface<N_S>> extends BaseClassifierTrainAndClassify {
    protected final Logger logger = LoggerFactory.getLogger(HoeffdingTree.class);
    protected final long nMin;

    private final int R;
    private final double delta;
    private final int attributesNumber;
    protected final double tau;
    protected Node<N_S, B> root;
    protected final B statisticsBuilder;
    protected long n = 0L;
    protected long treeSize;


    public HoeffdingTree(int classesNumber, double delta, int attributesNumber, double tau, long nMin, B statisticsBuilder) {
        if (classesNumber <= 0)
            throw new RuntimeException("wrong classes number: " + classesNumber);
        if (attributesNumber <= 0)
            throw new RuntimeException("wrong attributes number: " + attributesNumber);
        this.R = (int) (Math.log(classesNumber) / Math.log(2));
        this.delta = delta;
        this.attributesNumber = attributesNumber;
        this.tau = tau;
        this.nMin = nMin;
        this.statisticsBuilder = statisticsBuilder;
        this.root = new Node<>(statisticsBuilder, null);
        treeSize = 1L;
    }

    protected Tuple2<Integer, ArrayList<Tuple2<String, Long>>> classifyImplementation(Example example, ArrayList<Tuple2<String, Long>> performances) throws RuntimeException {
        Node<N_S, B> leaf = getLeaf(example, root);
        int predictedClass = leaf.getMajorityClass(example);
        logger.info(example + " predicted with " + predictedClass);
        return new Tuple2<>(predictedClass, performances);
    }

    @Override
    public String generateClassifierParams() {
        return "r" + R + "_d" + delta + "_t" + tau + "_n" + nMin;
    }

    @Override
    public void bootstrapTrainImplementation(Example example) {
        n++;
        Node<N_S, B> leaf = root;

        while (!leaf.isLeaf())
            leaf = leaf.getChild(example);

        updateLeaf(example, leaf, new ArrayList<>());
    }

    protected void updateLeaf(Example example, Node<N_S, B> leaf, ArrayList<Tuple2<String, Long>> performances) {
        leaf.updateStatistics(example);

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
                leaf.split(pojo.xA, statisticsBuilder, example, disableAttribute());
                performances.add(Tuple2.of(HoeffdingTreeFields.LEAF_SPLIT, 1L));
                treeSize += 2L;
            } else if (eps < tau) {
                logger.info("Epsilon is lower than tau, splitting");
                leaf.split(pojo.xA, statisticsBuilder, example, disableAttribute());
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

    protected boolean disableAttribute() {
        return attributesNumber != 1;
    }

    protected ArrayList<Tuple2<String, Long>> trainImplementation(Example example) throws RuntimeException {
        n++;
        ArrayList<Tuple2<String, Long>> trainingPerformances = new ArrayList<>();
        Node<N_S, B> leaf = getLeaf(example, trainingPerformances);
        logger.info("Training: " + example.toString());

        updateLeaf(example, leaf, trainingPerformances);

        logger.info(leaf.getStatistics().toString());

        return trainingPerformances;
    }

    protected Node<N_S, B> getLeaf(Example example, ArrayList<Tuple2<String, Long>> performances) {
        Instant start = Instant.now();
        long count = 1;
        Node<N_S, B> result = root;
        while (!(result.isLeaf())) {
            count++;
            result = result.getChild(example);
        }
        performances.add(Tuple2.of(HoeffdingTreeFields.NODES_DURING_TRAVERSAL_COUNT, count));
        performances.add(Tuple2.of(HoeffdingTreeFields.DURING_TRAVERSAL_DURATION, Helpers.toNow(start)));
        return result;
    }

    protected Node<N_S, B> getLeaf(Example example, Node<N_S, B> rootLeaf) {
        Node<N_S, B> result = rootLeaf;
        while (!(result.isLeaf()))
            result = result.getChild(example);

        return result;
    }

    protected double getEpsilon() {
        return ((double) R) * Math.sqrt(Math.log(1 / delta) / (2 * n));
    }

    protected class HighestHeuristicPOJO {
        public Integer xA;
        private Integer xB;
        public Double hXa;
        public Double hXb;


        public HighestHeuristicPOJO(Node<N_S, B> node) {
            hXb = null;
            hXa = null;
            xA = null;
            xB = null;

            Integer disabledAttributeIndex = node.getDisabledAttributeIndex();

            if (disabledAttributeIndex == null)
                findXaAndXb(0, attributesNumber, node);
            else {
                findXaAndXb(0, disabledAttributeIndex, node);
                findXaAndXb(disabledAttributeIndex + 1, attributesNumber, node);
            }
        }

        void findXaAndXb(int startIndex, int endIndex, Node<N_S, B> node) {
            for (int i = startIndex; i < endIndex; i++) {
                double h = heuristic(i, node);
                if (xA == null || h > hXa) {
                    xA = i;
                    hXa = h;
                } else if (xB == null || h > hXb) {
                    xB = i;
                    hXb = h;
                }
            }
        }
    }

    protected abstract double heuristic(int attributeIndex, Node<N_S, B> node);
}
