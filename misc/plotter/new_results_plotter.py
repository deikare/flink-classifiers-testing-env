import argparse
import os
import sys
from typing import Callable

import matplotlib.pyplot as plt
import numpy as np

import results_plotter
from misc.plotter.ClassifierResults import ClassifierResults
from misc.plotter.results_plotter import PlotPrinterConfig

import re


class PerformPlotting:
    def __init__(self, experimentId: str, performPlotting: Callable[
        [list[results_plotter.ClassifierResults], str, results_plotter.PlotPrinterConfig], None], description: str):
        self.description = description
        self.experimentId = experimentId
        self.performPlotting = performPlotting


def windowSubtitle(windowSize: int):
    return f"w oknie {windowSize} próbek"


def windowSamplesMapper(array: np.ndarray, windowSize: int):
    return array + windowSize


def window(array: np.ndarray, windowSize: int):
    return np.convolve(array, np.ones(windowSize) / windowSize, mode="valid")


def plotPercentageOfSplitTypes(results: list[ClassifierResults], windowSize: int, cfg: PlotPrinterConfig,
                               labelFun: Callable[[ClassifierResults], str] = None, prefix: str = None,
                               printSmall: bool = False):
    if cfg.is_set() and not printSmall:
        plt.figure(dpi=1200)
    else:
        plt.figure()

    for result in results:
        values = result.results["leafSplit"]
        allSplitCounts = window(np.where(values > 0, 1, 0), windowSize)
        hCounts = window(np.where(values == 1, 1, 0), windowSize)
        tauCounts = window(np.where(values == 2, 1, 0), windowSize)

        percentagesH = np.divide(hCounts * 100, allSplitCounts, where=allSplitCounts != 0,
                                 out=np.zeros_like(allSplitCounts))
        percentagesTau = np.divide(tauCounts * 100, allSplitCounts, where=allSplitCounts != 0,
                                   out=np.zeros_like(allSplitCounts))

        title = "$n_\\mathrm{splitType}$" + f" w oknie {windowSize} próbek"
        if prefix:
            title = f"{prefix}: {title}"
        plt.title(title)

        if labelFun and len(results) > 1:
            label = labelFun(result)
            plt.plot(percentagesH, label=f"$\\Delta h > \\epsilon$ {label}")
            plt.plot(percentagesTau, label=f"$\\epsilon < \\tau$ {label}")
        else:
            plt.plot(percentagesH, label="$\\Delta h > \\epsilon$")
            plt.plot(percentagesTau, label="$\\epsilon < \\tau$")

    plt.legend()
    plt.ylabel("$n_\\mathrm{splitType}$ [%]")
    plt.xlabel("numer próbki")
    if cfg.is_set():
        filename = f"leaf-split-percentages-windowed-{windowSize}"
        if prefix:
            filename = f"{prefix}_{filename}"
        plt.savefig(f"{cfg.get_path()}/{filename}.png")
    else:
        plt.show(block=False)


def stringLatexTable(headers: list[str], rows: list[tuple], caption: str, label: str):
    result = "printing table\n\n\\begin{table}[!h]\n"
    result += "\\centering\n"
    result += "\\caption{" + caption + "}\n"
    result += "\\label{" + label + "}\n\n"
    result += "\\begin{tabular} {| c " + ("| c " * (len(headers) - 1)) + "|}\n"
    result += "\\hline\n"

    headers_str = " & ".join(headers) + " \\\\ \\hline \\hline\n"
    result += headers_str

    for row in rows:
        valuesEnc = []
        for value in row:
            if isinstance(value, int):
                valuesEnc.append(str(value))
            elif isinstance(value, float):
                if value < 0.001:
                    valueStr = "{:.3e}".format(
                        value)  # Converts to scientific notation with 2 digits after the decimal point
                else:
                    valueStr = "{:.3f}".format(
                        value)  # Keeps the regular float format with 2 digits after the decimal point

                if 'e' in valueStr:
                    parts = valueStr.split('e')
                    parts[0] = parts[0].rstrip('0').rstrip('.')
                    formatted = 'e'.join(parts)
                else:
                    formatted = valueStr.rstrip('0').rstrip('.')
                valuesEnc.append("$\\num{" + formatted + "}$")
            else:
                valuesEnc.append(value)

        rowStr = " & ".join(valuesEnc) + "\\\\ \\hline\n"
        result += rowStr

    result += "\\end{tabular}\n\n"
    result += "\\end{table}\n"

    return result


class PlotSettings:
    def __init__(self, accuracy: bool, accuracyWindowed: bool, trainingDuration: bool, classificationDuration: bool):
        self.trainingDuration = trainingDuration
        self.classificationDuration = classificationDuration
        self.accuracyWindowed = accuracyWindowed
        self.accuracy = accuracy


def printDefaultPlots(results: list[results_plotter.ClassifierResults], dataset: str,
                      cfg: results_plotter.PlotPrinterConfig,
                      labelFun: Callable[[results_plotter.ClassifierResults], str], prefix: str = None,
                      subtitle: str = None, plotSettings: PlotSettings = PlotSettings(True, True, True, True),
                      showDetections: bool = False):
    results_plotter.plot(dataset, results, "accuracy", cfg,
                         labelFun=labelFun, prefix=prefix, printSmall=plotSettings.accuracy, subtitle=subtitle,
                         showDetections=showDetections)
    windowSize = 1000
    results_plotter.plot(dataset, results, "accuracy", cfg, labelFun=labelFun, prefix=prefix,
                         subtitle=windowSubtitle(windowSize),
                         mapper=lambda x: window(x, windowSize), printSmall=plotSettings.accuracyWindowed,
                         showDetections=showDetections, sampleNumberMapper=lambda x: windowSamplesMapper(x, windowSize))

    windowSize = 1000
    results_plotter.plot(dataset, results, "trainingDuration", cfg, labelFun=labelFun,
                         mapper=lambda x: window(x, windowSize),
                         subtitle=windowSubtitle(windowSize), prefix=prefix, printSmall=plotSettings.trainingDuration,
                         showDetections=showDetections, sampleNumberMapper=lambda x: windowSamplesMapper(x, windowSize))

    windowSize = 1000
    results_plotter.plot(dataset, results, "classificationDuration", cfg, labelFun=labelFun,
                         mapper=lambda x: window(x, windowSize), subtitle=windowSubtitle(windowSize), prefix=prefix,
                         printSmall=plotSettings.classificationDuration, showDetections=showDetections,
                         sampleNumberMapper=lambda x: windowSamplesMapper(x, windowSize))


class VfdtPlotSettings(PlotSettings):
    def __init__(self, accuracy: bool, accuracyWindowed: bool, trainingDuration: bool, classificationDuration: bool,
                 nodesDuringTraversalCount: bool, duringTraversalDuration: bool):
        super().__init__(accuracy, accuracyWindowed, trainingDuration, classificationDuration)
        self.duringTraversalDuration = duringTraversalDuration
        self.nodesDuringTraversalCount = nodesDuringTraversalCount


