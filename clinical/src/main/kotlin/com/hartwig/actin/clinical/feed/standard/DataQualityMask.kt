package com.hartwig.actin.clinical.feed.standard

class DataQualityMask {
    fun apply(ehrPatientRecord: EhrPatientRecord): EhrPatientRecord {
        return ehrPatientRecord.copy(treatmentHistory = scrubModifications(ehrPatientRecord))
    }

    private fun scrubModifications(ehrPatientRecord: EhrPatientRecord) =
        ehrPatientRecord.treatmentHistory.map { it.copy(modifications = emptyList()) }
}