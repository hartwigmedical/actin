package com.hartwig.actin.clinical.feed.standard

class DataQualityMask {
    fun apply(ehrPatientRecord: EhrPatientRecord): EhrPatientRecord {
        return scrubModifications(ehrPatientRecord)
    }

    private fun scrubModifications(ehrPatientRecord: EhrPatientRecord) =
        ehrPatientRecord.copy(treatmentHistory = ehrPatientRecord.treatmentHistory.map { it.copy(modifications = emptyList()) })
}