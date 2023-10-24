package com.hartwig.actin.molecular.orange.serialization

import com.google.common.io.Resources
import com.hartwig.hmftools.datamodel.OrangeJson
import org.junit.Assert
import org.junit.Test
import java.io.IOException

class OrangeJsonTest {
    @Test
    @Throws(IOException::class)
    fun canReadRealOrangeRecordJson() {
        Assert.assertNotNull(OrangeJson.getInstance().read(REAL_ORANGE_JSON))
    }

    companion object {
        private val REAL_ORANGE_JSON = Resources.getResource("serialization/real.v2.6.0.orange.json").path
        private const val EPSILON = 1.0E-2
    }
}