def printVfdtDefaultPlots(results: list[results_plotter.ClassifierResults], dataset: str,
                          cfg: results_plotter.PlotPrinterConfig,
                          labelFun: Callable[[results_plotter.ClassifierResults], str], prefix: str = None,
                          subtitle: str = None, plotSettings: VfdtPlotSettings = VfdtPlotSettings(
            True, True, True, True, True, True), printDefaults: bool = True, showDetections: bool = False):
    if printDefaults is True:
        printDefaultPlots(results, dataset, cfg, labelFun, prefix, subtitle, plotSettings=plotSettings,
                          showDetections=showDetections)

    results_plotter.plot(dataset, results, "nodesDuringTraversalCount", cfg, labelFun=labelFun,
                         prefix=prefix, printSmall=plotSettings.nodesDuringTraversalCount, subtitle=subtitle,
                         showDetections=showDetections)

    windowSize = 100
    results_plotter.plot(dataset, results, "duringTraversalDuration", cfg, labelFun=labelFun,
                         prefix=prefix, mapper=lambda x: window(x[10:][np.where(x[10:] <= 1e3)], windowSize),
                         subtitle=windowSubtitle(windowSize), printSmall=plotSettings.duringTraversalDuration,
                         showDetections=showDetections, sampleNumberMapper=lambda x: windowSamplesMapper(x, windowSize))


def classicVfdtNminComparison():
    def plottingAction(results: list[results_plotter.ClassifierResults], dataset: str,
                       cfg: results_plotter.PlotPrinterConfig):
        results.sort(key=lambda x: x.params["nMin"])
        labelFun = lambda result: "$N_\\mathrm{min} = $" + str(int(result.params["nMin"]))
        prefix = "classicVFDT"

        printVfdtDefaultPlots(results, dataset, cfg, labelFun, prefix)

        headers = ["$N_\\mathrm{min}$", "łącznie", "łącznie [\\%]", "heurystyka", "heurystyka [\\%]",
                   "anti-stall",
                   "anti-stall [\\%]"]
        latexTable = []
        for result in results:
            nMin = int(result.params["nMin"])
            n = result.results["accuracy"].shape[0]
            hSplitCounts = np.count_nonzero(result.results["leafSplit"] == 1)
            hSplitCountsPerc = hSplitCounts * 100.0 / n
            tauSplitCounts = np.count_nonzero(result.results["leafSplit"] == 2)
            tauSplitCountsPerc = tauSplitCounts * 100.0 / n

            totalSplitCounts = hSplitCounts + tauSplitCounts
            totalSplitCountsPerc = totalSplitCounts * 100.0 / n

            latexTable.append((nMin, totalSplitCounts, totalSplitCountsPerc, hSplitCounts, hSplitCountsPerc,
                               tauSplitCounts, tauSplitCountsPerc))

        print(stringLatexTable(headers, latexTable,
                               "Zestawienie, spośród ilu przetwarzanych próbek różnych typów podziałów w klasyfikatorze \\texttt{classicVFDT} (liczba próbek = " + str(
                                   n) + ")", "tab:6-classicVfdtComparison-splitTypes"))

    return PerformPlotting(
        # experimentId="cdb41068-3b1d-4528-9615-9d772826a7c8",
        experimentId="c8d15561-daac-4e1f-841a-8d615e1c64ab",
        performPlotting=plottingAction,
        description="classicVfdtNminComparison"
    )


def classicVfdtDeltaComparison():
    def plottingAction(results: list[results_plotter.ClassifierResults], dataset: str,
                       cfg: results_plotter.PlotPrinterConfig):
        results.sort(key=lambda x: x.params["delta"])

        labelFun = lambda result: "$\\delta = $" + str(result.params["delta"]).replace(".", ",")
        prefix = "classicVFDT"

        printVfdtDefaultPlots(results, dataset, cfg, labelFun, prefix)

        headers = ["$\\delta$", "łącznie", "łącznie [\\%]", "heurystyka", "heurystyka [\\%]",
                   "anti-stall",
                   "anti-stall [\\%]"]
        latexTable = []
        for result in results:
            delta = float(result.params["delta"])
            n = result.results["accuracy"].shape[0]
            hSplitCounts = np.count_nonzero(result.results["leafSplit"] == 1)
            hSplitCountsPerc = hSplitCounts * 100.0 / n
            tauSplitCounts = np.count_nonzero(result.results["leafSplit"] == 2)
            tauSplitCountsPerc = tauSplitCounts * 100.0 / n

            totalSplitCounts = hSplitCounts + tauSplitCounts
            totalSplitCountsPerc = totalSplitCounts * 100.0 / n

            latexTable.append((delta, totalSplitCounts, totalSplitCountsPerc, hSplitCounts, hSplitCountsPerc,
                               tauSplitCounts, tauSplitCountsPerc))

        print(stringLatexTable(headers, latexTable,
                               "Zestawienie, spośród ilu przetwarzanych próbek różnych typów podziałów w klasyfikatorze \\texttt{classicVFDT} (liczba próbek = " + str(
                                   n) + ")", "tab:6-classicVfdtComparison-splitTypes"))

    return PerformPlotting(
        experimentId="36ae951a-b6fc-4181-ad76-3a3aee944e6a",
        # experimentId="6fd5d12c-a5a4-4390-8e0b-b63bed68bf6a",
        # experimentId="c0c3f26e-a139-43d6-b46c-c8bdd1430a55",
        performPlotting=plottingAction,
        description="classicVfdtDeltaComparison"
    )


def classicVfdtTauComparison():
    def plottingAction(results: list[results_plotter.ClassifierResults], dataset: str,
                       cfg: results_plotter.PlotPrinterConfig):
        results.sort(key=lambda x: x.params["tau"])

        labelFun = lambda result: "$\\tau = $" + str(result.params["tau"]).replace(".", ",")
        prefix = "classicVFDT"

        printVfdtDefaultPlots(results, dataset, cfg, labelFun, prefix)

        headers = ["$\\tau$", "łącznie", "łącznie [\\%]", "heurystyka", "heurystyka [\\%]",
                   "anti-stall",
                   "anti-stall [\\%]"]
        latexTable = []
        for result in results:
            tau = float(result.params["tau"])
            n = result.results["accuracy"].shape[0]
            hSplitCounts = np.count_nonzero(result.results["leafSplit"] == 1)
            hSplitCountsPerc = hSplitCounts * 100.0 / n
            tauSplitCounts = np.count_nonzero(result.results["leafSplit"] == 2)
            tauSplitCountsPerc = tauSplitCounts * 100.0 / n

            totalSplitCounts = hSplitCounts + tauSplitCounts
            totalSplitCountsPerc = totalSplitCounts * 100.0 / n

            latexTable.append((tau, totalSplitCounts, totalSplitCountsPerc, hSplitCounts, hSplitCountsPerc,
                               tauSplitCounts, tauSplitCountsPerc))

        print(stringLatexTable(headers, latexTable,
                               "Zestawienie, spośród ilu przetwarzanych próbek różnych typów podziałów w klasyfikatorze \\texttt{classicVFDT} (liczba próbek = " + str(
                                   n) + ")", "tab:6-classicVfdtComparison-splitTypes"))

    return PerformPlotting(
        # experimentId="34e4f3ff-57f9-4e96-95f4-f5aa86b17d7f",
        experimentId="f2cadb3c-f014-4c47-b13d-568c1f24dcc0",
        performPlotting=plottingAction,
        description="classicVfdtTauComparison"
    )


