package com.hartwig.actin.clinical.curation

import com.hartwig.actin.datamodel.clinical.ingestion.UnusedCurationConfig
import com.hartwig.actin.clinical.curation.config.ComorbidityConfig
import com.hartwig.actin.datamodel.clinical.ingestion.CurationConfigValidationError
import com.hartwig.actin.clinical.curation.config.InfectionConfig
import com.hartwig.actin.clinical.curation.extraction.CurationExtractionEvaluation
import com.hartwig.actin.datamodel.clinical.Intolerance
import com.hartwig.actin.datamodel.clinical.ingestion.CurationCategory
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.io.File

private const val INPUT = "input"
private const val INPUT_UPPER_CASE = "INPUT"
private const val INPUT2 = "input2"

class CurationDatabaseTest {
    private val testConfig = ComorbidityConfig(INPUT, false, curated = Intolerance("", emptySet()))
    private val testConfig2 = testConfig.copy(input = INPUT2)

    @Test
    fun `Should return empty set when input is not found`() {
        val database = CurationDatabase<InfectionConfig>(emptyMap(), emptyList(), CurationCategory.COMORBIDITY) { emptySet() }
        assertThat(database.find(INPUT)).isEmpty()
    }

    @Test
    fun `Should return curation configs when key is found`() {
        val database = CurationDatabase(mapOf(INPUT to setOf(testConfig)), emptyList(), CurationCategory.COMORBIDITY) { emptySet() }
        assertThat(database.find(INPUT)).containsExactly(testConfig)
    }

    @Test
    fun `Should return curation configs when key is found with different case and isCaseSensitive = false`() {
        val database = CurationDatabase(mapOf(INPUT to setOf(testConfig)), emptyList(), CurationCategory.COMORBIDITY) { emptySet() }
        assertThat(database.find(INPUT_UPPER_CASE)).containsExactly(testConfig)
    }

    @Test
    fun `Should not return curation configs when key is found with different case and isCaseSensitive = true`() {
        val database = CurationDatabase(mapOf(INPUT to setOf(testConfig)), emptyList(), CurationCategory.COMORBIDITY) { emptySet() }
        assertThat(database.find(INPUT_UPPER_CASE, isCaseSensitive = true)).isEmpty()
    }

    @Test
    fun `Should return all unused curation inputs`() {
        val database = CurationDatabase(
            mapOf(INPUT to setOf(testConfig)), emptyList(), CurationCategory.COMORBIDITY
        ) { it.comorbidityEvaluatedInputs }
        assertThat(database.reportUnusedConfig(CurationExtractionEvaluation()))
            .containsExactly(UnusedCurationConfig(CurationCategory.COMORBIDITY, INPUT))
    }

    @Test
    fun `Should have category enums for all curation sheets`() {
        val dir = File(CURATION_DIRECTORY)
        val sheetNames = dir.listFiles()!!.map { it.name }.toSet()

        val catNames =  CurationCategory.entries
            .filterNot { it == CurationCategory.COMORBIDITY }  // TODO: Combine all comorbidities on comorbidity curation sheet
            .map {it.categoryName.lowercase().replace(" ", "_") + ".tsv"}
            .toSet()

        assertThat(sheetNames).containsExactlyInAnyOrderElementsOf(catNames)
    }

    @Test
    fun `Should combine databases when conflicting keys are ignored`() {
        val database = curationDatabase(mapOf(INPUT to setOf(testConfig.copy(ignore = true))), 1)
        val other = curationDatabase(mapOf(INPUT to setOf(testConfig), INPUT2 to setOf(testConfig2)), 2)
        val combined = database + other

        assertThat(combined.find(INPUT)).containsExactly(testConfig)
        assertThat(combined.find(INPUT2)).containsExactly(testConfig2)
        assertThat(combined.validationErrors).containsExactly(error(1), error(2))
    }

    @Test
    fun `Should combine curated value sets when conflicting keys are not ignored`() {
        val conflicting = testConfig.copy(curated = Intolerance("conflicting", emptySet()))
        val database = curationDatabase(mapOf(INPUT to setOf(testConfig)), 1)
        val other = curationDatabase(mapOf(INPUT to setOf(conflicting), INPUT2 to setOf(testConfig2)), 2)
        val combined = database + other

        assertThat(combined.find(INPUT)).containsExactly(testConfig, conflicting)
        assertThat(combined.find(INPUT2)).containsExactly(testConfig2)
        assertThat(combined.validationErrors).containsExactly(error(1), error(2))
    }

    @Test
    fun `Should retain single ignore entry with no conflicts when all conflicting entries are ignored`() {
        val ignoredConfig = testConfig.copy(ignore = true)
        val database = curationDatabase(mapOf(INPUT to setOf(ignoredConfig)), 1)
        val other = curationDatabase(mapOf(INPUT to setOf(ignoredConfig)), 2)

        val combined = database + other
        assertThat(combined.find(INPUT)).containsExactly(ignoredConfig)
        assertThat(combined.validationErrors).containsExactly(error(1), error(2))
    }

    @Test
    fun `Should retain ignore entries with no conflicts when combining databases`() {
        val ignoredConfig = testConfig.copy(ignore = true)
        val database = curationDatabase(mapOf(INPUT to setOf(ignoredConfig)), 1)
        val other = curationDatabase(mapOf(INPUT2 to setOf(testConfig2)), 2)

        val combined = database + other
        assertThat(combined.find(INPUT)).containsExactly(ignoredConfig)
        assertThat(combined.find(INPUT2)).containsExactly(testConfig2)
        assertThat(combined.validationErrors).containsExactly(error(1), error(2))
    }

    private fun CurationDatabaseTest.curationDatabase(
        configs: Map<InputText, Set<ComorbidityConfig>>, errorId: Int
    ): CurationDatabase<ComorbidityConfig> {
        return CurationDatabase(configs, listOf(error(errorId)), CurationCategory.COMORBIDITY) { emptySet() }
    }

    private fun error(value: Int): CurationConfigValidationError =
        CurationConfigValidationError(CurationCategory.COMORBIDITY, value.toString(), "", "", "")
}