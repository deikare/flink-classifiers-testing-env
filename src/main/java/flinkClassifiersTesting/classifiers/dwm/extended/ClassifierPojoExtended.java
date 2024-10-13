package flinkClassifiersTesting.classifiers.dwm.extended;

import flinkClassifiersTesting.classifiers.base.ClassifierPojo;
import flinkClassifiersTesting.classifiers.dwm.classic.ClassifierInterface;

public class ClassifierPojoExtended<C extends ClassifierInterface> extends ClassifierPojo<C> {
    protected long wrongClassificationsCounter;

    public ClassifierPojoExtended(C classifier, long sampleNumber) {
        super(classifier, sampleNumber);
        wrongClassificationsCounter = 0;
    }

    public void incWrongClassificationCounter() {
        wrongClassificationsCounter++;
    }

    public long getWrongClassificationsCounter() {
        return wrongClassificationsCounter;
    }
}
