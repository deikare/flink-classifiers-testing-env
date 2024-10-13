import math

import numpy as np
from typing import Callable
import matplotlib.pyplot as plt
from tabulate import tabulate
from matplotlib.ticker import MaxNLocator


class GaussianConfig:
    def __init__(self, mean: float, std_dev: float):
        self.mean = mean
        self.std_dev = std_dev


class ClassGetter:
    def __init__(self, fun: Callable[[np.ndarray], int], description: str):
        self.fun = fun
        self.description = description.lower()


class StreamConfig:
    def __init__(self, attr_configs: list[GaussianConfig]):
        self.attr_configs = attr_configs


class DriftConfig:
    def __init__(self, stream: StreamConfig, position: int, width: int):
        self.stream = stream
        self.position = position
        self.width = width


class Config:
    def __init__(self, first_stream: StreamConfig, drifts: list[DriftConfig], class_getter: ClassGetter, seed: int,
                 description: str, polishDescription: str):
        self.polishDescription = polishDescription
        self.description = description
        self.class_getter = class_getter
        self.first_stream = first_stream
        self.drifts = drifts
        self.seed = seed


class Generator:
    def __init__(self, config: Config):
        self.first_stream = config.first_stream
        self.drifts = config.drifts

        self.rng = np.random.default_rng(config.seed)
        self.class_getter = config.class_getter.fun

    def take(self, n: int):
        current_stream = self.first_stream
        current_drift_idx = 0
        current_drift = self.drifts[0]

        replace_drift_idx = current_drift.position + current_drift.width
        position = current_drift.position

        attributes_num = len(self.first_stream.attr_configs)
        attributes = np.zeros((n, attributes_num))
        classes = []

        for i in range(n):
            v = -4.0 * float(i - position) / float(current_drift.width)
            probability_drift = 1.0 / (1.0 + math.exp(v))
            if self.rng.random() > probability_drift:
                stream = current_stream
            else:
                stream = current_drift.stream

            x = []
            for j in range(attributes_num):
                config = stream.attr_configs[j]
                x.append(float(self.rng.normal(loc=config.mean, scale=config.std_dev, size=1)))

            attributes[i, :] = np.array(x)
            classes.append(self.class_getter(attributes[:(i + 1), :]))

            if i >= replace_drift_idx and current_drift_idx != len(self.drifts) - 1:
                current_stream = current_drift.stream

                current_drift_idx += 1
                current_drift = self.drifts[current_drift_idx]

                position = i + current_drift.position
                replace_drift_idx = position + current_drift.width

        return attributes, classes


def generatePlotTemplates(nAttributes: int, nClasses: int, config: Config,
                          directory: str):  # TODO add stream info + position + widths
    def generatePlotTemplate(plotName: str, caption: str, fracWidth: str = "0.316"):
        label = config.description + "-" + plotName
        lines = [
            "\\begin{subfigure}{" + f"{fracWidth}\\textwidth" + "}",
            "\t\\centering",
            "\t\\includegraphics[width=1\\textwidth]{" + f"{directory}/{config.description}/{plotName}" + "}",
            "\t\\caption{" + caption + "}",
            "\t\\label{fig:6-wadDatasets-" + label + "}",
            "\\end{subfigure}"
        ]

        return "\n".join(lines)

    subfigureTemplates = []
    for attrIdx in range(1, nAttributes + 1):
        subfigureTemplates.append(generatePlotTemplate(f"attr{attrIdx}", f"Wartości atrybutu {attrIdx}"))

    subfigureTemplates.append(generatePlotTemplate("classes", "Klasy próbek"))
    subfigureTemplates.append(generatePlotTemplate("classCounts", "Częstość klas"))
    subfigureTemplates.append(generatePlotTemplate("classesPerStream", "Częstość klas w strumieniach"))

    begin = [
        "\\begin{figure}[h!]",
        "\t\\centering"
    ]

    caption = "Podsumowanie zbioru do sprawdzania detektora \\texttt{WAD}, $|X| = " + str(
        nAttributes) + "$, liczba klas = " + str(nClasses) + ", " + config.polishDescription
    end = [
        "\t\\caption{" + caption + "}",
        "\t\\label{fig:6-wadDatasets-" + config.description + "-grouped}",
        "\\end{figure}"
    ]

    return "\n".join(begin + subfigureTemplates + end)


