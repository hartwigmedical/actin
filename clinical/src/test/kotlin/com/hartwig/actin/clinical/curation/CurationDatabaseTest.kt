package com.hartwig.actin.clinical.curation

import com.hartwig.actin.clinical.curation.config.CurationConfig
import com.hartwig.actin.clinical.curation.config.PrimaryTumorConfig
import com.hartwig.actin.clinical.curation.extraction.ExtractionEvaluation
import com.hartwig.actin.clinical.curation.translation.LaboratoryIdentifiers
import com.hartwig.actin.clinical.curation.translation.Translation
import io.mockk.confirmVerified
import io.mockk.mockk
import io.mockk.verify
import org.apache.logging.log4j.Logger
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

data class TestConfig(override val input: String, override val ignore: Boolean) : CurationConfig

class CurationDatabaseTest {

    @Test
    fun `Should return empty set when key is not found`() {
        val database = CurationDatabase<TestConfig>(emptyMap())
        assertThat(database.curate("input")).isEmpty()
    }


}