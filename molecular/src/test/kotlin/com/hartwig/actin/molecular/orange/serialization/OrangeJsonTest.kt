package com.hartwig.actin.molecular.orange.serialization

import com.hartwig.actin.testutil.ResourceLocator.resourceOnClasspath
import com.hartwig.hmftools.datamodel.OrangeJson
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class OrangeJsonTest {

    @Test
    fun `Should be able to deserialize real orange json`() {
        assertThat(OrangeJson.getInstance().read(REAL_ORANGE_JSON)).isNotNull()
    }

    companion object {
        private val REAL_ORANGE_JSON = resourceOnClasspath("serialization/real.v3.5.0.orange.json")
    }
}