def plot(attributes: np.ndarray, classes: list[int], config: Config, save_plots: bool, directory: str):
    samples, n_attributes = attributes.shape

    ranges = []

    sample_sum = 0
    for factory in config.drifts:
        sample_sum += factory.position
        if sample_sum >= samples:
            ranges.append(samples)
            break
        ranges.append(sample_sum)

    if ranges[-1] != samples:
        ranges.append(samples)

    ranges_with_zero = [0] + ranges

    tabular_results = []

    prefix = f"{config.polishDescription}, $|X| = {n_attributes}$"

    for idx, column in enumerate(attributes.T):
        plt.figure()
        plt.title(f"{prefix} - atrybut {idx + 1}")
        plt.plot(column, label="wartość atrybutu")
        plt.xlabel("próbka")

        for slice_idx in range(len(ranges)):
            start_idx = ranges_with_zero[slice_idx]
            end_idx = ranges_with_zero[slice_idx + 1]

            attr_slice = column[start_idx:end_idx]

            mean = float(np.mean(attr_slice))
            plt.plot(range(start_idx, end_idx), np.zeros((end_idx - start_idx, 1)) + mean, label="średnia",
                     color="orange")

            std_dev = np.std(attr_slice)

            tabular_results.append([idx, start_idx, end_idx, mean, std_dev])

        for streamChange in ranges[:-1]:
            plt.axvline(x=streamChange, linestyle=":", linewidth=1)

        cumulative_sum = np.cumsum(column)
        indices = np.arange(1, samples + 1)
        # plt.plot(cumulative_sum / indices, label="walking mean")

        handles, labels = plt.gca().get_legend_handles_labels()
        unique_labels = dict(zip(labels, handles))
        plt.legend(unique_labels.values(), unique_labels.keys())

        if save_plots:
            plt.savefig(f"{directory}/{config.description}/attr{idx + 1}.png")
        else:
            plt.show()

    # print(tabulate(tabular_results, headers=["attribute", "start", "end", "mean", "std_dev"]))

    plt.figure()
    plt.title(f"{prefix} - klasa próbki")
    plt.plot(classes)
    for streamChange in ranges[:-1]:
        plt.axvline(x=streamChange, linestyle=":", linewidth=1)
    plt.xlabel("próbka")
    plt.ylabel("klasa próbki")
    plt.gca().yaxis.set_major_locator(MaxNLocator(integer=True))

    if save_plots:
        plt.savefig(f"{directory}/{config.description}/classes.png")
    else:
        plt.show()

    npClasses = np.array(classes)
    totalUniqueClasses, counts = np.unique(npClasses, return_counts=True)
    counts = counts.astype(np.float64) * 100.0 / float(len(classes))
    plt.figure()
    plt.bar(totalUniqueClasses, counts)
    plt.xlabel("klasa")
    plt.ylabel("częstość klas [%]")
    plt.title(f"{prefix} - częstość klas")
    plt.gca().xaxis.set_major_locator(MaxNLocator(integer=True))

    if save_plots:
        plt.savefig(f"{directory}/{config.description}/classCounts.png")
    else:
        plt.show()

    n_classes = totalUniqueClasses.shape[0]

    barWidth = 0.2
    numGroups = n_classes

    positions = np.array(ranges)
    offset = barWidth * np.arange(numGroups)

    buckets = []
    for classIdx in range(n_classes):
        buckets.append((np.zeros((len(ranges))), np.zeros(len(ranges))))

    bucketsPerStream = []

    for i in range(len(ranges)):
        startSample = ranges_with_zero[i]
        endSample = ranges_with_zero[i + 1]
        currSamples = endSample - startSample

        classesSlice = npClasses[startSample:endSample]

        tmpCounts = []  # TODO remove after figuring out why buckets don't work
        for classIdx in range(n_classes):
            counts = float(np.count_nonzero(classesSlice == classIdx)) * 100.0 / float(currSamples)

            buckets[classIdx][0][i] = positions[i] + offset[classIdx]
            buckets[classIdx][1][i] = counts
            tmpCounts.append(counts)

        # print(f"range <{startSample}, {endSample}): {tmpCounts}")

        if i == 0:
            startSample = 1
        bucketsPerStream.append((startSample, endSample, tmpCounts))

    # Liczba strumieni i klas
    num_streams = len(bucketsPerStream)
    num_classes = len(bucketsPerStream[0][2])

    # Ustalenie odstępów między grupami słupków (dla strumieni)
    bar_width = 0.2
    indices = np.arange(num_streams)

    # Tworzenie wykresu
    plt.figure(figsize=(10, 6))

    bucketsPerClass = []
    for classIdx in range(num_classes):
        result = []
        for stream in bucketsPerStream:
            result.append((stream[0], stream[1], stream[2][classIdx]))

        bucketsPerClass.append(result)

    # Iterowanie przez każdą krotkę w liście danych
    for i, perClass in enumerate(bucketsPerClass):
        frequencies = []
        for stream in perClass:
            frequencies.append(stream[2])
        # Przesunięcie słupków dla każdego strumienia
        bars = plt.bar(indices + i * bar_width * num_classes, frequencies, bar_width, label=f'klasa {i}')
        for bar in bars:
            yval = bar.get_height()
            plt.text(bar.get_x() + bar.get_width() / 2, yval, float(yval), ha='center', va='bottom')
    # Ustawienie etykiet osi x (dla strumieni)
    xticks_positions = indices + bar_width * (num_classes * num_streams - 1) / (2 * num_streams)
    plt.xticks(xticks_positions,
               [f'próbki {bucketsPerStream[j][0]} - {bucketsPerStream[j][1]}' for j in range(num_streams)])

    # Dodanie legendy
    plt.legend(title='Klasa')

    # Dodanie etykiet i tytułu
    plt.xlabel('strumień')
    plt.ylabel('częstość [%]')
    plt.title(f'{prefix} - częstość klas w różnych strumieniach')

    if save_plots:
        plt.savefig(f"{directory}/{config.description}/classesPerStream.png")
    else:
        plt.show()

    # print(generatePlotTemplates(n_attributes, n_classes, config, directory))


