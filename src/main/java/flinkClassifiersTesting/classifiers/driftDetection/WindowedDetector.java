package flinkClassifiersTesting.classifiers.driftDetection;

public class WindowedDetector {
    private int[] correctPredictionWindow;
    private int currentWindowSize;
    private int currentIdx;
    private int currentAccuracy;
    private int maxAccuracy;
    private double warningFrac;
    private double driftFrac;

    public WindowedDetector(int windowSize, double warningFrac, double driftFrac) {
        this.warningFrac = warningFrac;
        this.driftFrac = driftFrac;
        currentWindowSize = 0;
        currentIdx = initIdx();
        maxAccuracy = 0;
        currentAccuracy = 0;
        correctPredictionWindow = new int[windowSize];
    }

    public void updateWindow(boolean predictedCorrectly) {
        int idxToReplace = currentIdx + 1;

        if (windowOfCompleteSize()) {
            idxToReplace %= correctPredictionWindow.length;

            int deletedValue = correctPredictionWindow[idxToReplace];
            currentAccuracy -= deletedValue;
        } else currentWindowSize++;

        currentIdx = idxToReplace;

        int toAdd = predictedCorrectly ? 1 : 0;
        correctPredictionWindow[currentIdx] = toAdd;
        currentAccuracy += toAdd;
    }

    public void updateMaxAccuracy() {
        if (windowOfCompleteSize() && currentAccuracy > maxAccuracy)
            maxAccuracy = currentAccuracy;
    }

    public boolean warningDetected() {
        return detect(warningFrac);
    }

    public boolean driftDetected() {
        return detect(driftFrac);
    }

    private boolean detect(double frac) {
        return windowOfCompleteSize() && (double) currentAccuracy < ((double) maxAccuracy) * frac;
    }

    private boolean windowOfCompleteSize() {
        return currentWindowSize == correctPredictionWindow.length;
    }

    public void clearWindow() {
        currentAccuracy = 0;
        maxAccuracy = 0;
        currentIdx = initIdx();
        currentWindowSize = 0;
    }

    private int initIdx() {
        return -1;
    }
}
