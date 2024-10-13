import org.junit.Test;
import flinkClassifiersTesting.processors.dto.ClassificationResult;
import flinkClassifiersTesting.processors.factory.dwm.DwmClassifierParams;
import flinkClassifiersTesting.processors.factory.vfdt.VfdtClassifierParams;

import static org.apache.flink.types.PojoTestUtils.assertSerializedAsPojo;

public class PojoSerializationTests {
    @Test
    public void vfdtClassifierParamsIsSerializable() {
        assertSerializedAsPojo(VfdtClassifierParams.class);
    }

    @Test
    public void dwmClassifierParamsIsSerializable() {
        assertSerializedAsPojo(DwmClassifierParams.class);
    }

    @Test
    public void classificationResultIsSerializable() {
        assertSerializedAsPojo(ClassificationResult.class);
    }
}
