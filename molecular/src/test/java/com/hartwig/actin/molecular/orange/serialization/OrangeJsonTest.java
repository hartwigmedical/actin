package com.hartwig.actin.molecular.orange.serialization;

import static org.junit.Assert.assertNotNull;
import java.io.IOException;
import com.hartwig.hmftools.datamodel.OrangeJson;
import com.google.common.io.Resources;
import org.junit.Test;

public class OrangeJsonTest {
    private static final String REAL_ORANGE_JSON = Resources.getResource("serialization/real.v2.6.0.orange.json").getPath();

    private static final double EPSILON = 1.0E-2;

    @Test
    public void canReadRealOrangeRecordJson() throws IOException {
        assertNotNull(OrangeJson.getInstance().read(REAL_ORANGE_JSON));
    }

}