package com.hartwig.actin.clinical.curation

import com.hartwig.actin.clinical.curation.config.CurationConfig
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

data class TestConfig(override val input: String, override val ignore: Boolean) : CurationConfig

class CurationDatabaseTest {

    @Test
    fun `Should return empty set when key is not found`() {
        val database = CurationDatabase<TestConfig>(emptyMap(), emptyList(), CurationCategory.ECG) { emptySet() }
        assertThat(database.curate("input")).isEmpty()
    }
}