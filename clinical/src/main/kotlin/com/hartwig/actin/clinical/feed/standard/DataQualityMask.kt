package com.hartwig.actin.clinical.feed.standard

private fun ProvidedPatientRecord.scrubModifications() =
    this.copy(treatmentHistory = this.treatmentHistory.map { it.copy(modifications = emptyList()) })

private fun ProvidedPatientRecord.scrubMedications() =
    this.copy(medications = null)

private fun ProvidedPatientRecord.scrubMolecularTests() =
    this.copy(molecularTests = this.molecularTests.map {
        it.copy(results = it.results.map { r ->
            if (r.hgvsCodingImpact == "NOT FOUND") r.copy(
                hgvsCodingImpact = null
            ) else if (r.hgvsProteinImpact == "NOT FOUND") r.copy(hgvsProteinImpact = null)
            else r
        }.toSet())
    })

class DataQualityMask {
    fun apply(ehrPatientRecord: ProvidedPatientRecord): ProvidedPatientRecord {
        return ehrPatientRecord.scrubMedications().scrubModifications().scrubMolecularTests()
    }
} 