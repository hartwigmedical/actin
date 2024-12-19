package com.hartwig.actin.datamodel.molecular.sort.evidence

import com.hartwig.actin.datamodel.molecular.evidence.ClinicalEvidence
import com.hartwig.actin.datamodel.molecular.evidence.TreatmentEvidenceCategories

class ClinicalEvidenceComparator : Comparator<ClinicalEvidence> {

    override fun compare(evidence1: ClinicalEvidence, evidence2: ClinicalEvidence): Int {
        val rank1 = rank(evidence1)
        val rank2 = rank(evidence2)

        return rank1.compareTo(rank2)
    }

    private fun rank(evidence: ClinicalEvidence): Int {
        return when {
            TreatmentEvidenceCategories.approved(evidence.treatmentEvidence).isNotEmpty() -> 1
            evidence.eligibleTrials.isNotEmpty() -> 2
            TreatmentEvidenceCategories.experimental(evidence.treatmentEvidence).any { it.isOnLabel } -> 3
            TreatmentEvidenceCategories.experimental(evidence.treatmentEvidence).isNotEmpty() -> 4
            TreatmentEvidenceCategories.preclinical(evidence.treatmentEvidence).any { it.isOnLabel } -> 5
            TreatmentEvidenceCategories.preclinical(evidence.treatmentEvidence).isNotEmpty() -> 6
            TreatmentEvidenceCategories.knownResistant(evidence.treatmentEvidence).any { it.isOnLabel } -> 7
            TreatmentEvidenceCategories.knownResistant(evidence.treatmentEvidence).isNotEmpty() -> 8
            TreatmentEvidenceCategories.suspectResistant(evidence.treatmentEvidence).any { it.isOnLabel } -> 9
            TreatmentEvidenceCategories.suspectResistant(evidence.treatmentEvidence).isNotEmpty() -> 10
            else -> 11
        }
    }
}
