package com.hartwig.actin.algo.evidence

import com.hartwig.actin.datamodel.molecular.evidence.EvidenceApprovalPhase

enum class TumorMatch {
    ON_LABEL,
    OFF_LABEL
}

enum class VariantMatch {
    EXACT,
    CATEGORY,
    CATEGORY_OTHER
}

data class ScoringMatch(val tumorMatch: TumorMatch, val variantMatch: VariantMatch)

data class ScoringConfig(val duplicateFactor: Double, val categoryMatchLevels: Map<ScoringMatch, Int>, val approvalPhaseLevel: ScoringLevel)

data class ScoringLevel(val scoring: Map<EvidenceApprovalPhase, Int>)

fun create() = ScoringConfig(
    0.1,
    mapOf(
        ScoringMatch(TumorMatch.ON_LABEL, VariantMatch.EXACT) to 20,
        ScoringMatch(TumorMatch.ON_LABEL, VariantMatch.CATEGORY) to 19,
        ScoringMatch(TumorMatch.ON_LABEL, VariantMatch.CATEGORY_OTHER) to 18,
        ScoringMatch(TumorMatch.OFF_LABEL, VariantMatch.EXACT) to 17,
        ScoringMatch(TumorMatch.OFF_LABEL, VariantMatch.CATEGORY) to 16,
        ScoringMatch(TumorMatch.OFF_LABEL, VariantMatch.CATEGORY_OTHER) to 15,
    ),
    ScoringLevel(
        mapOf(
            EvidenceApprovalPhase.UNKNOWN to 0,
            EvidenceApprovalPhase.FDA_APPROVED to 100,
            EvidenceApprovalPhase.GUIDELINE to 95,
            EvidenceApprovalPhase.PHASE_III to 90,
            EvidenceApprovalPhase.PHASE_II to 90,
            EvidenceApprovalPhase.PHASE_IB_II to 40,
            EvidenceApprovalPhase.PHASE_I to 35,
            EvidenceApprovalPhase.PHASE_0 to 30,
            EvidenceApprovalPhase.CLINICAL_STUDY to 30,
            EvidenceApprovalPhase.CASE_REPORTS_SERIES to 25,
            EvidenceApprovalPhase.PRECLINICAL to 10
        )
    )
)
