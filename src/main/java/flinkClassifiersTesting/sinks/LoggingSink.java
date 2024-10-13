package flinkClassifiersTesting.sinks;

import org.apache.flink.streaming.api.functions.sink.SinkFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoggingSink implements SinkFunction<String> {
    public final Logger logger = LoggerFactory.getLogger(LoggingSink.class);

    public LoggingSink() {
    }

    @Override
    public void invoke(String value, Context context) throws Exception {
        logger.info(value);
    }
}
