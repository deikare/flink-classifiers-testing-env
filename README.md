# Environment for Testing Classifiers in Flink

This project provides an environment to test classifiers in **Apache Flink**.

## Requirements

- **Java 11**
- **Apache Flink 1.19.1**

## Execution

1. Build the JAR file using the following Maven command:

    ```bash
    mvn clean package
    ```

2. Run the JAR using Flink:

    ```bash
    flink run <path-to-jar>
    ```

Alternatively, you can use the predefined execution configurations in IntelliJ:

- **Deploy Flink App**
- **Deploy Flink App & Plot**

> **Note:** You need to configure environment variables in IntelliJ's run configuration (
> see [Configuration](#configuration)).

![IntelliJ Flink Run Configuration](./readme/how-to-edit-flink-run-configuration.png)

## Configuration

| Environment Variable | Description                                          | Required | Default Value / Example (if required) |
|----------------------|------------------------------------------------------|----------|---------------------------------------|
| `FLINK_BIN_PATH`     | Absolute path of Flink 1.19.1 binaries               | Yes      | `/home/userxyz/flink-1.19.1/bin`      |
| `RESULTS_DIRECTORY`  | Directory for placing Flink's classification results | Yes      | `/home/userxyz/resultsDir`            |
| `FLINK_ADDRESS`      | URL of Flink cluster for API calls                   | No       | `localhost`                           |
| `FLINK_PORT`         | Port of Flink cluster for API calls                  | No       | `8081`                                |

## Development

### Classifier

To implement a new classifier, extend one of the following base classes:

- [
  `BaseClassifierClassifyAndTrain`](./src/main/java/flinkClassifiersTesting/classifiers/base/BaseClassifierClassifyAndTrain.java)
- [
  `BaseClassifierTrainAndClassify`](./src/main/java/flinkClassifiersTesting/classifiers/base/BaseClassifierTrainAndClassify.java)

### Classifier operator

Next, create a class to define your classifier operator, extending one of these classes:

* [
  `BaseProcessFunctionClassifyAndTrain`](./src/main/java/flinkClassifiersTesting/processors/base/BaseProcessFunctionClassifyAndTrain.java)
* [
  `BaseProcessFunctionTrainAndClassify`](./src/main/java/flinkClassifiersTesting/processors/base/BaseProcessFunctionTrainAndClassify.java)

See for example [`HoeffdingTree`](./src/main/java/flinkClassifiersTesting/classifiers/hoeffding/HoeffdingTree.java)
and [
`VfdtProcessFunction`](./src/main/java/flinkClassifiersTesting/processors/hoeffding/VfdtProcessFunction.java).

### Important notice

1. The `registerClassifier` method from [
   `BaseProcessFunctionClassifyAndTrain`](./src/main/java/flinkClassifiersTesting/processors/base/BaseProcessFunctionClassifyAndTrain.java)
   and [
   `BaseProcessFunctionTrainAndClassify`](./src/main/java/flinkClassifiersTesting/processors/base/BaseProcessFunctionTrainAndClassify.java)
   is generic, as `TypeInformation<C>` requires a concrete class.

2. For easier invocation of operators, provide a `ProcessFunction` factory. For example, see the `vfdt(...)` method in [
   `VfdtProcessFactory`](./src/main/java/flinkClassifiersTesting/processors/factory/vfdt/VfdtProcessFactory.java) and
   the classifier factory invocations in the `main()` method of [
   `DataStreamJob`](./src/main/java/flinkClassifiersTesting/DataStreamJob.java).

## Datasets

The dataset files used should follow these conventions:

- **Format:** `.csv`
- **Headers:** The first row should be a standard comma-separated list of features (e.g.,
  `period,nswprice,nswdemand,vicprice,vicdemand,transfer,class`).
- **Data Rows:** Each row should list the feature values separated by `#` and the class label separated by a comma (
  e.g., `0.0#0.0564#0.439155#0.003467#0.422915#0.414912,1`).

### Class Encoders

For the class labels, you must provide a corresponding `.txt` file in the same directory as the dataset. This file
should map the class names to integer values. Example for a binary classification dataset:

**`<dataset>.txt`:**

```
yes 1
no 0
```

### Dataset Formatter Script

You can use the [`dataset_file_formatter`](./misc/dataset_file_formatter.py) script to format any dataset into the
required structure.