def get_name(attributes: np.ndarray, classes: list[int], config: Config):
    samples, attributes_num = attributes.shape
    classes_num = len(set(classes))
    name = f"C{classes_num}N{samples}S{config.seed}"

    def stream_shortcut(stream: StreamConfig):
        result = ""
        for stream_config in stream.attr_configs:
            result += f"A{stream_config.mean}_{stream_config.std_dev}"

        return result

    name += f"F{stream_shortcut(config.first_stream)}"

    for drift in config.drifts:
        name += f"D{stream_shortcut(drift.stream)}P{drift.position}W{drift.width}"

    descriptionFormatted = config.description.replace(" ", "_")
    name += f"L{descriptionFormatted}"

    polishDescriptionFormatted = config.polishDescription.replace(" ", "_")
    name += f"T{polishDescriptionFormatted}"

    return name


class SineConst:
    def __init__(self, T: int = 20, A: float = 1.0):
        self.A = A
        self.T = T
        self.omega = 2 * math.pi / float(T)

    def sine(self, x: np.ndarray):
        data_length, _ = x.shape
        return self.A * math.sin(self.omega * data_length)

    def description(self):
        return f"ampl{self.A}-period{self.T}"


class WalkingMeanTracker(SineConst):
    def __init__(self, to_compare: float = 3.5, T: int = 20, A: float = 1.0):
        super().__init__(T, A)
        self.to_compare = to_compare
        self.mean = 0.0
        self.n = 0

    def class_getter_fun(self, x: np.ndarray):
        if self.n >= 1:
            loc_sum = self.mean * self.n + x[-1]
            self.n += 1

            self.mean = loc_sum / self.n
        else:
            self.n += 1

        data_length, _ = x.shape

        test_mean = self.mean + super().sine(x)

        if test_mean <= self.to_compare:
            return 1
        else:
            return 0

    def description(self):
        return f"same-{super().description()}-compare{self.to_compare}"


