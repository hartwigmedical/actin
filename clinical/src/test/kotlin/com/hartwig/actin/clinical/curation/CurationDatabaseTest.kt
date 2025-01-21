package com.hartwig.actin.clinical.curation

import com.hartwig.actin.clinical.UnusedCurationConfig
import com.hartwig.actin.clinical.curation.config.ComorbidityConfig
import com.hartwig.actin.clinical.curation.config.CurationConfigValidationError
import com.hartwig.actin.clinical.curation.config.InfectionConfig
import com.hartwig.actin.datamodel.clinical.Intolerance
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.io.File

private const val INPUT = "input"
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
    fun `Should return all unused curation inputs`() {
        val database = CurationDatabase(
            mapOf(INPUT to setOf(testConfig)), emptyList(), CurationCategory.COMORBIDITY
        ) { it.ecgEvaluatedInputs }
        assertThat(database.reportUnusedConfig(emptyList())).containsExactly(
            UnusedCurationConfig(
                CurationCategory.COMORBIDITY.categoryName,
                INPUT
            )
        )
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
        val combined = createAndCombineDatabases(true)
        assertThat(combined.find(INPUT)).containsExactly(testConfig)
        assertThat(combined.find(INPUT2)).containsExactly(testConfig2)
        assertThat(combined.validationErrors).containsExactly(error(1), error(2))
    }

    @Test
    fun `Should produce validation errors when conflicting keys are not ignored`() {
        val combined = createAndCombineDatabases(false)
        assertThat(combined.find(INPUT)).isEmpty()
        assertThat(combined.find(INPUT2)).containsExactly(testConfig2)
        val expectedConflictError = CurationConfigValidationError(
            CurationCategory.COMORBIDITY.categoryName, INPUT, "input", INPUT, "string", "Conflicting key: $INPUT"
        )
        assertThat(combined.validationErrors).containsExactly(error(1), error(2), expectedConflictError)
    }

    private fun createAndCombineDatabases(duplicateEntryIgnored: Boolean): CurationDatabase<ComorbidityConfig> {
        val database = curationDatabase(mapOf(INPUT to setOf(testConfig.copy(ignore = duplicateEntryIgnored))), 1)
        val other = curationDatabase(mapOf(INPUT to setOf(testConfig), INPUT2 to setOf(testConfig2)), 2)
        return database + other
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
    ): CurationDatabase<ComorbidityConfig> = CurationDatabase(configs, listOf(error(errorId)), CurationCategory.COMORBIDITY) { emptySet() }

    private fun error(value: Int): CurationConfigValidationError =
        CurationConfigValidationError(CurationCategory.COMORBIDITY.categoryName, value.toString(), "", "", "")
}