def classicVfdtCombinedComparison():
    def plottingAction(results: list[results_plotter.ClassifierResults], dataset: str,
                       cfg: results_plotter.PlotPrinterConfig):
        results = sorted(results, key=lambda x: (x.params["nMin"], x.params["delta"], x.params["tau"]))
        headers = ["$N_\\mathrm{min}$", "$\\delta$", "$\\tau$", "łącznie", "łącznie [\\%]", "$h$",
                   "$h$ [\\%]",
                   "anti-stall",
                   "anti-stall [\\%]"]
        latexTable = []
        for result in results:
            tau = float(result.params["tau"])
            delta = float(result.params["delta"])
            nMin = int(result.params["nMin"])

            n = result.results["accuracy"].shape[0]
            hSplitCounts = np.count_nonzero(result.results["leafSplit"] == 1)
            hSplitCountsPerc = hSplitCounts * 100.0 / n
            tauSplitCounts = np.count_nonzero(result.results["leafSplit"] == 2)
            tauSplitCountsPerc = tauSplitCounts * 100.0 / n

            totalSplitCounts = hSplitCounts + tauSplitCounts
            totalSplitCountsPerc = totalSplitCounts * 100.0 / n

            latexTable.append((nMin, delta, tau, totalSplitCounts, totalSplitCountsPerc, hSplitCounts, hSplitCountsPerc,
                               tauSplitCounts, tauSplitCountsPerc))

        latexTable = sorted(latexTable, key=lambda x: x[3], reverse=True)

        print(stringLatexTable(headers, latexTable,
                               "Zestawienie liczby różnych podziałów drzewa podczas eksperymentów z wykorzystaniem klasyfikatora \\texttt{classicVFDT} (liczba próbek = " + str(
                                   n) + ")", "tab:6-classicVfdtComparison-splitTypes"))

        results = sorted(results, key=lambda x: np.sum(np.where(x.results["leafSplit"] > 0, 1, 0)), reverse=True)

        windowSize = 3000
        results_plotter.plot(dataset, [results[0]], "leafSplit", cfg,
                             mapper=lambda x: 100.0 * window(np.where(x > 0, 1, 0), windowSize),
                             overridenTitle="$n_\\mathrm{split}$" + f" w oknie {windowSize} próbek",
                             overridenYLabel="$n_\\mathrm{split}$ [%]")
        plotPercentageOfSplitTypes([results[0]], 150, cfg)

    return PerformPlotting(
        experimentId="38978a8d-7d9a-4b84-aab8-cc34ea8142a9",
        performPlotting=plottingAction,
        description="classicVfdtCompoundComparison"
    )


def plitSplitTypesTab(results: list[results_plotter.ClassifierResults], dataset: str):
    if results:
        headers = ["typ", "łącznie", "łącznie [\\%]", "$\\delta h > \\epsilon$", "$\\delta h > \\epsilon$ [\\%]",
                   "$\\epsilon < \\tau$",
                   "$\\epsilon < \\tau$ [\\%]"]
        latexTable = []
        for result in results:
            type = "\\texttt{" + results_plotter.translateClassifierType(result.classifierType).replace("VFDT",
                                                                                                        "") + "}"
            n = result.results["accuracy"].shape[0]
            hSplitCounts = np.count_nonzero(result.results["leafSplit"] == 1)
            hSplitCountsPerc = hSplitCounts * 100.0 / n
            tauSplitCounts = np.count_nonzero(result.results["leafSplit"] == 2)
            tauSplitCountsPerc = tauSplitCounts * 100.0 / n

            totalSplitCounts = hSplitCounts + tauSplitCounts
            totalSplitCountsPerc = totalSplitCounts * 100.0 / n

            latexTable.append((type, totalSplitCounts, totalSplitCountsPerc, hSplitCounts, hSplitCountsPerc,
                               tauSplitCounts, tauSplitCountsPerc))

        latexTable = sorted(latexTable, key=lambda x: x[1], reverse=True)

        tau = str(float(results[0].params["tau"]))
        delta = str(float(results[0].params["delta"]))
        nMin = str(int(results[0].params["nMin"]))

        print(stringLatexTable(headers, latexTable,
                               "Zestawienie liczby różnych podziałów drzewa podczas eksperymentów w zależności od typu klasyfikatora \\texttt{VFDT} przy parametrach $N_\\mathrm{min} = " + nMin + "$, $\\delta = \\num{" + delta + "}$, $\\tau = \\num{" + tau + "}$, zbiór: " + dataset + " (liczba próbek = " + str(
                                   n) + ")", f"tab:6-vfdtComparison-splitTypes-{dataset}"))


