package com.hartwig.actin.treatment

import com.hartwig.actin.datamodel.molecular.evidence.CancerType
import com.hartwig.actin.datamodel.molecular.evidence.CancerTypeMatchApplicability
import com.hartwig.actin.datamodel.molecular.evidence.EvidenceLevelDetails
import com.hartwig.actin.datamodel.molecular.evidence.TreatmentEvidence

data class EvidenceScore(
    val event: String,
    val gene: String?,
    val scoringMatch: ScoringMatch,
    val evidenceLevelDetails: EvidenceLevelDetails,
    val cancerType: CancerType,
    val score: Double,
    val evidenceDescription: String
) : Comparable<EvidenceScore> {

    override fun compareTo(other: EvidenceScore): Int {
        return score.compareTo(other.score)
    }
}

class EvidenceScoringModel {

    fun score(treatment: TreatmentEvidence, gene: String?): EvidenceScore {
        val cancerTypeApplicability = when (treatment.cancerTypeMatch.applicability) {
            CancerTypeMatchApplicability.SPECIFIC_TYPE -> TumorMatch.PATIENT
            CancerTypeMatchApplicability.ALL_TYPES -> TumorMatch.ALL
            CancerTypeMatchApplicability.OTHER_TYPE -> TumorMatch.ANY
        }
        val exactVariant = if (treatment.molecularMatch.sourceEvidenceType.isCategoryEvent()) VariantMatch.CATEGORY else VariantMatch.EXACT
        val config = create()
        val scoringMatch = ScoringMatch(cancerTypeApplicability, exactVariant)
        val direction = if (treatment.evidenceDirection.hasBenefit) 1 else -1
        val factor = (config.categoryMatchLevels[scoringMatch] ?: 0) * direction
        val score = config.approvalPhaseLevel.scoring[treatment.evidenceLevelDetails] ?: 0
        return EvidenceScore(
            scoringMatch = scoringMatch,
            evidenceLevelDetails = treatment.evidenceLevelDetails,
            score = factor * score.toDouble(),
            event = treatment.molecularMatch.sourceEvent,
            gene = gene,
            evidenceDescription = treatment.efficacyDescription,
            cancerType = treatment.cancerTypeMatch.cancerType
        )
    }
}