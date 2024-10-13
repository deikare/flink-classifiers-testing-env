import csv
import json
import os
from typing import Callable

import matplotlib.pyplot as plt
import numpy as np
import pandas as pd
from tabulate import tabulate

from ClassifierResults import ClassifierResults

import argparse


class PlotPrinterConfig:
    def __init__(self, directory: str, description: str):
        self.directory = directory
        self.description = description

    def is_set(self):
        return self.description is not None

    def get_path(self):
        return f"{self.directory}/{self.description}"


def extractParams(classifierDir: str) -> dict[str, float]:
    resultDict = {}

    for classifierRaw in classifierDir.split("_"):
        valueBeginIdx = -1
        for i, c in enumerate(classifierRaw):
            if c.isdigit():
                valueBeginIdx = i
                break

        key = classifierRaw[:(valueBeginIdx)]
        value = float(classifierRaw[valueBeginIdx:])
        resultDict[key] = value

    return resultDict


def listOfExperimentFilePathsForParams(classifierExpPath: str) -> list[str]:
    pairs = []
    for maybeDir in os.scandir(classifierExpPath):
        if (maybeDir.is_dir()):
            for expFile in os.scandir(maybeDir.path):
                with open(expFile.path, "r") as expData:
                    firstLine = expData.readline()
                    timestamp = int((firstLine.split(","))[0])
                    pairs.append((expFile.path, timestamp))

    sortedPairs = sorted(pairs, key=lambda pair: pair[1])
    return list(map(lambda pair: pair[0], sortedPairs))


def readData(paths: list[str], headers: list[str]):
    collectedHeaders = ["timestamp", "class", "predicted"] + headers[3:]
    accuracyKey = "accuracy"
    result = {}
    for tmp in collectedHeaders + [accuracyKey]:
        result[tmp] = []

    count = 0
    correct = 0

    for path in paths:
        with open(path, "r") as file:
            reader = csv.DictReader(file, fieldnames=headers)

            for row in reader:
                count = count + 1
                if row["class"] == row["predicted"]:
                    correct = correct + 1

                result[accuracyKey].append(100.0 * float(correct) / float(count))

                for key in collectedHeaders:
                    result[key].append(int(row[key]))

    result_np = {}
    for key, value in result.items():
        result_np[key] = np.array(value)
    return result_np


def getUnit(measurement: str):
    if "Duration" in measurement or "duration" in measurement:
        return "$\mu$s"
    elif measurement == "accuracy":
        return "%"
    elif "%" in measurement:
        return "%"
    else:
        return None


def translatePerformanceType(performanceType: str):
    match performanceType:
        case "accuracy":
            return "$p_\\mathrm{klas}$"
        case "trainingDuration":
            return "$t_\\mathrm{ucz}$"
        case "classificationDuration":
            return "$t_\\mathrm{pred}$"
        case "nodesDuringTraversalCount":
            return "$n_\\mathrm{nodes}$"
        case "duringTraversalDuration":
            return "$t_\\mathrm{traverse}$"
        case "leafSplit":
            return "$f_\\mathrm{split}$"
        case "weightsNormalizationAndClassifierDeleteDuration":
            return "$t_\\mathrm{norm+del}$"
        case "deletedClassifiersCount":
            return "$n_\\mathrm{del}$"
        case "deletedClassifiersCount%":
            return "$n_\\mathrm{del}$"
        case "addedClassifiersCount":
            return "$n_\\mathrm{add}$"
        case "addClassifierDuration":
            return "$t_\\mathrm{add}$"
        case "classifiersAfterTrainCount":
            return "$n_\\mathrm{poUcz}$"
        case "avgClassifierTTL":
            return "$n_\\mathrm{TTL}$"
        case "weightsLoweringCount":
            return "$n_\\mathrm{w\\downarrow}$"
        case "correctVotesCount":
            return "$n_\\mathrm{poprawne}$"
        case "wrongVotesCount":
            return "$n_\\mathrm{błędne}$"
        case "weightsLoweringCount%":
            return "$n_\\mathrm{w\\downarrow}$"
        case "correctVotesCount%":
            return "$n_\\mathrm{poprawne}$"
        case "wrongVotesCount%":
            return "$n_\\mathrm{błędne}$"
        case "avgTrainDuration":
            return "$t_\\mathrm{ucz_\\mathrm{avg}}$"
        case "avgClassifyDuration":
            return "$t_\\mathrm{pred_\\mathrm{avg}}$"
        case "substituteTrainingBegin":
            return "TODO"
        case "replacedClassifier":
            return "TODO"
        case _:
            return ""


