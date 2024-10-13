package flinkClassifiersTesting.processors.factory.vfdt;

import flinkClassifiersTesting.classifiers.bstHoeffding.functional.FunctionalBstHoeffdingTree;
import flinkClassifiersTesting.classifiers.bstHoeffding.functional.NaiveBayesNodeStatistics;
import flinkClassifiersTesting.classifiers.bstHoeffding.functional.NaiveBayesNodeStatisticsBuilder;
import flinkClassifiersTesting.classifiers.bstHoeffding.standard.BstHoeffdingTree;
import flinkClassifiersTesting.classifiers.driftDetection.WindowedDetectorBstHoeffdingTree;
import flinkClassifiersTesting.classifiers.driftDetection.WindowedDetectorHoeffdingTree;
import flinkClassifiersTesting.classifiers.hoeffding.*;
import flinkClassifiersTesting.classifiers.hoeffding.noParentDisable.BstHoeffdingTreeNoParentDisable;
import flinkClassifiersTesting.classifiers.hoeffding.noParentDisable.FunctionalBstHoeffdingTreeNoParentDisable;
import flinkClassifiersTesting.classifiers.hoeffding.noParentDisable.HoeffdingTreeNoParentDisable;
import flinkClassifiersTesting.processors.factory.ProcessFunctionsFromParametersFactory;
import flinkClassifiersTesting.processors.hoeffding.*;
import flinkClassifiersTesting.processors.hoeffding.noParentDisable.FunctionalVfdtBstNoParentDisableProcessFunction;
import flinkClassifiersTesting.processors.hoeffding.noParentDisable.VfdtBstNoParentDisableProcessFunction;
import flinkClassifiersTesting.processors.hoeffding.noParentDisable.VfdtGaussianNaiveBayesNoParentDisableProcessFunction;
import flinkClassifiersTesting.processors.hoeffding.noParentDisable.VfdtNoParentDisableProcessFunction;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VfdtProcessFactory {
    public static ProcessFunctionsFromParametersFactory<VfdtClassifierParams, HoeffdingTree<SimpleNodeStatistics, SimpleNodeStatisticsBuilder>, VfdtProcessFunction> vfdt(List<VfdtClassifierParams> parameters) {
        String name = "vfdt";
        return new ProcessFunctionsFromParametersFactory<>(name, parameters) {
            @Override
            public VfdtProcessFunction createProcessFunction(VfdtClassifierParams params, int classNumber, int attributesNumber, String dataset, long samplesLimit, Map<String, Integer> classEncoder) {
                return new VfdtProcessFunction(name, dataset, samplesLimit, classEncoder) {
                    @Override
                    protected HoeffdingTree<SimpleNodeStatistics, SimpleNodeStatisticsBuilder> createClassifier() {

                        SimpleNodeStatisticsBuilder statisticsBuilder = new SimpleNodeStatisticsBuilder(classNumber, attributesNumber);
                        return new HoeffdingTree<>(classNumber, params.delta, attributesNumber, params.tau, params.nMin, statisticsBuilder) {
                            @Override
                            protected double heuristic(int attributeNumber, Node<SimpleNodeStatistics, SimpleNodeStatisticsBuilder> node) {
                                double threshold = 0.5;
                                return Math.abs(threshold - node.getStatistics().getSplittingValue(attributeNumber)) / threshold;
                            }
                        };
                    }
                };
            }
        };
    }

    public static ProcessFunctionsFromParametersFactory<VfdtClassifierParams, BstHoeffdingTree<SimpleNodeStatistics, SimpleNodeStatisticsBuilder>, VfdtBstProcessFunction> vfdtBst(List<VfdtClassifierParams> parameters) {
        String name = "vfdtBst";
        return new ProcessFunctionsFromParametersFactory<>(name, parameters) {
            @Override
            public VfdtBstProcessFunction createProcessFunction(VfdtClassifierParams params, int classNumber, int attributesNumber, String dataset, long samplesLimit, Map<String, Integer> classEncoder) {
                return new VfdtBstProcessFunction(name, dataset, samplesLimit, classEncoder) {
                    @Override
                    protected BstHoeffdingTree<SimpleNodeStatistics, SimpleNodeStatisticsBuilder> createClassifier() {
                        SimpleNodeStatisticsBuilder statisticsBuilder = new SimpleNodeStatisticsBuilder(classNumber, attributesNumber);

                        return new BstHoeffdingTree<>(classNumber, params.delta, attributesNumber, params.tau, params.nMin, statisticsBuilder) {
                            @Override
                            protected double heuristic(int attributeIndex, Node<SimpleNodeStatistics, SimpleNodeStatisticsBuilder> node) {
                                double threshold = 0.5;
                                return Math.abs(threshold - node.getStatistics().getSplittingValue(attributeIndex)) / threshold;
                            }
                        };
                    }
                };
            }
        };
    }

    public static ProcessFunctionsFromParametersFactory<VfdtClassifierParams, FunctionalBstHoeffdingTree<NaiveBayesNodeStatistics, NaiveBayesNodeStatisticsBuilder>, FunctionalVfdtBstProcessFunction> vfdtBstNb(List<VfdtClassifierParams> parameters) {
        String name = "vfdtBstNb";
        return new ProcessFunctionsFromParametersFactory<>(name, parameters) {
            @Override
            public FunctionalVfdtBstProcessFunction createProcessFunction(VfdtClassifierParams params, int classNumber, int attributesNumber, String dataset, long samplesLimit, Map<String, Integer> classEncoder) {
                return new FunctionalVfdtBstProcessFunction(name, dataset, samplesLimit, classEncoder) {
                    @Override
                    protected FunctionalBstHoeffdingTree<NaiveBayesNodeStatistics, NaiveBayesNodeStatisticsBuilder> createClassifier() {
                        NaiveBayesNodeStatisticsBuilder statisticsBuilder = new NaiveBayesNodeStatisticsBuilder(classNumber, attributesNumber, 10);
                        return new FunctionalBstHoeffdingTree<>(classNumber, params.delta, attributesNumber, params.tau, params.nMin, statisticsBuilder) {
                            @Override
                            protected double heuristic(int attributeIndex, Node<NaiveBayesNodeStatistics, NaiveBayesNodeStatisticsBuilder> node) {
                                double threshold = 0.5;
                                return Math.abs(threshold - node.getStatistics().getSplittingValue(attributeIndex)) / threshold;
                            }
                        };
                    }
                };
            }
        };
    }

    public static ProcessFunctionsFromParametersFactory<VfdtClassifierParams, HoeffdingTree<SimpleNodeStatistics, SimpleNodeStatisticsBuilder>, VfdtProcessFunction> vfdtEntropy(List<VfdtClassifierParams> parameters) {
        String name = "vfdtEntropy";
        return new ProcessFunctionsFromParametersFactory<>(name, parameters) {
            @Override
            public VfdtProcessFunction createProcessFunction(VfdtClassifierParams params, int classNumber, int attributesNumber, String dataset, long samplesLimit, Map<String, Integer> classEncoder) {
                return new VfdtProcessFunction(name, dataset, samplesLimit, classEncoder) {
                    @Override
                    protected HoeffdingTree<SimpleNodeStatistics, SimpleNodeStatisticsBuilder> createClassifier() {
                        SimpleNodeStatisticsBuilder statisticsBuilder = new SimpleNodeStatisticsBuilder(classNumber, attributesNumber);
                        return new HoeffdingTree<>(classNumber, params.delta, attributesNumber, params.tau, params.nMin, statisticsBuilder) {
                            @Override
                            protected double heuristic(int attributeIndex, Node<SimpleNodeStatistics, SimpleNodeStatisticsBuilder> node) {
                                double examplesInfo = 0.0;
//
                                Map<Double, Long> valueCounts = new HashMap<>();
                                List<Map<Double, Long>> existingCounts = node.getStatistics().getAttributeValueCounts().get(attributeIndex);
                                existingCounts.forEach(valuesMap -> valuesMap.forEach((attributeValue, count) -> valueCounts.compute(attributeValue, (key, existingCount) -> (existingCount == null) ? count : existingCount + count)));
//
                                double attributeInfo = 0.0;
                                double base = Math.log(2);
                                for (Map.Entry<Double, Long> flattenedEntry : valueCounts.entrySet()) {
                                    double sumForEachClass = 0.0;

                                    for (int classIndex = 0; classIndex < classNumber; classIndex++) {
                                        double prob = ((double) existingCounts.get(classIndex).getOrDefault(flattenedEntry.getKey(), 0L)) / ((double) node.getClassCount(classIndex));
                                        sumForEachClass += prob * Math.log(prob) / base;
                                    }

                                    attributeInfo -= ((double) flattenedEntry.getValue()) / ((double) node.getN()) * sumForEachClass;
                                }

                                return examplesInfo - attributeInfo;
                            }
                        };
                    }
                };
            }
        };
    }

    public static ProcessFunctionsFromParametersFactory<VfdtClassifierParams, HoeffdingTree<GaussianNaiveBayesStatistics, GaussianNaiveBayesStatisticsBuilder>, VfdtGaussianNaiveBayesProcessFunction> vfdtGaussianNb(List<VfdtClassifierParams> parameters) {
        String name = "vfdtGaussianNb";

        return new ProcessFunctionsFromParametersFactory<>(name, parameters) {
            @Override
            public VfdtGaussianNaiveBayesProcessFunction createProcessFunction(VfdtClassifierParams params, int classNumber, int attributesNumber, String dataset, long samplesLimit, Map<String, Integer> classEncoder) {
                return new VfdtGaussianNaiveBayesProcessFunction(name, dataset, samplesLimit, classEncoder) {
                    @Override
                    protected HoeffdingTree<GaussianNaiveBayesStatistics, GaussianNaiveBayesStatisticsBuilder> createClassifier() {
                        GaussianNaiveBayesStatisticsBuilder statisticsBuilder = new GaussianNaiveBayesStatisticsBuilder(classNumber, attributesNumber);
                        return new HoeffdingTree<>(classNumber, params.delta, attributesNumber, params.tau, params.nMin, statisticsBuilder) {
                            @Override
                            protected double heuristic(int attributeIndex, Node<GaussianNaiveBayesStatistics, GaussianNaiveBayesStatisticsBuilder> node) {
                                double threshold = 0.5;
                                return Math.abs(threshold - node.getStatistics().getSplittingValue(attributeIndex)) / threshold;
                            }
                        };
                    }
                };
            }
        };
    }

    public static ProcessFunctionsFromParametersFactory<WindowedDetectorVfdtClassifierParams, WindowedDetectorHoeffdingTree<SimpleNodeStatistics, SimpleNodeStatisticsBuilder>, WindowedDetectorVfdtProcessFunction> vfdtWindowedDetector(List<WindowedDetectorVfdtClassifierParams> parameters) {
        String name = "vfdtWindowedDetector";

        return new ProcessFunctionsFromParametersFactory<>(name, parameters) {
            @Override
            public WindowedDetectorVfdtProcessFunction createProcessFunction(WindowedDetectorVfdtClassifierParams params, int classNumber, int attributesNumber, String dataset, long samplesLimit, Map<String, Integer> classEncoder) {
                return new WindowedDetectorVfdtProcessFunction(name, dataset, samplesLimit, classEncoder) {
                    @Override
                    protected WindowedDetectorHoeffdingTree<SimpleNodeStatistics, SimpleNodeStatisticsBuilder> createClassifier() {
                        SimpleNodeStatisticsBuilder statisticsBuilder = new SimpleNodeStatisticsBuilder(classNumber, attributesNumber);
                        return new WindowedDetectorHoeffdingTree<>(classNumber, params.delta, attributesNumber, params.tau, params.nMin, statisticsBuilder, params.windowSize, params.warningFrac, params.driftFrac, params.updateMaxAccuracyEachSamples, params.bootstrapSubstituteTraining) {
                            @Override
                            protected double heuristic(int attributeIndex, Node<SimpleNodeStatistics, SimpleNodeStatisticsBuilder> node) {
                                double threshold = 0.5;
                                return Math.abs(threshold - node.getStatistics().getSplittingValue(attributeIndex)) / threshold;
                            }
                        };
                    }
                };
            }
        };
    }

    public static ProcessFunctionsFromParametersFactory<WindowedDetectorVfdtClassifierParams, WindowedDetectorHoeffdingTree<SimpleNodeStatistics, SimpleNodeStatisticsBuilder>, WindowedDetectorVfdtProcessFunction> vfdtEntropyWindowedDetector(List<WindowedDetectorVfdtClassifierParams> parameters) {
        String name = "vfdtEntropyWindowedDetector";

        return new ProcessFunctionsFromParametersFactory<>(name, parameters) {
            @Override
            public WindowedDetectorVfdtProcessFunction createProcessFunction(WindowedDetectorVfdtClassifierParams params, int classNumber, int attributesNumber, String dataset, long samplesLimit, Map<String, Integer> classEncoder) {
                return new WindowedDetectorVfdtProcessFunction(name, dataset, samplesLimit, classEncoder) {
                    @Override
                    protected WindowedDetectorHoeffdingTree<SimpleNodeStatistics, SimpleNodeStatisticsBuilder> createClassifier() {
                        SimpleNodeStatisticsBuilder statisticsBuilder = new SimpleNodeStatisticsBuilder(classNumber, attributesNumber);
                        return new WindowedDetectorHoeffdingTree<>(classNumber, params.delta, attributesNumber, params.tau, params.nMin, statisticsBuilder, params.windowSize, params.warningFrac, params.driftFrac, params.updateMaxAccuracyEachSamples, params.bootstrapSubstituteTraining) {
                            @Override
                            protected double heuristic(int attributeIndex, Node<SimpleNodeStatistics, SimpleNodeStatisticsBuilder> node) {
                                double examplesInfo = 0.0;
//
                                Map<Double, Long> valueCounts = new HashMap<>();
                                List<Map<Double, Long>> existingCounts = node.getStatistics().getAttributeValueCounts().get(attributeIndex);
                                existingCounts.forEach(valuesMap -> valuesMap.forEach((attributeValue, count) -> valueCounts.compute(attributeValue, (key, existingCount) -> (existingCount == null) ? count : existingCount + count)));
//
                                double attributeInfo = 0.0;
                                double base = Math.log(2);
                                for (Map.Entry<Double, Long> flattenedEntry : valueCounts.entrySet()) {
                                    double sumForEachClass = 0.0;

                                    for (int classIndex = 0; classIndex < classNumber; classIndex++) {
                                        double prob = ((double) existingCounts.get(classIndex).getOrDefault(flattenedEntry.getKey(), 0L)) / ((double) node.getClassCount(classIndex));
                                        sumForEachClass += prob * Math.log(prob) / base;
                                    }

                                    attributeInfo -= ((double) flattenedEntry.getValue()) / ((double) node.getN()) * sumForEachClass;
                                }

                                return examplesInfo - attributeInfo;
                            }
                        };
                    }
                };
            }
        };
    }

    public static ProcessFunctionsFromParametersFactory<WindowedDetectorVfdtClassifierParams, WindowedDetectorBstHoeffdingTree<SimpleNodeStatistics, SimpleNodeStatisticsBuilder>, WindowedDetectorBstVfdtProcessFunction> bstVfdtWindowedDetector(List<WindowedDetectorVfdtClassifierParams> parameters) {
        String name = "bstVfdtWindowedDetector";

        return new ProcessFunctionsFromParametersFactory<>(name, parameters) {
            @Override
            public WindowedDetectorBstVfdtProcessFunction createProcessFunction(WindowedDetectorVfdtClassifierParams params, int classNumber, int attributesNumber, String dataset, long samplesLimit, Map<String, Integer> classEncoder) {
                SimpleNodeStatisticsBuilder statisticsBuilder = new SimpleNodeStatisticsBuilder(classNumber, attributesNumber);
                return new WindowedDetectorBstVfdtProcessFunction(name, dataset, samplesLimit, classEncoder) {
                    @Override
                    protected WindowedDetectorBstHoeffdingTree<SimpleNodeStatistics, SimpleNodeStatisticsBuilder> createClassifier() {
                        return new WindowedDetectorBstHoeffdingTree<>(classNumber, params.delta, attributesNumber, params.tau, params.nMin, statisticsBuilder, params.windowSize, params.warningFrac, params.driftFrac, params.updateMaxAccuracyEachSamples, params.bootstrapSubstituteTraining) {
                            @Override
                            protected double heuristic(int attributeIndex, Node<SimpleNodeStatistics, SimpleNodeStatisticsBuilder> node) {
                                double threshold = 0.5;
                                return Math.abs(threshold - node.getStatistics().getSplittingValue(attributeIndex)) / threshold;
                            }
                        };
                    }
                };
            }
        };
    }

    public static ProcessFunctionsFromParametersFactory<VfdtClassifierParams, HoeffdingTreeNoParentDisable<SimpleNodeStatistics, SimpleNodeStatisticsBuilder>, VfdtNoParentDisableProcessFunction> vfdtNoParentDisable(List<VfdtClassifierParams> parameters) {
        String name = "vfdtNoParentDisable";
        return new ProcessFunctionsFromParametersFactory<>(name, parameters) {
            @Override
            public VfdtNoParentDisableProcessFunction createProcessFunction(VfdtClassifierParams params, int classNumber, int attributesNumber, String dataset, long samplesLimit, Map<String, Integer> classEncoder) {
                return new VfdtNoParentDisableProcessFunction(name, dataset, samplesLimit, classEncoder) {
                    @Override
                    protected HoeffdingTreeNoParentDisable<SimpleNodeStatistics, SimpleNodeStatisticsBuilder> createClassifier() {

                        SimpleNodeStatisticsBuilder statisticsBuilder = new SimpleNodeStatisticsBuilder(classNumber, attributesNumber);
                        return new HoeffdingTreeNoParentDisable<>(classNumber, params.delta, attributesNumber, params.tau, params.nMin, statisticsBuilder) {
                            @Override
                            protected double heuristic(int attributeNumber, Node<SimpleNodeStatistics, SimpleNodeStatisticsBuilder> node) {
                                double threshold = 0.5;
                                return Math.abs(threshold - node.getStatistics().getSplittingValue(attributeNumber)) / threshold;
                            }
                        };
                    }
                };
            }
        };
    }

    public static ProcessFunctionsFromParametersFactory<VfdtClassifierParams, BstHoeffdingTreeNoParentDisable<SimpleNodeStatistics, SimpleNodeStatisticsBuilder>, VfdtBstNoParentDisableProcessFunction> vfdtBstNoParentDisable(List<VfdtClassifierParams> parameters) {
        String name = "vfdtBstNoParentDisable";
        return new ProcessFunctionsFromParametersFactory<>(name, parameters) {
            @Override
            public VfdtBstNoParentDisableProcessFunction createProcessFunction(VfdtClassifierParams params, int classNumber, int attributesNumber, String dataset, long samplesLimit, Map<String, Integer> classEncoder) {
                return new VfdtBstNoParentDisableProcessFunction(name, dataset, samplesLimit, classEncoder) {
                    @Override
                    protected BstHoeffdingTreeNoParentDisable<SimpleNodeStatistics, SimpleNodeStatisticsBuilder> createClassifier() {
                        SimpleNodeStatisticsBuilder statisticsBuilder = new SimpleNodeStatisticsBuilder(classNumber, attributesNumber);

                        return new BstHoeffdingTreeNoParentDisable<>(classNumber, params.delta, attributesNumber, params.tau, params.nMin, statisticsBuilder) {
                            @Override
                            protected double heuristic(int attributeIndex, Node<SimpleNodeStatistics, SimpleNodeStatisticsBuilder> node) {
                                double threshold = 0.5;
                                return Math.abs(threshold - node.getStatistics().getSplittingValue(attributeIndex)) / threshold;
                            }
                        };
                    }
                };
            }
        };
    }

    public static ProcessFunctionsFromParametersFactory<VfdtClassifierParams, FunctionalBstHoeffdingTreeNoParentDisable<NaiveBayesNodeStatistics, NaiveBayesNodeStatisticsBuilder>, FunctionalVfdtBstNoParentDisableProcessFunction> vfdtBstNbNoParentDisable(List<VfdtClassifierParams> parameters) {
        String name = "vfdtBstNbNoParentDisable";
        return new ProcessFunctionsFromParametersFactory<>(name, parameters) {
            @Override
            public FunctionalVfdtBstNoParentDisableProcessFunction createProcessFunction(VfdtClassifierParams params, int classNumber, int attributesNumber, String dataset, long samplesLimit, Map<String, Integer> classEncoder) {
                return new FunctionalVfdtBstNoParentDisableProcessFunction(name, dataset, samplesLimit, classEncoder) {
                    @Override
                    protected FunctionalBstHoeffdingTreeNoParentDisable<NaiveBayesNodeStatistics, NaiveBayesNodeStatisticsBuilder> createClassifier() {
                        NaiveBayesNodeStatisticsBuilder statisticsBuilder = new NaiveBayesNodeStatisticsBuilder(classNumber, attributesNumber, 10);
                        return new FunctionalBstHoeffdingTreeNoParentDisable<>(classNumber, params.delta, attributesNumber, params.tau, params.nMin, statisticsBuilder) {
                            @Override
                            protected double heuristic(int attributeIndex, Node<NaiveBayesNodeStatistics, NaiveBayesNodeStatisticsBuilder> node) {
                                double threshold = 0.5;
                                return Math.abs(threshold - node.getStatistics().getSplittingValue(attributeIndex)) / threshold;
                            }
                        };
                    }
                };
            }
        };
    }

    public static ProcessFunctionsFromParametersFactory<VfdtClassifierParams, HoeffdingTreeNoParentDisable<SimpleNodeStatistics, SimpleNodeStatisticsBuilder>, VfdtNoParentDisableProcessFunction> vfdtEntropyNoParentDisable(List<VfdtClassifierParams> parameters) {
        String name = "vfdtEntropyNoParentDisable";
        return new ProcessFunctionsFromParametersFactory<>(name, parameters) {
            @Override
            public VfdtNoParentDisableProcessFunction createProcessFunction(VfdtClassifierParams params, int classNumber, int attributesNumber, String dataset, long samplesLimit, Map<String, Integer> classEncoder) {
                return new VfdtNoParentDisableProcessFunction(name, dataset, samplesLimit, classEncoder) {
                    @Override
                    protected HoeffdingTreeNoParentDisable<SimpleNodeStatistics, SimpleNodeStatisticsBuilder> createClassifier() {
                        SimpleNodeStatisticsBuilder statisticsBuilder = new SimpleNodeStatisticsBuilder(classNumber, attributesNumber);
                        return new HoeffdingTreeNoParentDisable<>(classNumber, params.delta, attributesNumber, params.tau, params.nMin, statisticsBuilder) {
                            @Override
                            protected double heuristic(int attributeIndex, Node<SimpleNodeStatistics, SimpleNodeStatisticsBuilder> node) {
                                double examplesInfo = 0.0;
//
                                Map<Double, Long> valueCounts = new HashMap<>();
                                List<Map<Double, Long>> existingCounts = node.getStatistics().getAttributeValueCounts().get(attributeIndex);
                                existingCounts.forEach(valuesMap -> valuesMap.forEach((attributeValue, count) -> valueCounts.compute(attributeValue, (key, existingCount) -> (existingCount == null) ? count : existingCount + count)));
//
                                double attributeInfo = 0.0;
                                double base = Math.log(2);
                                for (Map.Entry<Double, Long> flattenedEntry : valueCounts.entrySet()) {
                                    double sumForEachClass = 0.0;

                                    for (int classIndex = 0; classIndex < classNumber; classIndex++) {
                                        double prob = ((double) existingCounts.get(classIndex).getOrDefault(flattenedEntry.getKey(), 0L)) / ((double) node.getClassCount(classIndex));
                                        sumForEachClass += prob * Math.log(prob) / base;
                                    }

                                    attributeInfo -= ((double) flattenedEntry.getValue()) / ((double) node.getN()) * sumForEachClass;
                                }

                                return examplesInfo - attributeInfo;
                            }
                        };
                    }
                };
            }
        };
    }

    public static ProcessFunctionsFromParametersFactory<VfdtClassifierParams, HoeffdingTreeNoParentDisable<GaussianNaiveBayesStatistics, GaussianNaiveBayesStatisticsBuilder>, VfdtGaussianNaiveBayesNoParentDisableProcessFunction> vfdtGaussianNbNoParentDisable(List<VfdtClassifierParams> parameters) {
        String name = "vfdtGaussianNbNoParentDisable";

        return new ProcessFunctionsFromParametersFactory<>(name, parameters) {
            @Override
            public VfdtGaussianNaiveBayesNoParentDisableProcessFunction createProcessFunction(VfdtClassifierParams params, int classNumber, int attributesNumber, String dataset, long samplesLimit, Map<String, Integer> classEncoder) {
                return new VfdtGaussianNaiveBayesNoParentDisableProcessFunction(name, dataset, samplesLimit, classEncoder) {
                    @Override
                    protected HoeffdingTreeNoParentDisable<GaussianNaiveBayesStatistics, GaussianNaiveBayesStatisticsBuilder> createClassifier() {
                        GaussianNaiveBayesStatisticsBuilder statisticsBuilder = new GaussianNaiveBayesStatisticsBuilder(classNumber, attributesNumber);
                        return new HoeffdingTreeNoParentDisable<>(classNumber, params.delta, attributesNumber, params.tau, params.nMin, statisticsBuilder) {
                            @Override
                            protected double heuristic(int attributeIndex, Node<GaussianNaiveBayesStatistics, GaussianNaiveBayesStatisticsBuilder> node) {
                                double threshold = 0.5;
                                return Math.abs(threshold - node.getStatistics().getSplittingValue(attributeIndex)) / threshold;
                            }
                        };
                    }
                };
            }
        };
    }
}
