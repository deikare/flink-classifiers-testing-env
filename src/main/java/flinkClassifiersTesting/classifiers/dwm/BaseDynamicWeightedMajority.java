package flinkClassifiersTesting.classifiers.dwm;

import org.apache.flink.api.java.tuple.Tuple2;
import flinkClassifiersTesting.classifiers.base.BaseClassifierClassifyAndTrain;
import flinkClassifiersTesting.classifiers.base.ClassifierPojo;
import flinkClassifiersTesting.classifiers.dwm.classic.ClassifierInterface;
import flinkClassifiersTesting.classifiers.helpers.Helpers;
import flinkClassifiersTesting.inputs.Example;

import java.time.Instant;
import java.util.ArrayList;
import java.util.ListIterator;
import java.util.NoSuchElementException;

import static flinkClassifiersTesting.classifiers.helpers.Helpers.getIndexOfHighestValue;

public abstract class BaseDynamicWeightedMajority<C extends ClassifierInterface, T extends ClassifierPojo<C>> extends BaseClassifierClassifyAndTrain {
    protected final double beta;
    protected final double threshold;
    protected final int classNumber;
    protected long sampleNumber;
    protected final int updateClassifiersEachSamples;

    protected ArrayList<T> classifiersPojo;

    public BaseDynamicWeightedMajority(double beta, double threshold, int classNumber, int updateClassifiersEachSamples) {
        this.beta = beta;
        this.threshold = threshold;
        this.classNumber = classNumber;
        this.updateClassifiersEachSamples = updateClassifiersEachSamples;
        this.sampleNumber = 0;
        this.classifiersPojo = new ArrayList<>(1);
    }

    @Override
    public void bootstrapTrainImplementation(Example example) {
        if (classifiersPojo.isEmpty())
            classifiersPojo.add(createClassifierWithWeight(1L));

        classifiersPojo.forEach(classifier -> classifier.getClassifier().train(example));
    }

    @Override
    protected ArrayList<Tuple2<String, Long>> trainImplementation(Example example, int predictedClass, ArrayList<Tuple2<String, Long>> performances) {
        if (shouldNormalizeWeightsAndDeleteClassifiers()) {
            normalizeWeightsAndDeleteClassifiersSideEffects();
            performances.addAll(normalizeWeightsAndDeleteClassifiersWithWeightUnderThreshold());
            if (predictedClass != example.getMappedClass()) {
                Instant start = Instant.now();
                classifiersPojo.add(createClassifierWithWeight(sampleNumber));
                performances.add(Tuple2.of(DwmClassifierFields.ADD_CLASSIFIER_DURATION, Helpers.toNow(start)));
                performances.add(Tuple2.of(DwmClassifierFields.ADDED_CLASSIFIERS_COUNT, 1L));
            } else {
                performances.add(Tuple2.of(DwmClassifierFields.ADD_CLASSIFIER_DURATION, 0L));
                performances.add(Tuple2.of(DwmClassifierFields.ADDED_CLASSIFIERS_COUNT, 0L));
            }
        } else {
            performances.add(Tuple2.of(DwmClassifierFields.WEIGHTS_NORMALIZATION_AND_CLASSIFIER_DELETE_DURATION, 0L));
            performances.add(Tuple2.of(DwmClassifierFields.DELETED_CLASSIFIERS_COUNT, -1L));
            performances.add(Tuple2.of(DwmClassifierFields.AVG_CLASSIFIER_TTL, -1L));

            performances.add(Tuple2.of(DwmClassifierFields.ADD_CLASSIFIER_DURATION, 0L));
            performances.add(Tuple2.of(DwmClassifierFields.ADDED_CLASSIFIERS_COUNT, 0L));
        }

        ArrayList<Tuple2<String, Long>> avgLocalPerformances = new ArrayList<>();
        classifiersPojo.forEach(classifier -> {
            ArrayList<Tuple2<String, Long>> localClassifierPerformances = classifier.train(example);
            updateGlobalWithLocalPerformances(localClassifierPerformances, avgLocalPerformances);
        });

        averagePerformanceByLocalClassifier(avgLocalPerformances, classifiersPojo.size());
        performances.addAll(avgLocalPerformances);

        performances.add(Tuple2.of(DwmClassifierFields.CLASSIFIERS_AFTER_TRAIN_COUNT, (long) classifiersPojo.size()));

        return performances;
    }

    protected abstract boolean shouldNormalizeWeightsAndDeleteClassifiers();

    protected abstract void normalizeWeightsAndDeleteClassifiersSideEffects();