def vfdtDifferentTypesNoDetectionElecComparison():
    def plottingAction(results: list[results_plotter.ClassifierResults], dataset: str,
                       cfg: results_plotter.PlotPrinterConfig):
        results.sort(key=lambda x: np.average(x.results["nodesDuringTraversalCount"]), reverse=True)

        labelFun = lambda result: results_plotter.translateClassifierType(result.classifierType).replace("VFDT", "")
        prefix = "klasyfikator VFDT"

        printVfdtDefaultPlots(results, dataset, cfg, labelFun, prefix)

        windowSize = 3000
        results_plotter.plot(dataset, results, "leafSplit", cfg,
                             mapper=lambda x: 100.0 * window(np.where(x > 0, 1, 0), windowSize),
                             overridenTitle="$n_\\mathrm{split}$" + f" w oknie {windowSize} próbek",
                             overridenYLabel="$n_\\mathrm{split}$ [%]", labelFun=labelFun, prefix=prefix,
                             printSmall=True)

        for result in results:
            windowSize = 150
            plotPercentageOfSplitTypes([result], windowSize, cfg,
                                       prefix=labelFun(result) + "VFDT", printSmall=True)

        headers = ["typ", "łącznie", "łącznie [\\%]", "heurystyka", "heurystyka [\\%]",
                   "anti-stall",
                   "anti-stall [\\%]"]
        latexTable = []
        for result in results:
            type = labelFun(result)
            n = result.results["accuracy"].shape[0]
            hSplitCounts = np.count_nonzero(result.results["leafSplit"] == 1)
            hSplitCountsPerc = hSplitCounts * 100.0 / n
            tauSplitCounts = np.count_nonzero(result.results["leafSplit"] == 2)
            tauSplitCountsPerc = tauSplitCounts * 100.0 / n

            totalSplitCounts = hSplitCounts + tauSplitCounts
            totalSplitCountsPerc = totalSplitCounts * 100.0 / n

            latexTable.append((type, totalSplitCounts, totalSplitCountsPerc, hSplitCounts, hSplitCountsPerc,
                               tauSplitCounts, tauSplitCountsPerc))

        latexTable = sorted(latexTable, key=lambda x: x[1], reverse=True)

        tau = str(float(results[0].params["tau"]))
        delta = str(float(results[0].params["delta"]))
        nMin = str(int(results[0].params["nMin"]))

        print(stringLatexTable(headers, latexTable,
                               "Zestawienie liczby różnych podziałów drzewa podczas eksperymentów w zależności od typu klasyfikatora \\texttt{VFDT} przy parametrach $N_\\mathrm{min} = " + nMin + "$, $\\delta = \\num{" + delta + "}$, $\\tau = \\num{" + tau + "}$ (liczba próbek = " + str(
                                   n) + ")", "tab:6-vfdtComparisonNoDetection-splitTypes"))

    return PerformPlotting(
        experimentId="4289d444-543d-4fe8-be72-7624b9eb856c",
        performPlotting=plottingAction,
        description="vfdtsComparison"
    )


def vfdtWithOrWithoutParentDisable():
    def plottingAction(results: list[results_plotter.ClassifierResults], dataset: str,
                       cfg: results_plotter.PlotPrinterConfig):
        # TODO filter so legend can show all
        results.sort(key=lambda x: np.average(x.results["nodesDuringTraversalCount"]), reverse=True)

        resultBins = {}
        binKeys = [result.classifierType for result in results]
        binKeys = set([s.replace("NoParentDisable", "") for s in binKeys])

        for key in binKeys:
            withParent = [r for r in results if r.classifierType == key][0]
            withoutParent = [r for r in results if r.classifierType == f"{key}NoParentDisable"][0]

            resultBins[key] = [withParent, withoutParent]

        labelFun = lambda result: results_plotter.translateClassifierType(result.classifierType)

        for key, resultPair in resultBins.items():
            labelF = lambda \
                    x: "nie zabraniając" if "NoParentDisable" in x.classifierType else "zabraniając $X^\\mathrm{up}_{L}$"
            results_plotter.plot(dataset, resultPair, "accuracy", cfg, labelFun=labelF,
                                 prefix=f"klasyfikator {labelFun(resultPair[0])}", printSmall=True)

        headers = ["typ", "zabraniając $X^\\mathrm{up}_{L}$ ", "pole pod krzywą $p_\\mathrm{klas}$"]
        latexTable = []
        for result in results:
            type = labelFun(result).replace("VFDT", "").replace("NP", "")

            if "NoParentDisable" in result.classifierType:
                parentDisableFlag = "nie"
            else:
                parentDisableFlag = "tak"

            fields = np.trapz(result.results["accuracy"], dx=1)

            latexTable.append(
                (type, parentDisableFlag, fields))

        latexTable = sorted(latexTable, key=lambda x: (x[0], -x[2]), reverse=False)

        tau = str(float(results[0].params["tau"]))
        delta = str(float(results[0].params["delta"]))
        nMin = str(int(results[0].params["nMin"]))

        print(stringLatexTable(headers, latexTable,
                               "Zestawienie pola pod krzywą skuteczności klasyfikacji w zależności od typu klasyfikatora \\texttt{VFDT} przy parametrach $N_\\mathrm{min} = " + nMin + "$, $\\delta = \\num{" + delta + "}$, $\\tau = \\num{" + tau + "}$",
                               "tab:6-vfdtComparisonWithOrWithoutParentDisable-auc"))

    return PerformPlotting(
        experimentId="de3166a9-7060-4130-a7e1-12a719a544e5",
        performPlotting=plottingAction,
        description="vfdtsWithOrWithoutParentDisableComparison"
    )


def addPercentagesToResults(results: list[results_plotter.ClassifierResults]):
    for result in results:
        norm = result.results["classifiersAfterClassificationCount"]

        for key in ["weightsLoweringCount", "correctVotesCount", "wrongVotesCount", "deletedClassifiersCount"]:
            result.results[f"{key}%"] = 100.0 * result.results[key] / norm

    return results


class DwmPlotSettings(PlotSettings):
    def __init__(self, accuracy: bool, accuracyWindowed: bool, trainingDuration: bool, classificationDuration: bool,
                 weightsNormalizationAndClassifierDeleteDuration: bool, addClassifierDuration: bool,
                 avgClassifierTTL: bool, classifiersAfterTrainCount: bool):
        super().__init__(accuracy, accuracyWindowed, trainingDuration, classificationDuration)
        self.addClassifierDuration = addClassifierDuration
        self.avgClassifierTTL = avgClassifierTTL
        self.classifiersAfterTrainCount = classifiersAfterTrainCount
        self.weightsNormalizationAndClassifierDeleteDuration = weightsNormalizationAndClassifierDeleteDuration


def printDwmDefaultComparison(results: list[results_plotter.ClassifierResults], dataset: str,
                              cfg: results_plotter.PlotPrinterConfig,
                              labelFun: Callable[[results_plotter.ClassifierResults], str], prefix: str = None,
                              subtitle: str = None,
                              plotSettings: DwmPlotSettings = DwmPlotSettings(
                                  True, True, True, True, True, True, True, True), printDefaults: bool = True):
    if printDefaults is True:
        printDefaultPlots(results, dataset, cfg, labelFun, prefix, subtitle, plotSettings=plotSettings)

    windowSize = 1000
    results_plotter.plot(dataset, results, "weightsNormalizationAndClassifierDeleteDuration", cfg,
                         labelFun=labelFun,
                         mapper=lambda x: window(x, windowSize), subtitle=windowSubtitle(windowSize), prefix=prefix,
                         printSmall=plotSettings.weightsNormalizationAndClassifierDeleteDuration)

    windowSize = 1000
    results_plotter.plot(dataset, results, "addClassifierDuration", cfg,
                         labelFun=labelFun,
                         mapper=lambda x: window(x, windowSize), subtitle=windowSubtitle(windowSize), prefix=prefix,
                         printSmall=plotSettings.addClassifierDuration)

    results_plotter.plot(dataset, results, "avgClassifierTTL", cfg, labelFun=labelFun, prefix=prefix,
                         printSmall=plotSettings.avgClassifierTTL, subtitle=subtitle)
    results_plotter.plot(dataset, results, "classifiersAfterTrainCount", cfg, labelFun=labelFun, prefix=prefix,
                         printSmall=plotSettings.classifiersAfterTrainCount, subtitle=subtitle)


