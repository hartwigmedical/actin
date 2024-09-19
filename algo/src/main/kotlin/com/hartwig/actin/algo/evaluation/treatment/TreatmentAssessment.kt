package com.hartwig.actin.algo.evaluation.treatment

data class TreatmentAssessment(
    val hasHadValidTreatment: Boolean = false,
    val hasInconclusiveDate: Boolean = false,
    val hasHadTrialAfterMinDate: Boolean = false
) {

    fun combineWith(other: TreatmentAssessment): TreatmentAssessment {
        return TreatmentAssessment(
            hasHadValidTreatment || other.hasHadValidTreatment,
            hasInconclusiveDate || other.hasInconclusiveDate,
            hasHadTrialAfterMinDate || other.hasHadTrialAfterMinDate
        )
    }
}
