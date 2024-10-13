package flinkClassifiersTesting.processors.factory;

import com.google.gson.Gson;
import org.apache.flink.api.common.JobExecutionResult;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.serialization.SimpleStringEncoder;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.connector.file.sink.FileSink;
import org.apache.flink.connector.file.src.FileSource;
import org.apache.flink.core.fs.Path;
import org.apache.flink.formats.csv.CsvReaderFormat;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.dataformat.csv.CsvSchema;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import flinkClassifiersTesting.classifiers.base.BaseClassifier;
import flinkClassifiersTesting.inputs.PlainExample;
import flinkClassifiersTesting.processors.base.BaseProcessFunction;
import flinkClassifiersTesting.processors.dto.ClassificationGlobalParameters;
import flinkClassifiersTesting.processors.dto.ClassificationResult;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class FlinkProcessFactory {
    public static <T extends ClassifierParamsInterface, C extends BaseClassifier, P extends BaseProcessFunction<C>> void runJobs(String datasetPath, long bootstrapSamplesLimit, ProcessFunctionsFromParametersFactory<T, C, P> processFunctionsFromParametersFactory) throws FileNotFoundException {
        final String classEncoderFilepath = datasetPath.replace(".csv", ".txt");

        final Tuple2<Integer, Map<String, Integer>> classNumberAndEncoder = readClassEncoder(classEncoderFilepath);
        final int classNumber = classNumberAndEncoder.f0;
        final Map<String, Integer> encoder = classNumberAndEncoder.f1;
        final int attributesNumber = readAttributesNumber(datasetPath);

        final String experimentId = readExperimentId();

        final FileSource<PlainExample> source = createSource(datasetPath);

        String dataset = readDatasetName(datasetPath);
        final String resultsPath = readResultsPath();
        final String sinkDirectory = String.join("/", resultsPath, experimentId, dataset, processFunctionsFromParametersFactory.getName());

        for (T processFunctionParams : processFunctionsFromParametersFactory.getParameters()) {
            final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

            P processFunction = processFunctionsFromParametersFactory.createProcessFunction(processFunctionParams, classNumber, attributesNumber, dataset, bootstrapSamplesLimit, encoder);

            DataStream<ClassificationResult> stream = env.fromSource(source, WatermarkStrategy.forMonotonousTimestamps(), "fileSource")
                    .keyBy(PlainExample::getId)
                    .process(processFunction)
                    .name(processFunctionsFromParametersFactory.getName());

            final String currentSinkPath = sinkDirectory + "/" + processFunctionParams.directoryName();

            FileSink<String> fileSink = FileSink.<String>forRowFormat(new Path(currentSinkPath), new SimpleStringEncoder<>("UTF-8")).build();

            stream.map(classificationResult -> {
                String fields = classificationResult.performances.stream().map(tuple -> tuple.f1.toString()).collect(Collectors.joining(","));
                return classificationResult.timestamp + "," + classificationResult.exampleClass + "," + classificationResult.predicted + "," + fields;
            }).sinkTo(fileSink);

            System.out.println("Running: " + processFunctionsFromParametersFactory.getName() + ", params: " + processFunctionParams);

            try {
                JobExecutionResult jobExecutionResult = env.execute(processFunctionsFromParametersFactory.getName());
                List<String> csvColumnsHeader = new ArrayList<>(List.of("timestamp", "class", "predicted"));
                csvColumnsHeader.addAll(processFunction.csvColumnsHeader());
                ClassificationGlobalParameters globalParameters = new ClassificationGlobalParameters(jobExecutionResult.getJobID().toString(), csvColumnsHeader);
                Gson gson = new Gson();
                Files.write(Paths.get(currentSinkPath + "/result.json"), gson.toJson(globalParameters).getBytes(StandardCharsets.UTF_8));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    private static int readAttributesNumber(String filepath) throws FileNotFoundException {
        File file = new File(filepath);

        Scanner scanner = new Scanner(file);

        String line = scanner.nextLine();
        scanner.close();

        String[] attributesAsString = line.split(",");
        return attributesAsString.length - 1;
    }

    private static Tuple2<Integer, Map<String, Integer>> readClassEncoder(String filepath) throws FileNotFoundException {
        File file = new File(filepath);

        Map<String, Integer> mappings = new HashMap<>();
        Scanner scanner = new Scanner(file);

        while (scanner.hasNext()) {
            String line = scanner.nextLine();
            String[] encoding = line.split(" ");

            mappings.put(encoding[0], Integer.parseInt(encoding[1]));
        }

        scanner.close();

        return Tuple2.of(mappings.size(), mappings);
    }

    private static String readResultsPath() {
        String resultsPath = System.getenv("RESULTS_DIRECTORY");
        if (resultsPath == null)
            throw new RuntimeException("RESULTS_DIRECTORY variable is not set");
        return resultsPath;
    }

    private static String readExperimentId() {
        String experimentId = System.getenv("EXPERIMENT_ID");
        if (experimentId == null)
            experimentId = UUID.randomUUID().toString();

        return experimentId;
    }

    private static String readDatasetName(String datasetPath) {
        String dataset = new File(datasetPath).getName();
        int lastDotIndex = dataset.lastIndexOf('.');
        if (lastDotIndex > 0 && lastDotIndex < dataset.length() - 1) {
            dataset = dataset.substring(0, lastDotIndex);
        }

        return dataset;
    }

    private static FileSource<PlainExample> createSource(String filepath) {
        CsvReaderFormat<PlainExample> format = CsvReaderFormat.forSchema(CsvSchema.builder().setSkipFirstDataRow(true).addArrayColumn("attributes", "#").addColumn("plainClass").build(), TypeInformation.of(PlainExample.class));

        return FileSource.forRecordStreamFormat(format, Path.fromLocalFile(new File(filepath))).build();
    }
}
