package com.hartwig.actin.molecular.orange.serialization

import com.google.common.io.Resources
import com.hartwig.hmftools.datamodel.OrangeJson
import org.junit.Assert.assertNotNull
import org.junit.Test

class OrangeJsonTest {

    @Test
    fun canReadRealOrangeRecordJson() {
        assertNotNull(OrangeJson.getInstance().read(REAL_ORANGE_JSON))
    }

    companion object {
        private val REAL_ORANGE_JSON = Resources.getResource("serialization/real.v3.0.0.orange.json").path
    }
}