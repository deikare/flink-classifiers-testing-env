package flinkClassifiersTesting.classifiers.helpers;

import java.time.Duration;
import java.time.Instant;

public class Helpers {
    public static <A extends Comparable<A>> int getIndexOfHighestValue(A[] tab) {
        int resultIndex = 0;
        A max = tab[0];

        for (int i = 1; i < tab.length; i++) {
            A comparedValue = tab[i];
            if (comparedValue.compareTo(max) > 0) {
                resultIndex = i;
                max = comparedValue;
            }
        }

        return resultIndex;
    }

    public static long toNow(Instant start) {
        return Duration.between(start, Instant.now()).toNanos();
    }

    public static String timestampTrailingZeros(Instant start) {
        return start.getEpochSecond() + String.format("%09d", start.getNano());
    }
}
