import numpy as np


class ClassifierResults:
    def __init__(self, params: dict[str, float], classifierType: str, results: dict[str, np.ndarray],
                 headers: list[str],
                 jobId: str):
        self.params = params
        self.classifierType = classifierType
        # self.results = results
        resultsScaled = {}
        for key, arr in results.items():
            if "Duration" in key or "duration" in key:
                resultsScaled[key] = arr / 1000
            else:
                resultsScaled[key] = arr
        self.results = resultsScaled
        self.headers = headers
        self.jobId = jobId
        self.totalTrainingTime = np.sum(results["trainingDuration"])
        self.totalClassificationTime = np.sum(results["classificationDuration"])

    def accuracy(self) -> float:
        return self.results["accuracy"][-1]

    def accuracies(self):
        return self.results["accuracy"]
