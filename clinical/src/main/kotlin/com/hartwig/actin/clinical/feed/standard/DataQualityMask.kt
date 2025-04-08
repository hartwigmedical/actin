package com.hartwig.actin.clinical.feed.standard

import com.hartwig.actin.datamodel.clinical.provided.ProvidedMolecularTestResult
import com.hartwig.actin.datamodel.clinical.provided.ProvidedPatientRecord


private fun ProvidedPatientRecord.scrubModifications() =
    this.copy(treatmentHistory = this.treatmentHistory.map { it.copy(modifications = emptyList()) })

private fun ProvidedPatientRecord.scrubMedications() =
    this.copy(medications = null)

private fun ProvidedPatientRecord.addAlwaysTestedGenes(panelGeneList: PanelGeneList) =
    this.copy(molecularTests = this.molecularTests.map {
        it.copy(testedGenes = panelGeneList.listGenesForPanel(it.test) + (it.testedGenes ?: emptySet()))
    })

private fun ProvidedPatientRecord.removeAllEmptyMolecularTestResults() =
    this.copy(molecularTests = this.molecularTests.map {
        it.copy(results = it.results.filterNot { r -> r.isAllFieldsExceptGeneNull() }.toSet())
    })

fun ProvidedMolecularTestResult.isAllFieldsExceptGeneNull(): Boolean {
    return this.ihcResult == null &&
            this.hgvsProteinImpact == null &&
            this.hgvsCodingImpact == null &&
            this.transcript == null &&
            this.fusionGeneUp == null &&
            this.fusionGeneDown == null &&
            this.fusionTranscriptUp == null &&
            this.fusionTranscriptDown == null &&
            this.fusionExonUp == null &&
            this.fusionExonDown == null &&
            this.exon == null &&
            this.codon == null &&
            this.exonSkipStart == null &&
            this.exonSkipEnd == null &&
            this.amplifiedGene == null &&
            this.deletedGene == null &&
            this.noMutationsFound == null &&
            this.freeText == null &&
            this.msi == null &&
            this.tmb == null &&
            this.vaf == null
}

class DataQualityMask(private val panelGeneList: PanelGeneList) {
    fun apply(ehrPatientRecord: ProvidedPatientRecord): ProvidedPatientRecord {
        return ehrPatientRecord.scrubMedications().scrubModifications().addAlwaysTestedGenes(panelGeneList)
            .removeAllEmptyMolecularTestResults()
    }
} 