class WindowedMeanTracker(SineConst):
    def __init__(self, window_size: int = 100, to_compare: float = 2.0, T: int = 20, A: float = 1.0):
        super().__init__(T, A)
        self.to_compare = to_compare
        self.window_size = window_size

    def class_getter_fun(self, x: np.ndarray):
        data_length, _ = x.shape
        if data_length <= self.window_size:
            mean = np.mean(x)
        else:
            mean = np.mean(x[-self.window_size:])

        test_mean = mean + super().sine(x)

        if test_mean <= self.to_compare:
            return 1
        else:
            return 0

    def description(self):
        return f"windowed-{self.window_size}-compare{self.to_compare}-{super().description()}"


def attr_1_mean_changes():
    mean_tracker = WalkingMeanTracker(A=2, T=5000, to_compare=3)

    class_getter = ClassGetter(mean_tracker.class_getter_fun,
                               mean_tracker.description())

    return Config(
        first_stream=StreamConfig([GaussianConfig(0.0, 10.0)]),
        drifts=[
            DriftConfig(StreamConfig([GaussianConfig(5.0, 1.0)]), 5000, 100),
            DriftConfig(StreamConfig([GaussianConfig(10.0, 1.0)]), 5000, 100)
        ],
        class_getter=class_getter,
        seed=10,
        description="attr_1_mean_changes",
        polishDescription="małe zmiany średniej"
    )


def attr_1_mean_changes_v2():
    mean_tracker = WalkingMeanTracker(A=3, T=8500, to_compare=3)

    class_getter = ClassGetter(mean_tracker.class_getter_fun,
                               mean_tracker.description())

    return Config(
        first_stream=StreamConfig([GaussianConfig(0.0, 10.0)]),
        drifts=[
            DriftConfig(StreamConfig([GaussianConfig(5.0, 1.0)]), 5000, 100),
            DriftConfig(StreamConfig([GaussianConfig(10.0, 1.0)]), 5000, 100)
        ],
        class_getter=class_getter,
        seed=10,
        description="attr_1_mean_changes",
        polishDescription="małe zmiany średniej"
    )


def attr_1_larger_mean_changes():
    mean_tracker = WalkingMeanTracker(A=5, T=500, to_compare=10)

    class_getter = ClassGetter(mean_tracker.class_getter_fun,
                               mean_tracker.description())

    return Config(
        first_stream=StreamConfig([GaussianConfig(0.0, 10.0)]),
        drifts=[
            DriftConfig(StreamConfig([GaussianConfig(50.0, 10.0)]), 5000, 100),
            DriftConfig(StreamConfig([GaussianConfig(-50.0, 10.0)]), 5000, 100)
        ],
        class_getter=class_getter,
        seed=10,
        description="attr_1_larger_mean_changes",
        polishDescription="duże zmiany średniej"
    )


def attr_1_slow_mean_changes():
    result = attr_1_mean_changes()

    for i in range(len(result.drifts)):
        result.drifts[i].width = 2500

    result.description = "attr_1_slow_mean_changes"
    result.polishDescription = "wolne zmiany średniej"

    return result


def attr_1_slower_mean_changes():
    result = attr_1_mean_changes()

    for i in range(len(result.drifts)):
        result.drifts[i].position = 10000
        result.drifts[i].width = 5000

    result.description = "attr_1_slower_mean_changes"
    result.polishDescription = "wolne zmiany średniej"

    return result


def attr_1_std_dev_changes():
    mean_tracker = WindowedMeanTracker(A=0.5, T=5000, to_compare=0.0, window_size=2500)

    class_getter = ClassGetter(mean_tracker.class_getter_fun,
                               mean_tracker.description())

    return Config(
        first_stream=StreamConfig([GaussianConfig(0.0, 1.0)]),
        drifts=[
            DriftConfig(StreamConfig([GaussianConfig(0.0, 15.0)]), 5000, 100),
            DriftConfig(StreamConfig([GaussianConfig(0.0, 100.0)]), 5000, 100)
        ],
        class_getter=class_getter,
        seed=10,
        description="attr_1_std_dev_changes",
        polishDescription="zmiany $\\sigma$"
    )


