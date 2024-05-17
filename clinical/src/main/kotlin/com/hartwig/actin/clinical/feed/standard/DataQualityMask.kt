package com.hartwig.actin.clinical.feed.standard

private fun ProvidedPatientRecord.scrubModifications() =
    this.copy(treatmentHistory = this.treatmentHistory.map { it.copy(modifications = emptyList()) })

private fun ProvidedPatientRecord.scrubMedications() =
    this.copy(medications = null)

class DataQualityMask {
    fun apply(ehrPatientRecord: ProvidedPatientRecord): ProvidedPatientRecord {
        return ehrPatientRecord.scrubMedications().scrubModifications()
    }
}