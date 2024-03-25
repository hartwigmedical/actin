package com.hartwig.actin.clinical.feed.standard

private fun EhrPatientRecord.scrubModifications() =
    this.copy(treatmentHistory = this.treatmentHistory.map { it.copy(modifications = emptyList()) })

private fun EhrPatientRecord.scrubMedications() =
    this.copy(medications = null)

class DataQualityMask {
    fun apply(ehrPatientRecord: EhrPatientRecord): EhrPatientRecord {
        return ehrPatientRecord.scrubMedications().scrubModifications()
    }
}