package com.hartwig.actin.clinical.feed.standard

private fun ProvidedPatientRecord.scrubModifications() =
    this.copy(treatmentHistory = this.treatmentHistory.map { it.copy(modifications = emptyList()) })

private fun ProvidedPatientRecord.scrubMedications() =
    this.copy(medications = null)

private fun ProvidedPatientRecord.scrubMolecularTests() =
    this.copy(molecularTests = this.molecularTests.map {
        it.copy(results = it.results.map { r ->
            r.copy(
                hgvsCodingImpact = checkImpact(r, ProvidedMolecularTestResult::hgvsCodingImpact),
                hgvsProteinImpact = checkImpact(r, ProvidedMolecularTestResult::hgvsProteinImpact)
            )
        }.toSet())
    })

private fun checkImpact(r: ProvidedMolecularTestResult, accessor: (ProvidedMolecularTestResult) -> String?): String? {
    if (accessor.invoke(r) == "NOT FOUND")
        return null
    return accessor.invoke(r)
}

class DataQualityMask {
    fun apply(ehrPatientRecord: ProvidedPatientRecord): ProvidedPatientRecord {
        return ehrPatientRecord.scrubMedications().scrubModifications().scrubMolecularTests()
    }
} 