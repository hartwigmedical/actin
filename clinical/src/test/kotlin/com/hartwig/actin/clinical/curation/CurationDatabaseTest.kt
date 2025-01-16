package com.hartwig.actin.clinical.curation

import com.hartwig.actin.clinical.UnusedCurationConfig
import com.hartwig.actin.clinical.curation.config.InfectionConfig
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.io.File

private const val INPUT = "input"

class CurationDatabaseTest {

    @Test
    fun `Should return empty set when input is not found`() {
        val database = CurationDatabase<InfectionConfig>(emptyMap(), emptyList(), CurationCategory.INFECTION) { emptySet() }
        assertThat(database.find(INPUT)).isEmpty()
    }

    @Test
    fun `Should return curation configs when key is found`() {
        val testConfig = InfectionConfig(INPUT, false, "")
        val database = CurationDatabase(
            mapOf(INPUT to setOf(testConfig)),
            emptyList(),
            CurationCategory.INFECTION
        ) { emptySet() }
        assertThat(database.find(INPUT)).containsExactly(testConfig)
    }

    @Test
    fun `Should return all unused curation inputs`() {
        val testConfig = InfectionConfig(INPUT, false, "")
        val database = CurationDatabase(
            mapOf(INPUT to setOf(testConfig)),
            emptyList(),
            CurationCategory.INFECTION
        ) { it.ecgEvaluatedInputs }
        assertThat(database.reportUnusedConfig(emptyList())).containsExactly(
            UnusedCurationConfig(
                CurationCategory.INFECTION.categoryName,
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
            .map {it.categoryName.lowercase().replace(" ", "_")+".tsv"}
            .toSet()

        assertThat(sheetNames).containsExactlyInAnyOrderElementsOf(catNames)
    }
}