def translateClassifierType(classifierType: str):
    match classifierType:
        case "vfdt":
            return "classicVFDT"
        case "vfdtBst":
            return "bstVFDT"
        case "vfdtEntropy":
            return "entropyVFDT"
        case "vfdtGaussianNb":
            return "gaussNbVFDT"
        case "vfdtBstNb":
            return "bstGaussNbVFDT"
        case "vfdtNoParentDisable":
            return "classicVFDTNP"
        case "vfdtBstNoParentDisable":
            return "bstVFDTNP"
        case "vfdtBstNbNoParentDisable":
            return "bstGaussNbVFDTNP"
        case "vfdtEntropyNoParentDisable":
            return "entropyVFDTNP"
        case "vfdtGaussianNbNoParentDisable":
            return "gaussNbVFDTNP"
        case "dwm":
            return "classicDWM"
        case "extendedDwm":
            return "EDWM"
        case "vfdtWindowedDetector":
            return "classicWadVFDT"
        case "bstVfdtWindowedDetector":
            return "bstWadVFDT"
        case "vfdtEntropyWindowedDetector":
            return "entropyWadVFDT"
        case _:
            return ""


def readAllResults(classifierPath: str):
    classifierType = os.path.basename(os.path.normpath(classifierPath))

    allResults = []

    for classifierParamsRaw in os.listdir(classifierPath):
        classifierParamsPath = f"{classifierPath}/{classifierParamsRaw}"

        classifierParams = extractParams(classifierParamsRaw)

        with open(f"{classifierParamsPath}/result.json", "r") as resultFile:
            resultJson = json.load(resultFile)
            headers = resultJson["dataHeader"]

            chronologicalDataFilePaths = listOfExperimentFilePathsForParams(classifierParamsPath)
            results = readData(chronologicalDataFilePaths, headers)

            allResults.append(
                ClassifierResults(classifierParams, classifierType, results, headers, resultJson["jobId"]))

    return allResults


def getBestClassifier(classifierPath: str):
    classifierType = os.path.basename(os.path.normpath(classifierPath))

    bestAccuracy = -1.0
    bestParams = {}
    bestResults = {}
    bestHeaders = []
    bestJobId = ""

    allResults = []

    for classifierParamsRaw in os.listdir(classifierPath):
        classifierParamsPath = f"{classifierPath}/{classifierParamsRaw}"

        classifierParams = extractParams(classifierParamsRaw)

        with open(f"{classifierParamsPath}/result.json", "r") as resultFile:
            resultJson = json.load(resultFile)
            headers = resultJson["dataHeader"]

            chronologicalDataFilePaths = listOfExperimentFilePathsForParams(classifierParamsPath)
            results = readData(chronologicalDataFilePaths, headers)

            currentAccuracy = results["accuracy"][-1]
            if currentAccuracy > bestAccuracy:
                bestAccuracy = currentAccuracy
                bestParams = classifierParams
                bestResults = results
                bestHeaders = headers
                bestJobId = resultJson["jobId"]

            trainingDuration = round(np.sum(results["trainingDuration"]) / 1e9, 2)
            classificationDuration = round(np.sum(results["classificationDuration"]) / 1e9, 2)

            allResults.append((os.path.basename(classifierPath), classifierParams, currentAccuracy,
                               trainingDuration + classificationDuration, trainingDuration,
                               classificationDuration))

    return ClassifierResults(bestParams, classifierType, bestResults, bestHeaders, bestJobId), allResults


def plotWindowed(dataset: str, classifierResults: list[ClassifierResults], performanceType: str,
                 plot_printer_config: PlotPrinterConfig, windowSize: int):
    plot(dataset, classifierResults, performanceType, plot_printer_config,
         mapper=lambda x: np.convolve(x, np.ones(windowSize) / windowSize, mode="valid"),
         subtitle=f"window {windowSize} samples", sampleNumberMapper=lambda x: x + windowSize, showDetections=False)


