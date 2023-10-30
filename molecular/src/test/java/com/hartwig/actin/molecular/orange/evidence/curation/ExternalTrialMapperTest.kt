package com.hartwig.actin.molecular.orange.evidence.curation

import org.junit.Assert
import org.junit.Test

class ExternalTrialMapperTest {
    @Test
    fun canMapExternalTrials() {
        val mapper = TestExternalTrialMapperFactory.create("EXT 1", "ACT 1")
        Assert.assertEquals("ACT 1", mapper.map("EXT 1"))
        Assert.assertEquals("ACT 1", mapper.map("ACT 1"))
        Assert.assertEquals("random", mapper.map("random"))
    }
}