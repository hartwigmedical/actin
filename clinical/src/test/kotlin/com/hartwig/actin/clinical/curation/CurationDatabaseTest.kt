package com.hartwig.actin.clinical.curation

import com.hartwig.actin.clinical.UnusedCurationConfig
import com.hartwig.actin.clinical.curation.config.CurationConfig
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

data class TestConfig(override val input: String, override val ignore: Boolean) : CurationConfig

private const val INPUT = "input"

class CurationDatabaseTest {

    @Test
    fun `Should return empty set when input is not found`() {
        val database = CurationDatabase<TestConfig>(emptyMap(), emptyList(), CurationCategory.ECG) { emptySet() }
        assertThat(database.find(INPUT)).isEmpty()
    }

    @Test
    fun `Should return curation configs when key is found`() {
        val testConfig = TestConfig(INPUT, true)
        val database = CurationDatabase(
            mapOf(INPUT to setOf(testConfig)),
            emptyList(),
            CurationCategory.ECG
        ) { emptySet() }
        assertThat(database.find(INPUT)).containsExactly(testConfig)
    }

    @Test
    fun `Should return all unused curation inputs`() {
        val testConfig = TestConfig(INPUT, true)
        val database = CurationDatabase(
            mapOf(INPUT to setOf(testConfig)),
            emptyList(),
            CurationCategory.ECG
        ) { it.ecgEvaluatedInputs }
        assertThat(database.reportUnusedConfig(emptyList())).containsExactly(UnusedCurationConfig(CurationCategory.ECG, INPUT))
    }
}