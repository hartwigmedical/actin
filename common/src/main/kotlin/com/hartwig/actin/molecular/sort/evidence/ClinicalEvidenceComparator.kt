package com.hartwig.actin.molecular.sort.evidence

import com.hartwig.actin.molecular.datamodel.evidence.ClinicalEvidence
import com.hartwig.actin.molecular.datamodel.evidence.ClinicalEvidenceCategories

class ClinicalEvidenceComparator : Comparator<ClinicalEvidence> {

    override fun compare(evidence1: ClinicalEvidence, evidence2: ClinicalEvidence): Int {
        val rank1 = rank(evidence1)
        val rank2 = rank(evidence2)

        return rank1.compareTo(rank2)
    }

    private fun rank(evidence: ClinicalEvidence): Int {
        return when {
            ClinicalEvidenceCategories.approved(evidence.treatmentEvidence).isNotEmpty() -> 1
            evidence.externalEligibleTrials.isNotEmpty() -> 2
            ClinicalEvidenceCategories.experimental(evidence.treatmentEvidence).any { it.onLabel } -> 3
            ClinicalEvidenceCategories.experimental(evidence.treatmentEvidence).isNotEmpty() -> 4
            ClinicalEvidenceCategories.preclinical(evidence.treatmentEvidence).any { it.onLabel } -> 5
            ClinicalEvidenceCategories.preclinical(evidence.treatmentEvidence).isNotEmpty() -> 6
            ClinicalEvidenceCategories.knownResistant(evidence.treatmentEvidence).any { it.onLabel } -> 7
            ClinicalEvidenceCategories.knownResistant(evidence.treatmentEvidence).isNotEmpty() -> 8
            ClinicalEvidenceCategories.suspectResistant(evidence.treatmentEvidence).any { it.onLabel } -> 9
            ClinicalEvidenceCategories.suspectResistant(evidence.treatmentEvidence).isNotEmpty() -> 10
            else -> 11
        }
    }
}
