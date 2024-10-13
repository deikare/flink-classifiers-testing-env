# Environment for testing classifiers in Flink

## Requirements

* Java 11
* Apache Flink 1.19.1

## Execution

1) Build a JAR using `maven clean package`
2) Run it using `flink run <path to jar>`

You can use predefined execution configuration from Intellij - `Deploy Flink App` or `Deploy Flink App & Plot` but you
have to setup env variables (see [Configuration](#configuration) - set them up in Intellij run configuration):

![Alt text](./readme/how-to-edit-flink-run-configuration.png)

## Configuration

| Environment Variable | Description                                             | Required | Default Value / Example (if required) |
|----------------------|---------------------------------------------------------|----------|---------------------------------------|
| `FLINK_BIN_PATH`     | Absolute path for binaries of Flink 1.19.1              | Yes      | `/home/userxyz/flink-1.19.1/bin`      |
| `RESULTS_DIRECTORY`  | Directory for placing classification results from Flink | Yes      | `/home/userxyz/resultsDir`            |
| `FLINK_ADDRESS`      | URL of Flink cluster for API calls                      | No       | `localhost`                           |
| `FLINK_PORT`         | Port of Flink cluster for API calls                     | No       | `8081`                                |

## Development

New classifier should extend one of two classes:

* [
  `BaseClassifierClassifyAndTrain`](./src/main/java/flinkClassifiersTesting/classifiers/base/BaseClassifierClassifyAndTrain.java)
* [
  `BaseClassifierTrainAndClassify`](./src/main/java/flinkClassifiersTesting/classifiers/base/BaseClassifierTrainAndClassify.java)

Next you have to create classifier operator definition method extending one of two classes:

* [
  `BaseProcessFunctionClassifyAndTrain`](./src/main/java/flinkClassifiersTesting/processors/base/BaseProcessFunctionClassifyAndTrain.java)
* [
  `BaseProcessFunctionTrainAndClassify`](./src/main/java/flinkClassifiersTesting/processors/base/BaseProcessFunctionTrainAndClassify.java)

See for example [`HoeffdingTree`](./src/main/java/flinkClassifiersTesting/classifiers/hoeffding/HoeffdingTree.java)
and [
`VfdtProcessFunction`](./src/main/java/flinkClassifiersTesting/processors/hoeffding/VfdtProcessFunction.java).

<b>Important notice</b>

### Important notice

1) `registerClassifier` method from [
   `BaseProcessFunctionClassifyAndTrain`](./src/main/java/flinkClassifiersTesting/processors/base/BaseProcessFunctionClassifyAndTrain.java) /
   [
   `BaseProcessFunctionTrainAndClassify`](./src/main/java/flinkClassifiersTesting/processors/base/BaseProcessFunctionTrainAndClassify.java)
   is pretty generic - it cannot be templated because `TypeInformation<C>` requires concrete class
2) For easier invocation of operators you can provide `ProcessFunction` factory - see `vfdt(...)` method from  [
   `VfdtProcessFactory`](./src/main/java/flinkClassifiersTesting/processors/factory/vfdt/VfdtProcessFactory.java) and
   then example of classifier invocations in `main()` of [
   `DataStreamJob`](./src/main/java/flinkClassifiersTesting/DataStreamJob.java)

## Datasets

* Dataset file should be in `.csv` format
* Each file should be formatted into:
    * headers row is standard comma separated one (e.g.
      `feat_1,feat_2,feat_3,feat_4,feat_5,feat_6,feat_7,feat_8,target`)
    * each data rows consist of list of attributes and class at the end - attributes should be separated by `#` and
      class by comma (e.g. `19.8#14.0#1019.6#8.4#9.9#15.9#28.9#14.0,0`)
* You should inquire class encoder `String -> Int` in file `<dataset>.txt` in same directory as `<dataset>.csv` -
  example of file for binary classification problem (`yes` and `no` are real class names from `<dataset>.csv` rows):
  ```
  yes 1
  no 0
  ``` 
* You can use [
  `dataset_file_formatter`](./misc/dataset_file_formatter.py) script to format any dataset