class WalkingMeanNConfig:
    def __init__(self, weights: list[float], A: float, T: int, to_compare: float):
        self.to_compare = to_compare
        self.T = T
        self.A = A
        self.weights = weights


class WalkingMeanTrackerN:
    def __init__(self, config: WalkingMeanNConfig):
        self.weights = config.weights
        sum_weights = sum(self.weights)
        for i in range(len(self.weights)):
            self.weights[i] /= sum_weights

        self.trackers = [WalkingMeanTracker() for _ in self.weights]
        self.sine = SineConst(A=config.A, T=config.T)
        self.to_compare = config.to_compare

    def description(self):
        results = ""

        for i in range(len(self.weights)):
            results += f"-weight{self.weights[i]}"

        results += "-" + self.sine.description()
        results += f"-comp{self.to_compare}"
        return results

    def class_getter_fun(self, x: np.ndarray):
        avg = 0.0

        for i in range(len(self.weights)):
            tmp_x = x[:, i]
            tmp_x = tmp_x.reshape(-1, 1)
            self.trackers[i].class_getter_fun(tmp_x)

            avg += self.weights[i] * self.trackers[i].mean

        if avg + self.sine.sine(x) <= self.to_compare:
            return 1
        else:
            return 0


def attr_2_mean_changes():
    mean_tracker = WalkingMeanTrackerN(WalkingMeanNConfig([0.4, 0.6], 0.9, 5000, 5.0))

    class_getter = ClassGetter(mean_tracker.class_getter_fun,
                               mean_tracker.description())

    return Config(
        first_stream=StreamConfig([
            GaussianConfig(0.0, 1.0),
            GaussianConfig(10.0, 1.0)]
        ),
        drifts=[
            DriftConfig(StreamConfig([
                GaussianConfig(5.0, 1.0),
                GaussianConfig(5.0, 1.0)
            ]), 5000, 100),
            DriftConfig(StreamConfig([
                GaussianConfig(10.0, 1.0),
                GaussianConfig(0.0, 1.0)
            ]), 5000, 100)
        ],
        class_getter=class_getter,
        seed=10,
        description="attr_2_mean_changes",
        polishDescription="małe zmiany średniej"
    )


def attr_2_large_mean_changes():
    mean_tracker = WalkingMeanTrackerN(WalkingMeanNConfig([0.8, 0.2], 10, 3000, 45.0))

    class_getter = ClassGetter(mean_tracker.class_getter_fun,
                               mean_tracker.description())

    return Config(
        first_stream=StreamConfig([
            GaussianConfig(0.0, 10.0),
            GaussianConfig(150.0, 10.0)]
        ),
        drifts=[
            DriftConfig(StreamConfig([
                GaussianConfig(50.0, 10.0),
                GaussianConfig(50.0, 10.0)
            ]), 5000, 100),
            DriftConfig(StreamConfig([
                GaussianConfig(100.0, 10.0),
                GaussianConfig(0.0, 10.0)
            ]), 5000, 100)
        ],
        class_getter=class_getter,
        seed=10,
        description="attr_2_large_mean_changes",
        polishDescription="duże zmiany średniej"
    )


def attr_2_slow_changes():
    result = attr_2_mean_changes()
    width = 5000
    for i in range(len(result.drifts)):
        result.drifts[i].width = width

    result.description = "attr_2_slow_changes"
    result.polishDescription = "wolne zmiany średniej"

    return result


