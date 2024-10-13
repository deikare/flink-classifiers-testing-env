package flinkClassifiersTesting.classifiers.dwm.extended;

import flinkClassifiersTesting.classifiers.dwm.BaseDynamicWeightedMajority;
import flinkClassifiersTesting.classifiers.dwm.classic.ClassifierInterface;

public abstract class ExtendedDynamicWeightedMajority<C extends ClassifierInterface> extends BaseDynamicWeightedMajority<C, ClassifierPojoExtended<C>> {

    public ExtendedDynamicWeightedMajority(double beta, double threshold, int classNumber, int updateClassifiersEachSamples) {
        super(beta, threshold, classNumber, updateClassifiersEachSamples);
    }

    @Override
    protected ClassifierPojoExtended<C> createClassifierWithWeight(long sampleNumber) {
        return new ClassifierPojoExtended<>(createClassifier(), sampleNumber);
    }

    @Override
    protected boolean shouldNormalizeWeightsAndDeleteClassifiers() {
        return sampleNumber % updateClassifiersEachSamples == 0;
    }

    @Override
    protected void normalizeWeightsAndDeleteClassifiersSideEffects() {

    }

    @Override
    protected long lowerWeightAndReturnWeightLoweringCount(ClassifierPojoExtended<C> classifierPojo, long weightsLoweringCount) {
        classifierPojo.incWrongClassificationCounter();
        if (classifierPojo.getWrongClassificationsCounter() != 0 && classifierPojo.getWrongClassificationsCounter() % updateClassifiersEachSamples == 0) {
            classifierPojo.lowerWeight(beta);
            weightsLoweringCount++;
        }
        return weightsLoweringCount;
    }
}
