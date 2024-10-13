import numpy as np
from matplotlib import pyplot as plt
import csv
from typing import Callable


def readData(dataset: str):
    basePath = "/home/deikare/wut/streaming-datasets/"
    results = {}
    with open(basePath + dataset + ".csv", newline='') as csvFile:
        reader = csv.reader(csvFile, delimiter=",")
        keys = next(reader)
        for key in keys:
            results[key] = []

        for row in reader:
            for idx, value in enumerate(row):
                results[keys[idx]].append(float(value))

        resultsFormatted = {}
        for key, values in results.items():
            resultsFormatted[key] = np.array(values)

    return resultsFormatted


def plotBuckets(data: dict[str, np.ndarray], bucketSize: int, reducer: Callable[[np.ndarray], float],
                operationType: str):
    bucketsNum = bucketsNumber(data, bucketSize)

    for key, array in data.items():
        if key != "class":
            result = np.zeros((bucketsNum, 2))
            for i in range(bucketsNum):
                startIdx = i * bucketSize
                result[i, 0] = startIdx

                endIdx = startIdx + bucketSize

                subarray = array[startIdx:endIdx]
                result[i, 1] = reducer(subarray)

            plt.step(x=result[:, 0], y=result[:, 1])
            plt.title(f"bucketed - {key}: {operationType}")
            plt.xlabel("sample")
            plt.show()


def plot(data: dict[str, np.ndarray], mapper: Callable[[np.ndarray], np.ndarray] = lambda x: x,
         operationType: str = None):
    for key, array in data.items():
        if key != "class":
            plt.plot(mapper(array))
            title = key if operationType is None else f"{key}: {operationType}"
            plt.title(title)
            plt.xlabel("sample")
            plt.show()


def plotAgainstPrevious(data: dict[str, np.ndarray], bucketSize: int, previousReducer: Callable[[np.ndarray], float],
                        reducer: Callable[[np.ndarray, float], float], operationType: str):
    bucketsNum = bucketsNumber(data, bucketSize)
    for key, array in data.items():
        if key != "class":
            result = np.zeros((bucketsNum, 2))

            begSlice = array[:bucketSize]
            prevValue = previousReducer(begSlice)
            result[0, 1] = reducer(begSlice, prevValue)

            for i in range(1, bucketsNum):
                startIdx = i * bucketSize
                result[i, 0] = startIdx
                endIndex = startIdx + bucketSize

                currSlice = array[startIdx:endIndex]

                result[i, 1] = reducer(currSlice, prevValue)
                prevValue = previousReducer(currSlice)

            plt.step(x=result[:, 0], y=result[:, 1])
            plt.title(f"against previous - {key}: {operationType}")
            plt.xlabel("sample")
            plt.show()


def bucketsNumber(data: dict[str, np.ndarray], bucketSize: int):
    return len(data["class"]) // bucketSize


if __name__ == "__main__":
    data = readData("elec")

    plot(data)
    bucketSize = 1000

    plotBuckets(data, bucketSize, lambda x: np.mean(x), "mean")

    windowSize = 1000
    plot(data, lambda x: np.convolve(x, np.ones(windowSize) / windowSize, mode="valid"), "moving avg")

    # plotBuckets(data, bucketSize, lambda x: np.std(x), "stdDev")
    # plotAgainstPrevious(data, bucketSize, lambda x: np.mean(x), lambda arr, mean: np.sqrt(np.mean((arr - mean) ** 2)),
    #                     "std dev")

    plot(data, lambda x: np.gradient(x), "gradient")
    plot(data, lambda x: np.gradient(np.convolve(x, np.ones(windowSize) / windowSize, mode="valid")),
         "moving gradient")
