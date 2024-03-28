package com.hartwig.actin.molecular.orange.serialization

import com.hartwig.actin.testutil.ResourceLocator
import com.hartwig.hmftools.datamodel.OrangeJson
import org.junit.Assert.assertNotNull
import org.junit.Test

class OrangeJsonTest {

    @Test
    fun canReadRealOrangeRecordJson() {
        assertNotNull(OrangeJson.getInstance().read(REAL_ORANGE_JSON))
    }

    companion object {
        private val REAL_ORANGE_JSON = ResourceLocator(this).onClasspath("serialization/real.v3.0.0.orange.json")
    }
}