    @Override
    protected Tuple2<Integer, ArrayList<Tuple2<String, Long>>> classifyImplementation(Example example) {
        sampleNumber++;
        ArrayList<Tuple2<String, Long>> globalClassifyResults = new ArrayList<>();

        Double[] votesForEachClass = initializeVoteForEachClass();

        long weightsLoweringCount = 0L;

        long correctVotesCount = 0L;
        long wrongVotesCount = 0L;

        for (T classifierPojo : classifiersPojo) {
            Tuple2<Integer, ArrayList<Tuple2<String, Long>>> classifyResults = classifierPojo.classify(example);

            updateGlobalWithLocalPerformances(classifyResults.f1, globalClassifyResults);

            if (classifyResults.f0 == example.getMappedClass())
                correctVotesCount++;
            else {
                wrongVotesCount++;
                weightsLoweringCount = lowerWeightAndReturnWeightLoweringCount(classifierPojo, weightsLoweringCount);
            }

            votesForEachClass[classifyResults.f0] += classifierPojo.getWeight();
        }

        averagePerformanceByLocalClassifier(globalClassifyResults, classifiersPojo.size());

        globalClassifyResults.add(Tuple2.of(DwmClassifierFields.WEIGHTS_LOWERING_COUNT, weightsLoweringCount));
        globalClassifyResults.add(Tuple2.of(DwmClassifierFields.CORRECT_VOTES_COUNT, correctVotesCount));
        globalClassifyResults.add(Tuple2.of(DwmClassifierFields.WRONG_VOTES_COUNT, wrongVotesCount));
        globalClassifyResults.add(Tuple2.of(DwmClassifierFields.CLASSIFIERS_AFTER_CLASSIFICATION_COUNT, (long) classifiersPojo.size()));

        return Tuple2.of(getIndexOfHighestValue(votesForEachClass), globalClassifyResults);
    }

    protected abstract long lowerWeightAndReturnWeightLoweringCount(T classifierPojo, long weightsLoweringCount);

    protected ArrayList<Tuple2<String, Long>> normalizeWeightsAndDeleteClassifiersWithWeightUnderThreshold() {
        ArrayList<Tuple2<String, Long>> performances = new ArrayList<>(3);

        double maxWeight = classifiersPojo.stream().mapToDouble(ClassifierPojo::getWeight).max().orElseThrow(NoSuchElementException::new);
        ListIterator<T> classifierIterator = classifiersPojo.listIterator();
        long deletedCount = 0;
        long deletedTTL = 0;

        Instant start = Instant.now();

        while (classifierIterator.hasNext()) {
            T classifierAndWeight = classifierIterator.next();
            classifierAndWeight.normalizeWeight(maxWeight);
            if (classifierAndWeight.getWeight() < threshold) {
                classifierIterator.remove();
                deletedCount++;
                deletedTTL += sampleNumber - classifierAndWeight.getSampleNumber();
            }
        }

        performances.add(Tuple2.of(DwmClassifierFields.WEIGHTS_NORMALIZATION_AND_CLASSIFIER_DELETE_DURATION, Helpers.toNow(start)));
        performances.add(Tuple2.of(DwmClassifierFields.DELETED_CLASSIFIERS_COUNT, deletedCount));

        if (deletedCount != 0)
            performances.add(Tuple2.of(DwmClassifierFields.AVG_CLASSIFIER_TTL, deletedTTL / deletedCount));
        else performances.add(Tuple2.of(DwmClassifierFields.AVG_CLASSIFIER_TTL, 0L));

        return performances;
    }

    protected static void averagePerformanceByLocalClassifier(ArrayList<Tuple2<String, Long>> globalClassifyResults, int usedClassifiersCount) {
        globalClassifyResults.forEach(measurement -> measurement.f1 /= usedClassifiersCount);
    }

    protected static void updateGlobalWithLocalPerformances(ArrayList<Tuple2<String, Long>> localPerformances, ArrayList<Tuple2<String, Long>> globalPerformances) {
        for (int localMeasurementIndex = 0; localMeasurementIndex < localPerformances.size(); localMeasurementIndex++) {
            if (localMeasurementIndex >= globalPerformances.size())
                globalPerformances.add(localPerformances.get(localMeasurementIndex));
            else {
                Tuple2<String, Long> measurementFromGlobal = globalPerformances.get(localMeasurementIndex);
                measurementFromGlobal.f1 += localPerformances.get(localMeasurementIndex).f1;
            }
        }
    }

    @Override
    public String generateClassifierParams() {
        return "b" + beta + "_t" + threshold + "_u" + updateClassifiersEachSamples;
    }

    protected Double[] initializeVoteForEachClass() {
        Double[] result = new Double[classNumber];
        for (int i = 0; i < classNumber; i++) {
            result[i] = 0.0;
        }

        return result;
    }

    protected abstract C createClassifier();

    protected abstract T createClassifierWithWeight(long sampleNumber);
}
