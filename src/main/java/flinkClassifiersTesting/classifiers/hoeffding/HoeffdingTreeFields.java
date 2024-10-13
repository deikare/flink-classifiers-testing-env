package flinkClassifiersTesting.classifiers.hoeffding;

import flinkClassifiersTesting.classifiers.base.BaseClassifierFields;

public class HoeffdingTreeFields extends BaseClassifierFields {
    public static final String NODES_DURING_TRAVERSAL_COUNT = "nodesDuringTraversalCount";
    public static final String DURING_TRAVERSAL_DURATION = "duringTraversalDuration";
    public static final String LEAF_SPLIT = "leafSplit"; //tri state - -1 - n < nMin, 0 not split, 1 - split on h, 2 - split on tau
    public static final String TREE_SIZE = "treeSize";
}