def dwmBetaComparison():
    def plottingAction(results: list[results_plotter.ClassifierResults], dataset: str,
                       cfg: results_plotter.PlotPrinterConfig):
        results = addPercentagesToResults(results)
        results.sort(key=lambda x: x.params["beta"])
        labelFun = lambda result: "$\\beta = $" + str(float(result.params["beta"]))
        prefix = "classicDWM"
        printDwmDefaultComparison(results, dataset, cfg, labelFun, prefix)

    return PerformPlotting(
        experimentId="d9b0a0fb-8fbc-45de-a8fa-a896362c074b",
        performPlotting=plottingAction,
        description="dwmBetaComparison"
    )


def dwmTauComparison():
    def plottingAction(results: list[results_plotter.ClassifierResults], dataset: str,
                       cfg: results_plotter.PlotPrinterConfig):
        results = addPercentagesToResults(results)
        results.sort(key=lambda x: x.params["threshold"])
        labelFun = lambda result: "$\\tau = $" + str(float(result.params["threshold"]))
        prefix = "classicDWM"
        printDwmDefaultComparison(results, dataset, cfg, labelFun, prefix)

    return PerformPlotting(
        experimentId="e5cc16bc-f381-4be9-aa27-5e4e405f657c",
        performPlotting=plottingAction,
        description="dwmTauComparison"
    )


def dwmTComparison():
    def plottingAction(results: list[results_plotter.ClassifierResults], dataset: str,
                       cfg: results_plotter.PlotPrinterConfig):
        results = addPercentagesToResults(results)
        results.sort(key=lambda x: x.params["updateClassifiersEachSamples"])
        labelFun = lambda result: "$T = $" + str(float(result.params["updateClassifiersEachSamples"]))
        prefix = "classicDWM"
        printDwmDefaultComparison(results, dataset, cfg, labelFun, prefix)

        windowSize = 1000
        results_plotter.plot(dataset, results, "weightsLoweringCount%", cfg,
                             labelFun=labelFun,
                             mapper=lambda x: window(x, windowSize), subtitle=windowSubtitle(windowSize), prefix=prefix,
                             printSmall=False)

    return PerformPlotting(
        experimentId="236ddd3a-75ef-411a-aaf8-e13e23dc93e2",
        performPlotting=plottingAction,
        description="dwmTComparison"
    )


def dwmVsEdwmComparison():
    def plottingAction(results: list[results_plotter.ClassifierResults], dataset: str,
                       cfg: results_plotter.PlotPrinterConfig):
        results = addPercentagesToResults(results)
        results.sort(key=lambda x: x.accuracy(), reverse=True)

        labelFun = lambda result: results_plotter.translateClassifierType(result.classifierType)

        def labelingFun(result: results_plotter.ClassifierResults):

            if result.classifierType == "extendedDwm":
                label = results_plotter.translateClassifierType(result.classifierType)
            else:
                beta = result.params["beta"]
                tau = result.params["threshold"]
                T = str(int(result.params["updateClassifiersEachSamples"]))

                translatedType = results_plotter.translateClassifierType(result.classifierType)
                label = f"{translatedType}: $\\beta$ = {beta}, $T$ = {T}"

            return label

        prefix = "DWM"
        printDwmDefaultComparison(results, dataset, cfg, labelingFun, prefix)

        headers = ["typ", "$\\beta$ ", "$\\tau$", "$T$"]
        latexTable = []
        for result in results:
            type = "\\texttt{" + labelFun(result) + "}"

            beta = float(result.params["beta"])
            tau = float(result.params["threshold"])
            T = int(result.params["updateClassifiersEachSamples"])

            latexTable.append((type, beta, tau, T))

        # latexTable = sorted(latexTable, key=lambda x: (x[0], -x[2]), reverse=False)

        print(stringLatexTable(headers, latexTable,
                               "Parametry porównanych klasyfikatorów \\texttt{classicDWM} oraz \\texttt{EDWM}",
                               "tab:6-dwmVsEdwmComparisonParameters"))

    return PerformPlotting(
        experimentId="745c79ac-3838-4f99-91f2-c5bf5e7478c6",
        performPlotting=plottingAction,
        description="dwmVsEdwmComparison"
    )


def splitResults(results: list[results_plotter.ClassifierResults]):
    dwmResults = [s for s in results if "dwm" in s.classifierType or "Dwm" in s.classifierType]
    vfdtResults = [s for s in results if "vfdt" in s.classifierType]

    return dwmResults, vfdtResults


def plotSeparatedStandardDwmAndVfdt(results: list[results_plotter.ClassifierResults], dataset: str,
                                    cfg: results_plotter.PlotPrinterConfig,
                                    vfdtPlotSettings: VfdtPlotSettings = VfdtPlotSettings(
                                        True, True, True, True, True, True),
                                    dwmPlotSettings: DwmPlotSettings = DwmPlotSettings(
                                        True, True, True, True, True, True, True, True)):
    dwmResults = [s for s in results if "dwm" in s.classifierType or "Dwm" in s.classifierType]
    dwmResults = addPercentagesToResults(dwmResults)
    vfdtResults = [s for s in results if "vfdt" in s.classifierType]

    vfdtResults.sort(key=lambda x: np.trapz(x.results["nodesDuringTraversalCount"], dx=1), reverse=True)

    labelFun = lambda x: results_plotter.translateClassifierType(x.classifierType)

    printVfdtDefaultPlots(vfdtResults, dataset, cfg, labelFun, prefix="VFDT", printDefaults=False,
                          plotSettings=vfdtPlotSettings)
    printDwmDefaultComparison(dwmResults, dataset, cfg, labelFun, prefix="DWM", printDefaults=False,
                              plotSettings=dwmPlotSettings)

    return dwmResults, vfdtResults


