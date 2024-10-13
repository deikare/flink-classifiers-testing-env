package flinkClassifiersTesting.processors.dto;

import java.util.List;

public class ClassificationGlobalParameters {
    private final String jobId;
    private final List<String> dataHeader;


    public ClassificationGlobalParameters(String jobId, List<String> dataHeader) {
        this.jobId = jobId;
        this.dataHeader = dataHeader;
    }

    public List<String> getDataHeader() {
        return dataHeader;
    }

    public String getJobId() {
        return jobId;
    }
}