def plotDwm(dataset: str, classifierResults: list[ClassifierResults], plot_printer_config: PlotPrinterConfig):
    dwmResults = [s for s in classifierResults if "dwm" in s.classifierType or "Dwm" in s.classifierType]

    if dwmResults:
        plot(dataset, dwmResults, "avgClassifierTTL", plot_printer_config, showDetections=False)
        plot(dataset, dwmResults, "classifiersAfterTrainCount", plot_printer_config, showDetections=False)


def plotVfdt(dataset: str, classifierResults: list[ClassifierResults], plot_printer_config: PlotPrinterConfig):
    vfdtResults = [s for s in classifierResults if "fdt" in s.classifierType or "Vfdt" in s.classifierType]

    if vfdtResults:
        vfdtResults.sort(key=lambda x: np.trapz(x.results["nodesDuringTraversalCount"]), reverse=True)
        plot(dataset, vfdtResults, "nodesDuringTraversalCount", plot_printer_config, showDetections=False)
        plotWindowed(dataset, vfdtResults, "duringTraversalDuration", plot_printer_config, 100)


def plotComparison(dataset: str, classifierResults: list[ClassifierResults], plot_printer_config: PlotPrinterConfig):
    plot(dataset, classifierResults, "accuracy", plot_printer_config,
         labelFun=lambda classifier: f"{classifier.classifierType}: {round(classifier.accuracy(), 2)}",
         showDetections=True)
    windowSize = 1000

    plotWindowed(dataset, classifierResults, "accuracy", plot_printer_config, windowSize)
    plotWindowed(dataset, classifierResults, "classificationDuration", plot_printer_config, windowSize)
    plotWindowed(dataset, classifierResults, "trainingDuration", plot_printer_config, windowSize)

    plotVfdt(dataset, classifierResults, plot_printer_config)
    plotDwm(dataset, classifierResults, plot_printer_config)


def plot(dataset: str, classifierResults: list[ClassifierResults], performanceType: str,
         plot_printer_config: PlotPrinterConfig,
         mode: str = None,
         labelFun: Callable[[ClassifierResults], str] = lambda classifier: classifier.classifierType,
         mapper: Callable[[np.ndarray[int]], np.ndarray[int]] = lambda x: x, subtitle: str = None,
         showDetections: bool = True, prefix: str = None, overridenTitle: str = None, overridenYLabel: str = None,
         overridenUnit: str = None, printSmall: bool = False,
         sampleNumberMapper: Callable[[np.ndarray], np.ndarray] = lambda x: x):
    if plot_printer_config.is_set() and printSmall is False:
        plt.figure(dpi=1200)
    else:
        plt.figure()

    performanceTypeTranslated = translatePerformanceType(performanceType)

    if overridenTitle:
        plt.title(overridenTitle)
    else:
        plt.title(title(dataset, prefix, performanceTypeTranslated, subtitle))

    if overridenUnit:
        unit = overridenUnit
    else:
        unit = getUnit(performanceType)

    for classifier in classifierResults:
        y = mapper(classifier.results[performanceType])

        if mode is None or mode == "sample":
            sampleNumbers = sampleNumberMapper(np.cumsum(np.ones_like(y)))
            curve = plt.plot(sampleNumbers, y, label=labelFun(classifier))
            if showDetections is True:
                if "Detector" in classifier.classifierType or "detector" in classifier.classifierType:
                    detectionIdxes = np.where(classifier.results["replacedClassifier"] > 0)[0]
                    for detectionIdx in detectionIdxes:
                        plt.axvline(x=detectionIdx, color=curve[0].get_color(), linestyle=":", linewidth=1)
        else:
            if mode == "event":
                x = pd.to_datetime(classifier.results["timestamp"], unit="ns", utc=True)
            else:
                timestamps = classifier.results["timestamp"]
                x = (timestamps - timestamps[0]) / 1e9
            plt.plot(x, y, label=labelFun(classifier))

    if len(classifierResults) > 1:
        plt.legend()

    if overridenYLabel:
        plt.ylabel(overridenYLabel)
    else:
        plt.ylabel(ylabel(performanceTypeTranslated, unit))

    if mode is None or mode == "sample":
        xlabel = "numer próbki"
    elif mode == "event":
        xlabel = "\\emph{event time}"
    else:
        xlabel = "\\emph{processing time} [s]"
    plt.xlabel(xlabel)

    if plot_printer_config.is_set():
        if overridenTitle:
            title_formatted = overridenTitle.replace(" ", "_").replace("\\", "").replace("$", "").replace("{",
                                                                                                          "").replace(
                "}", "").strip()
            plt.savefig(f"{plot_printer_config.get_path()}/{title_formatted}.png")
        else:
            if mode is None or mode == "sample":
                xlabel = "sample"
            elif mode == "event":
                xlabel = "event time"
            else:
                xlabel = "processing time [s]"

            xlabel_formatted = xlabel.split(" ")[0]
            if subtitle:
                subtitle_formatted = subtitle.replace(" ", "_").replace("%", "")
            else:
                subtitle_formatted = ""

            if prefix:
                prefix_formatted = prefix.replace(" ", "_").replace("%", "")
            else:
                prefix_formatted = ""

            perfTypeReplaced = performanceType.replace("%", "")

            plt.savefig(
                f"{plot_printer_config.get_path()}/{perfTypeReplaced}_{prefix_formatted}_{subtitle_formatted}_{xlabel_formatted}.png")
    else:
        plt.show(block=False)