def printTables(vfdtResults: list[results_plotter.ClassifierResults],
                dwmResults: list[results_plotter.ClassifierResults], dataset: str, captionDataset: str = None,
                mapLatexType: Callable[[str], str] = None, datasetNoTexttt: bool = False):
    n = vfdtResults[0].results["accuracy"].shape[0]

    def latexType(classifierType: str):
        return "\\texttt{" + results_plotter.translateClassifierType(classifierType) + "}"

    def augmentHeaders(headers: list[str]):
        return ["typ"] + headers + ["$p_\\mathrm{klas}$", "$t_\\mathrm{pred}$",
                                    "$t_\\mathrm{ucz}$", "$t_\\mathrm{total}$"]

    def calculateAccuracy(result: results_plotter.ClassifierResults):
        return float(np.mean(result.results["accuracy"]))

    def calculateTime(result: results_plotter.ClassifierResults):
        def rescale(time):
            return time / (10 ** 9)

        clasTime = rescale(result.totalClassificationTime)
        trainTime = rescale(result.totalTrainingTime)
        totalTime = clasTime + trainTime

        return clasTime, trainTime, totalTime

    def caption(classifierGroupType: str):
        if datasetNoTexttt is True:
            tmpDataset = "\\texttt{" + dataset.replace("_", "\\_") + "}"
        else:
            tmpDataset = dataset.replace("_", "\\_")
        return "Zestawienie wyników klasyfikacji - zbiór " + tmpDataset + ", klasyfikator \\texttt{" + classifierGroupType + "}"

    def labelFactory(classifierGroupType: str):
        if captionDataset is not None:
            tmpDataset = captionDataset
        else:
            tmpDataset = dataset
        return "tab:6-defaultResults-" + tmpDataset + "-" + classifierGroupType.lower()

    if vfdtResults:
        vfdtHeaders = augmentHeaders(["$N_\\mathrm{min}$", "$\\delta$", "$\\tau$"])

        table = []
        for result in vfdtResults:
            vfdtType = latexType(result.classifierType)
            if mapLatexType is not None:
                vfdtType = mapLatexType(vfdtType)
            tau = float(result.params["tau"])
            delta = float(result.params["delta"])
            nMin = int(result.params["nMin"])

            pred = calculateAccuracy(result)

            clasTime, trainTime, totalTime = calculateTime(result)

            table.append(
                (vfdtType, nMin, delta, tau, pred, clasTime, trainTime, totalTime))

        groupType = "VFDT"
        print(stringLatexTable(vfdtHeaders, table, caption(groupType), labelFactory(groupType)))

    if dwmResults:
        dwmHeaders = augmentHeaders(["$\\beta$ ", "$\\tau$", "$T$"])
        table = []
        for result in dwmResults:
            dwmType = latexType(result.classifierType)

            beta = float(result.params["beta"])
            tau = float(result.params["threshold"])
            T = int(result.params["updateClassifiersEachSamples"])

            pred = calculateAccuracy(result)

            clasTime, trainTime, totalTime = calculateTime(result)

            table.append(
                (dwmType, beta, tau, T, pred, clasTime, trainTime, totalTime))

        groupType = "DWM"
        print(stringLatexTable(dwmHeaders, table, caption(groupType), labelFactory(groupType)))


def plotPercentageOfNodesTravelledVsAllNodeTreeCounts(results: list[ClassifierResults],
                                                      dataset: str,
                                                      cfg: PlotPrinterConfig,
                                                      labelFun: Callable[[ClassifierResults], str] = None,
                                                      prefix: str = None,
                                                      printSmall: bool = False, start: int = 0):
    if results:
        if cfg.is_set() and not printSmall:
            plt.figure(dpi=1200)
        else:
            plt.figure()

        title = "$n_\\mathrm{nodes\%}$"
        if prefix:
            title = f"{prefix}: {title}"
        title = f"zbiór {dataset} - {title}"
        plt.title(title)

        for result in results:
            values = 100.0 * result.results["nodesDuringTraversalCount"][start:] / result.results["treeSize"][start:]
            x = np.cumsum(np.ones_like(values)) + start
            if labelFun is None:
                plt.plot(x, values)
            else:
                plt.plot(x, values, label=labelFun(result))

        plt.legend()
        plt.ylabel("$n_\\mathrm{nodes\%}$ [%]")
        plt.xlabel("numer próbki")
        if cfg.is_set():
            filename = "nodesDuringTraversalPercentage"
            if prefix:
                filename = f"{prefix}_{filename}"
            plt.savefig(f"{cfg.get_path()}/{filename}.png")
        else:
            plt.show(block=False)


def plottingActionDifferentDatasetsNoDetection(results: list[results_plotter.ClassifierResults], dataset: str,
                                               cfg: results_plotter.PlotPrinterConfig):
    results.sort(key=lambda x: np.trapz(x.results["accuracy"], dx=1), reverse=True)

    labelFun = lambda x: results_plotter.translateClassifierType(x.classifierType)
    printDefaultPlots(results, dataset, cfg, labelFun)

    dwmResults, vfdtResults = plotSeparatedStandardDwmAndVfdt(results, dataset, cfg)

    # plotPercentageOfNodesTravelledVsAllNodeTreeCounts(vfdtResults, dataset, cfg,
    #                                                   lambda x: labelFun(x).replace("VFDT", ""),
    #                                                   printSmall=True, prefix="VFDT")

    plitSplitTypesTab(vfdtResults, dataset)

    printTables(vfdtResults, dwmResults, dataset)


def fixPercentagePlots(results: list[results_plotter.ClassifierResults], dataset: str,
                       cfg: results_plotter.PlotPrinterConfig, start: int = 0):
    results = sorted(results, key=lambda x: np.trapz(x.results["nodesDuringTraversalCount"], dx=1), reverse=True)

    plotPercentageOfNodesTravelledVsAllNodeTreeCounts(results, dataset, cfg,
                                                      lambda x: results_plotter.translateClassifierType(
                                                          x.classifierType).replace("VFDT", ""),
                                                      printSmall=True, prefix="VFDT", start=start)


def airlinesNoDetection():
    return PerformPlotting(
        experimentId="4dece8be-79b6-44e8-b90c-9762a9403c1d",
        performPlotting=plottingActionDifferentDatasetsNoDetection,
        description="airlinesNoDetection"
    )


def airlinesNoDetectionOnlyPercentagePlots():
    return PerformPlotting(
        experimentId="f8eb5234-73a4-42e8-b3bd-a49a9633ab5c",
        performPlotting=lambda x, y, z: fixPercentagePlots(x, y, z, 10000),
        description="airlinesNoDetection"
    )


def movingSquaresNoDetection():
    return PerformPlotting(
        experimentId="cde8392e-90a3-4f71-8f5e-6c71f7c8d296",
        performPlotting=plottingActionDifferentDatasetsNoDetection,
        description="movingSquaresNoDetection"
    )


def movingSquaresNoDetectionOnlyPercentagePlots():
    return PerformPlotting(
        experimentId="d062e95e-d409-4f75-9df7-938efc8ae1a7",
        performPlotting=lambda x, y, z: fixPercentagePlots(x, y, z, 5000),
        description="movingSquaresNoDetection"
    )


def weatherNoDetection():
    return PerformPlotting(
        experimentId="39881d75-5404-4a39-a328-fc353d088100",
        performPlotting=plottingActionDifferentDatasetsNoDetection,
        description="weatherNoDetection"
    )


