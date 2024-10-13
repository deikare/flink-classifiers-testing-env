import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

public class TimestampTests {
    @TestFactory
    Stream<DynamicTest> testPaddings() {
        return generateLongList(0L, 8L).stream()
                .map(nanoseconds -> {
                    Instant time = Instant.now();
                    String nanosecondsString = String.valueOf(nanoseconds);
                    int prependZeros = 9 - nanosecondsString.length();
                    StringBuilder builder = new StringBuilder();
                    for (int j = 0; j < prependZeros; j++) {
                        builder.append("0");
                    }
                    builder.append(nanosecondsString);
                    String expectedResult = time.getEpochSecond() + builder.toString();
                    return dynamicTest("ns: " + nanosecondsString, () -> assertEquals(expectedResult, time.getEpochSecond() + String.format("%09d", nanoseconds)));
                });
    }

    private static List<Long> generateLongList(long start, long end) {
        List<Long> result = new ArrayList<>();

        for (long i = start; i <= end; i++) {
            long x = i * ((long) Math.pow(10, i));
            result.add(x);
        }

        return result;
    }
}
