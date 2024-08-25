package com.hartwig.actin.molecular.sort.evidence

import com.hartwig.actin.molecular.datamodel.evidence.ClinicalEvidence
import com.hartwig.actin.molecular.datamodel.evidence.EvidenceLevel

class ClinicalEvidenceComparator : Comparator<ClinicalEvidence> {

    override fun compare(evidence1: ClinicalEvidence, evidence2: ClinicalEvidence): Int {
        val rank1 = rank(evidence1)
        val rank2 = rank(evidence2)

        return rank1.compareTo(rank2)
    }

    private fun rank(evidence: ClinicalEvidence): Int {
        return when {
            evidence.treatmentEvidence.any { it.evidenceLevel == EvidenceLevel.A } -> 1
            evidence.externalEligibleTrials.isNotEmpty() -> 2
            evidence.treatmentEvidence.any { it.evidenceLevel == EvidenceLevel.B } -> 3
            else -> 4
        }
    }
}