def weatherNoDetectionOnlyPercentagePlots():
    return PerformPlotting(
        experimentId="87542dd5-7978-4bdf-87e3-0134d5807590",
        performPlotting=lambda x, y, z: fixPercentagePlots(x, y, z, 500),
        description="weatherNoDetection"
    )


def seaBigNoDetection():
    return PerformPlotting(
        experimentId="78e8d9a1-4ebe-45f2-bc66-01c40451334d",
        performPlotting=plottingActionDifferentDatasetsNoDetection,
        description="seaBigNoDetection"
    )


def seaBigNoDetectionOnlyPercentagePlots():
    return PerformPlotting(
        experimentId="4a735e0e-c685-4d0a-90db-46fdb09a38e9",
        performPlotting=lambda x, y, z: fixPercentagePlots(x, y, z, 1000),
        description="seaBigNoDetection"
    )


class AttributeConfig:
    def __init__(self, mean: float, sigma: float):
        self.mean = mean
        self.sigma = sigma


class DriftConfig:
    def __init__(self, attributesConfig: list[AttributeConfig], width: int, position: int):
        self.position = position
        self.width = width
        self.attributeConfig = attributesConfig


class DriftsConfig:
    def __init__(self, text: str):

        match = re.search(r"C(.*?)N", text)
        if match:
            self.nClasses = int(match.group(1))
        else:
            sys.exit("classes number not found")

        match = re.search(r"N(.*?)S", text)
        if match:
            self.nSamples = int(match.group(1))
        else:
            sys.exit("samples number not found")

        texts = text.split("F")

        descriptions = texts[1].split("L")

        descriptionsSplit = descriptions[1].split("T")
        self.description = descriptionsSplit[0]
        self.polishDescription = descriptionsSplit[1].replace("_", " ")

        streams = descriptions[0].split("D")

        def readStream(streamText: str):
            result = []
            for pairStr in streamText.split("A"):
                if pairStr:
                    attributesSplit = pairStr.split("_")
                    result.append(AttributeConfig(float(attributesSplit[0]), float(attributesSplit[1])))

            return result

        self.firstStream = readStream(streams[0])

        self.drifts = []

        for driftText in streams[1:]:
            driftTextSplit = driftText.split("P")
            stream = readStream(driftTextSplit[0])

            positionAndWidth = driftTextSplit[1].split("W")

            self.drifts.append(DriftConfig(stream, position=int(positionAndWidth[0]), width=int(positionAndWidth[1])))

        self.nAttributes = len(self.firstStream)


def readRanges(config: DriftsConfig):
    ranges = []

    sample_sum = 0
    for drift in config.drifts:
        sample_sum += drift.position - drift.width // 2
        if sample_sum >= config.nSamples:
            ranges.append(config.nSamples)
            break
        ranges.append(sample_sum)

    ranges.append(config.nSamples)
    result = set(ranges)
    result = list(result)
    result.sort(reverse=False)

    return result


def detectionsTable(results: list[results_plotter.ClassifierResults], driftsConfig: DriftsConfig, dataset: str,
                    captionPrefix: str = None):
    if results:
        detectionsResults = []

        ranges = readRanges(driftsConfig)
        for result in results:
            if "Detector" in result.classifierType:
                delays = []
                correctDetectionsCounter = 0
                falseDetectionsCounter = 0
                for i in range(len(ranges) - 1):
                    curr = ranges[i]
                    next = ranges[i + 1]

                    arr = result.results["replacedClassifier"][curr:next]

                    delay = int(np.argmax(arr == 1))
                    delays.append(delay)

                    limit = int(1.2 * delay)
                    correctDetectionsCounter += np.count_nonzero(arr[:limit])
                    falseDetectionsCounter += np.count_nonzero(arr[limit:])

                typeTemplated = results_plotter.translateClassifierType(result.classifierType).replace("Wad",
                                                                                                       "").replace(
                    "VFDT", "")
                typeTemplated = "\\texttt{" + typeTemplated + "}"
                alpha = float(result.params["warningFrac"])
                beta = float(result.params["driftFrac"])
                windowSize = int(result.params["windowSize"])
                firstDriftStart = driftsConfig.drifts[0].position - driftsConfig.drifts[0].width // 2
                detectionsCount = int(np.count_nonzero(result.results["replacedClassifier"][firstDriftStart:]))
                correctDetectionsPercentage = 100.0 * correctDetectionsCounter / detectionsCount
                falseDetectionsPercentage = 100.0 * falseDetectionsCounter / detectionsCount
                tmpResult = [typeTemplated, windowSize, alpha, beta, detectionsCount, correctDetectionsPercentage,
                             falseDetectionsPercentage] + delays
                detectionsResults.append(tuple(tmpResult))

        if detectionsResults:
            headers = ["typ", "$w$", "$\\alpha$", "$\\beta$", "$n_\\mathrm{detect}$", "$d_\\mathrm{true}$",
                       "$d_\\mathrm{false}$"]

            cusum = 0
            for drift in driftsConfig.drifts:
                cusum += drift.position
                headers.append("$n_\\mathrm{delay}_{" + str(cusum) + "}$")

            if captionPrefix is None:
                tmpCaption = f"Zestawienie wyników detekcji, zbiór - {dataset}"
            else:
                tmpCaption = f"Zestawienie wyników detekcji: {captionPrefix} - zbiór: {dataset}"
            print(stringLatexTable(headers, detectionsResults, tmpCaption,
                                   f"tab:6-detectionResults-{driftsConfig.description}"))

    else:
        return None


