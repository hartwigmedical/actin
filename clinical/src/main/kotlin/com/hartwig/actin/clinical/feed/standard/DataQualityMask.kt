package com.hartwig.actin.clinical.feed.standard

import com.hartwig.actin.datamodel.clinical.provided.ProvidedPatientRecord


private fun ProvidedPatientRecord.scrubModifications() =
    this.copy(treatmentHistory = this.treatmentHistory.map { it.copy(modifications = emptyList()) })

private fun ProvidedPatientRecord.scrubMedications() =
    this.copy(medications = null)

private fun ProvidedPatientRecord.addAlwaysTestedGenes(panelGeneList: PanelGeneList) =
    this.copy(molecularTests = this.molecularTests.map {
        it.copy(testedGenes = panelGeneList.listGenesForPanel(it.test) + (it.testedGenes ?: emptySet()))
    })

class DataQualityMask(private val panelGeneList: PanelGeneList) {
    fun apply(ehrPatientRecord: ProvidedPatientRecord): ProvidedPatientRecord {
        return ehrPatientRecord.scrubMedications().scrubModifications().addAlwaysTestedGenes(panelGeneList)
    }
} 