def attr_2_std_dev_changes():
    mean_tracker = WalkingMeanTrackerN(WalkingMeanNConfig([0.5, 0.5], 1.25, 5000, 75.3))

    class_getter = ClassGetter(mean_tracker.class_getter_fun,
                               mean_tracker.description())

    return Config(
        first_stream=StreamConfig([
            GaussianConfig(0.0, 10.0),
            GaussianConfig(150.0, 100.0)]
        ),
        drifts=[
            DriftConfig(StreamConfig([
                GaussianConfig(0.0, 50.0),
                GaussianConfig(150.0, 50.0)
            ]), 5000, 100),
            DriftConfig(StreamConfig([
                GaussianConfig(0.0, 100.0),
                GaussianConfig(150.0, 10.0)
            ]), 5000, 100)
        ],
        class_getter=class_getter,
        seed=10,
        description="attr_2_std_dev_changes",
        polishDescription="zmiany $\\sigma$"
    )


def attr_3_mean_changes():
    mean_tracker = WalkingMeanTrackerN(WalkingMeanNConfig([1, 1.5, 1], 0.75, 5000, 5))

    class_getter = ClassGetter(mean_tracker.class_getter_fun,
                               mean_tracker.description())

    return Config(
        first_stream=StreamConfig([
            GaussianConfig(0.0, 1.0),
            GaussianConfig(2.5, 1.0),
            GaussianConfig(10.0, 1.0)]
        ),
        drifts=[
            DriftConfig(StreamConfig([
                GaussianConfig(5.0, 1.0),
                GaussianConfig(7.5, 1.0),
                GaussianConfig(5.0, 1.0)
            ]), 5000, 100),
            DriftConfig(StreamConfig([
                GaussianConfig(10.0, 1.0),
                GaussianConfig(12.5, 1.0),
                GaussianConfig(0.0, 1.0)
            ]), 5000, 100)
        ],
        class_getter=class_getter,
        seed=10,
        description="attr_3_mean_changes",
        polishDescription="małe zmiany średniej"
    )


def attr_3_large_mean_changes():
    mean_tracker = WalkingMeanTrackerN(WalkingMeanNConfig([2, 1, 0.75], 1, 2500, 43))

    class_getter = ClassGetter(mean_tracker.class_getter_fun,
                               mean_tracker.description())

    return Config(
        first_stream=StreamConfig([
            GaussianConfig(0.0, 2.0),
            GaussianConfig(20.5, 2.0),
            GaussianConfig(200.0, 2.0)]
        ),
        drifts=[
            DriftConfig(StreamConfig([
                GaussianConfig(5.0, 2.0),
                GaussianConfig(70.5, 2.0),
                GaussianConfig(100.0, 2.0)
            ]), 5000, 100),
            DriftConfig(StreamConfig([
                GaussianConfig(10.0, 2.0),
                GaussianConfig(120.5, 2.0),
                GaussianConfig(0.0, 2.0)
            ]), 5000, 100)
        ],
        class_getter=class_getter,
        seed=10,
        description="attr_3_large_mean_changes",
        polishDescription="duże zmiany średniej"
    )


def attr_3_slow_changes():
    result = attr_3_mean_changes()

    for i in range(len(result.drifts)):
        result.drifts[i].width = result.drifts[i].position

    result.description = "attr_3_slow_changes"
    result.polishDescription = "wolne zmiany średniej"

    return result


def attr_3_std_dev_changes():
    mean_tracker = WalkingMeanTrackerN(WalkingMeanNConfig([1, 1, 1], 0.4, 5000, 100.0))

    class_getter = ClassGetter(mean_tracker.class_getter_fun,
                               mean_tracker.description())

    return Config(
        first_stream=StreamConfig([
            GaussianConfig(0.0, 10.0),
            GaussianConfig(100.0, 50.0),
            GaussianConfig(200.0, 100.0)]
        ),
        drifts=[
            DriftConfig(StreamConfig([
                GaussianConfig(0.0, 50.0),
                GaussianConfig(100.0, 100.0),
                GaussianConfig(200.0, 50.0)
            ]), 5000, 100),
            DriftConfig(StreamConfig([
                GaussianConfig(0.0, 100.0),
                GaussianConfig(100.0, 10.0),
                GaussianConfig(200.0, 10.0)
            ]), 5000, 100)
        ],
        class_getter=class_getter,
        seed=10,
        description="attr_3_std_dev_changes",
        polishDescription="zmiany $\\sigma$"
    )