def printVfdtDetectionPerformanceTables(vfdtResults: list[results_plotter.ClassifierResults], dataset: str,
                                        captionDataset: str = None,
                                        mapLatexType: Callable[[str], str] = None, datasetNoTexttt: bool = False,
                                        captionPrefix: str = None):
    n = vfdtResults[0].results["accuracy"].shape[0]

    def latexType(classifierType: str):
        return "\\texttt{" + results_plotter.translateClassifierType(classifierType) + "}"

    def augmentHeaders(headers: list[str]):
        return ["typ"] + headers + ["$p_\\mathrm{klas}$", "$t_\\mathrm{pred}$",
                                    "$t_\\mathrm{ucz}$", "$t_\\mathrm{total}$"]

    def calculateAccuracy(result: results_plotter.ClassifierResults):
        return float(np.mean(result.results["accuracy"]))

    def calculateTime(result: results_plotter.ClassifierResults):
        def rescale(time):
            return time / (10 ** 9)

        clasTime = rescale(result.totalClassificationTime)
        trainTime = rescale(result.totalTrainingTime)
        totalTime = clasTime + trainTime

        return clasTime, trainTime, totalTime

    def caption(classifierGroupType: str):
        if datasetNoTexttt is True:
            tmpDataset = "\\texttt{" + dataset.replace("_", "\\_") + "}"
        else:
            tmpDataset = dataset.replace("_", "\\_")
        if captionPrefix is None:
            return "Zestawienie wyników klasyfikacji - zbiór " + tmpDataset + ", klasyfikator \\texttt{" + classifierGroupType + "}"
        else:
            return f"Zestawienie wyników klasyfikacji: {captionPrefix} - zbiór " + tmpDataset + ", klasyfikator \\texttt{" + classifierGroupType + "}"

    def labelFactory(classifierGroupType: str):
        if captionDataset is not None:
            tmpDataset = captionDataset
        else:
            tmpDataset = dataset
        return "tab:6-defaultResults-" + tmpDataset + "-" + classifierGroupType.lower()

    if vfdtResults:
        vfdtHeaders = augmentHeaders(["$N_\\mathrm{min}$", "$\\delta$", "$\\tau$", "$w$", "$\\alpha$", "$\\beta$"])

        table = []
        for result in vfdtResults:
            vfdtType = latexType(result.classifierType)
            if mapLatexType is not None:
                vfdtType = mapLatexType(vfdtType)
            tau = float(result.params["tau"])
            delta = float(result.params["delta"])
            nMin = int(result.params["nMin"])

            additionalParams = []
            for key in ["windowSize", "warningFrac", "driftFrac"]:
                if key in result.params:
                    if "window" in key:
                        value = int(result.params[key])
                    else:
                        value = float(result.params[key])
                else:
                    value = "-"
                additionalParams.append(value)

            pred = calculateAccuracy(result)

            clasTime, trainTime, totalTime = calculateTime(result)
            tmpRow = [vfdtType, nMin, delta, tau] + additionalParams + [pred, clasTime, trainTime, totalTime]
            table.append(tuple(tmpRow))

    groupType = "VFDT"
    print(stringLatexTable(vfdtHeaders, table, caption(groupType), labelFactory(groupType)))


def detectionPlottingAction(results: list[results_plotter.ClassifierResults], dataset: str,
                            cfg: results_plotter.PlotPrinterConfig,
                            labelFun: Callable[[ClassifierResults], str] = lambda
                                    result: results_plotter.translateClassifierType(result.classifierType),
                            plotPrefix: str = None, captionPrefix: str = None):
    driftsConfig = DriftsConfig(dataset)
    results.sort(key=lambda x: np.trapz(x.results["nodesDuringTraversalCount"], dx=1), reverse=True)

    datasetFormatted = f"{driftsConfig.polishDescription}, $|X| = {driftsConfig.nAttributes}$"

    printVfdtDetectionPerformanceTables(results, datasetFormatted, captionDataset=driftsConfig.description,
                                        mapLatexType=lambda x: x.replace("VFDT", ""), captionPrefix=captionPrefix)
    detectionsTable(results, driftsConfig, datasetFormatted, captionPrefix=captionPrefix)

    printVfdtDefaultPlots(results, datasetFormatted, cfg, labelFun, showDetections=True, prefix=plotPrefix)


def wadParamsComparison():
    def labelFun(result: ClassifierResults):
        if "Detector" in result.classifierType:
            alpha = float(result.params["warningFrac"])
            beta = float(result.params["driftFrac"])
            windowSize = int(result.params["windowSize"])

            params = {"w": windowSize, "\\alpha": alpha, "\\beta": beta}

            paramsArr = []
            for key, value in params.items():
                paramsArr.append(f"${key} = {value}$")

            return ", ".join(paramsArr)
        else:
            return results_plotter.translateClassifierType(result.classifierType)

    return PerformPlotting(
        # experimentId="951dc145-cbb8-4d5f-bb4c-97afc6c99257",
        experimentId="8f045081-fa6b-45fa-9ac3-4c2be6155912",
        performPlotting=lambda x, y, z: detectionPlottingAction(x, y, z, labelFun=labelFun,
                                                                captionPrefix="różne parametry \\texttt{WAD}"),
        description="wadParamsComparison"
    )


def attr1SigmaChanges():
    return PerformPlotting(
        experimentId="de100595-3b6b-48a0-81a8-c4668ef820f8",
        performPlotting=lambda x, y, z: detectionPlottingAction(x, y, z),
        description="wadAttr1SigmaChanges"
    )


def attr2SlowMeanChanges():
    return PerformPlotting(
        experimentId="9ce4aa61-27e0-442b-865a-55993137d796",
        performPlotting=lambda x, y, z: detectionPlottingAction(x, y, z),
        description="wadAttr2SlowMeanChanges"
    )


def attr3SlowMeanChanges():
    return PerformPlotting(
        experimentId="1473580d-6b4c-4e46-98fa-5a5b7a0ed122",
        performPlotting=lambda x, y, z: detectionPlottingAction(x, y, z),
        description="wadAttr3SlowMeanChanges"
    )


def elecWithDetections():
    def plottingAction(results: list[results_plotter.ClassifierResults], dataset: str,
                       cfg: results_plotter.PlotPrinterConfig):
        results.sort(key=lambda x: np.trapz(x.results["nodesDuringTraversalCount"], dx=1), reverse=True)

        labelFun = lambda x: results_plotter.translateClassifierType(x.classifierType)

        prefix = "klasyfikator VFDT"

        printVfdtDetectionPerformanceTables(results, dataset,
                                            mapLatexType=lambda x: x.replace("VFDT", ""))

        printVfdtDefaultPlots(results, dataset, cfg, labelFun, showDetections=True, prefix=prefix)

    return PerformPlotting(
        experimentId="94b6027c-30ee-4597-ab11-25d5e0c7be88",
        performPlotting=plottingAction,
        description="elecWithDetections"
    )


if __name__ == '__main__':
    parser = argparse.ArgumentParser()
    parser.add_argument("--description", action="store", default=None, required=False,
                        help="Description if plots should be printed as files")
    args = parser.parse_args()

    cfg = results_plotter.PlotPrinterConfig("/home/deikare/wut/mgr-flink/thesis/img/6",
                                            args.description)
    saveToFile = False
    experiment = elecWithDetections()

    if saveToFile:
        cfg.description = experiment.description

    experimentId = experiment.experimentId
    print(f"experimentId: {experimentId}")
    resultsPath = f"/home/deikare/wut/vfdtResults/{experimentId}"

    for dataset in os.listdir(resultsPath):
        datasetPath = f"{resultsPath}/{dataset}"

        allResults = []

        for classifierType in os.listdir(datasetPath):
            classifierPath = f"{datasetPath}/{classifierType}"

            allResults.extend(results_plotter.readAllResults(classifierPath))

        if saveToFile and not os.path.exists(cfg.get_path()):
            os.makedirs(cfg.get_path())

        experiment.performPlotting(allResults, dataset, cfg)

        if not cfg.is_set():
            plt.show()
