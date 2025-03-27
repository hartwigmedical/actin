package com.hartwig.actin.clinical.feed.standard

import com.hartwig.actin.datamodel.clinical.provided.ProvidedMolecularTest
import com.hartwig.actin.datamodel.clinical.provided.ProvidedPatientRecord

val ARCHER_ALWAYS_TESTED_GENES = setOf("ALK", "ROS1", "RET", "MET", "NTRK1", "NTRK2", "NTRK3", "NRG1")
val GENERIC_PANEL_ALWAYS_TESTED_GENES = setOf("EGFR", "BRAF", "KRAS")

private fun ProvidedPatientRecord.scrubModifications() =
    this.copy(treatmentHistory = this.treatmentHistory.map { it.copy(modifications = emptyList()) })

private fun ProvidedPatientRecord.scrubMedications() =
    this.copy(medications = null)

private fun ProvidedPatientRecord.addAlwaysTestedGenes(panelGeneList: PanelGeneList) =
    this.copy(molecularTests = this.molecularTests.map {
        it.copy(testedGenes = panelGeneList[it.test] + (it.testedGenes ?: emptySet()))
    })

fun knownGenes(test: ProvidedMolecularTest): Set<String>? {
    val testNameLowerCase = test.test.lowercase()
    return when {
        testNameLowerCase.contains("archer") -> ARCHER_ALWAYS_TESTED_GENES + (test.testedGenes ?: emptySet())
        testNameLowerCase.contains("avl") || testNameLowerCase.contains("next generation sequencing") || testNameLowerCase
            .contains("ngs") -> GENERIC_PANEL_ALWAYS_TESTED_GENES + (test.testedGenes ?: emptySet())

        else -> test.testedGenes
    }
}

class DataQualityMask(private val panelGeneList: PanelGeneList) {
    fun apply(ehrPatientRecord: ProvidedPatientRecord): ProvidedPatientRecord {
        return ehrPatientRecord.scrubMedications().scrubModifications().addAlwaysTestedGenes(panelGeneList)
    }
} 