def plotBuckets(dataset: str, classifierResults: list[ClassifierResults], bucketSize: int,
                performanceType: str, plot_printer_config: PlotPrinterConfig,
                labelFun: Callable[[ClassifierResults], str] = lambda classifier: classifier.classifierType,
                reducer: Callable[[ClassifierResults, int, int], float] = lambda result, start, end: 100.0 * np.mean(
                    (1 - np.abs(result.results["class"] - result.results["predicted"]))[start:end]),
                subtitle: str = "mean accuracy"):
    plt.figure()

    performanceTypeTranslated = translatePerformanceType(performanceType)

    plt.title(f"zbiór {dataset}: {subtitle} - zgrupowane po {bucketSize} próbek")

    unit = getUnit(performanceType)

    bucketsNum = len(classifierResults[0].results["accuracy"]) // bucketSize

    for classifier in classifierResults:
        x = np.zeros(bucketsNum)
        y = np.zeros(bucketsNum)

        for i in range(bucketsNum):
            startIdx = i * bucketSize
            x[i] = startIdx

            endIdx = startIdx + bucketSize

            y[i] = reducer(classifier, startIdx, endIdx)

        plt.step(x, y, label=labelFun(classifier))

    plt.legend()
    plt.xlabel("numer próbki")
    plt.ylabel(ylabel(performanceTypeTranslated, unit))

    if plot_printer_config.is_set():
        subtitle_formatted = subtitle.replace(" ", "_")
        plt.savefig(f"{plot_printer_config.get_path()}/bucketed_{subtitle_formatted}.png")
    else:
        plt.show()


def title(dataset: str, prefix: str, performanceType: str, subtitle: str):
    result = f"zbiór {dataset}"
    if prefix:
        result += " - " + prefix
    result += f": {performanceType}"
    if subtitle:
        result += " - " + subtitle

    return result


def ylabel(performanceType: str, unit: str):
    if unit:
        result = f"{performanceType} [{unit}]"
    else:
        result = performanceType

    return result


if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument("--description", action="store", default=None, required=False,
                        help="Description if plots should be printed as files")
    parser.add_argument("--plotsDir", action="store", default=None, required=False,
                        help="Directory for plots to be saved to")
    args = parser.parse_args()

    plot_printer_config = PlotPrinterConfig(args.plotsDir, args.description)

    experimentId = os.environ["EXPERIMENT_ID"]
    print(f"experimentId: {experimentId}")
    resultsInputDir = os.environ["RESULTS_DIRECTORY"]
    resultsPath = f"{resultsInputDir}/{experimentId}"
    for dataset in os.listdir(resultsPath):
        datasetPath = f"{resultsPath}/{dataset}"

        bestClassifierResults = []

        allResults = []

        for classifierType in os.listdir(datasetPath):
            classifierPath = f"{datasetPath}/{classifierType}"

            bestClassifier, tmpResults = getBestClassifier(classifierPath)
            bestClassifierResults.append(bestClassifier)
            allResults.extend(tmpResults)

        bestClassifierResults = sorted(bestClassifierResults, key=lambda classifier: classifier.accuracy(),
                                       reverse=True)

        allResults = sorted(allResults, key=lambda x: x[2], reverse=True)
        print(tabulate(allResults, headers=["type", "params", "accuracy", "duration", "trainingDuration",
                                            "classificationDuration"]))

        plotComparison(dataset, bestClassifierResults, plot_printer_config)

    if not plot_printer_config.is_set():
        plt.show()
