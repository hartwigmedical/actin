package com.hartwig.actin.datamodel.molecular.evidence

data class ClinicalEvidence(
    val treatmentEvidence: Set<TreatmentEvidence>,
    val eligibleTrials: Set<ExternalTrial>,
) {

    operator fun plus(other: ClinicalEvidence): ClinicalEvidence {
        return ClinicalEvidence(
            treatmentEvidence + other.treatmentEvidence,
            eligibleTrials + other.eligibleTrials
        )
    }
}
