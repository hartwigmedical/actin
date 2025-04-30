package com.hartwig.actin.algo.evidence

import com.hartwig.actin.datamodel.molecular.evidence.EvidenceLevelDetails

enum class TumorMatch {
    EXACT,
    INEXACT,
    OTHER
}

enum class VariantMatch {
    EXACT,
    CATEGORY,
    CATEGORY_OTHER
}

data class ScoringMatch(val tumorMatch: TumorMatch, val variantMatch: VariantMatch)

data class ScoringConfig(val categoryMatchLevels: Map<ScoringMatch, Int>, val approvalPhaseLevel: ScoringLevel)

data class ScoringLevel(val scoring: Map<EvidenceLevelDetails, Int>)

fun create() = ScoringConfig(
    mapOf(
        ScoringMatch(TumorMatch.EXACT, VariantMatch.EXACT) to 20,
        ScoringMatch(TumorMatch.EXACT, VariantMatch.CATEGORY) to 19,
        ScoringMatch(TumorMatch.EXACT, VariantMatch.CATEGORY_OTHER) to 18,
        ScoringMatch(TumorMatch.INEXACT, VariantMatch.EXACT) to 17,
        ScoringMatch(TumorMatch.INEXACT, VariantMatch.CATEGORY) to 16,
        ScoringMatch(TumorMatch.INEXACT, VariantMatch.CATEGORY_OTHER) to 15,
        ScoringMatch(TumorMatch.OTHER, VariantMatch.EXACT) to 14,
        ScoringMatch(TumorMatch.OTHER, VariantMatch.CATEGORY) to 13,
        ScoringMatch(TumorMatch.OTHER, VariantMatch.CATEGORY_OTHER) to 12,
    ),
    ScoringLevel(
        mapOf(
            EvidenceLevelDetails.UNKNOWN to 0,
            EvidenceLevelDetails.FDA_APPROVED to 100,
            EvidenceLevelDetails.GUIDELINE to 95,
            EvidenceLevelDetails.PHASE_III to 90,
            EvidenceLevelDetails.PHASE_II to 90,
            EvidenceLevelDetails.PHASE_IB_II to 40,
            EvidenceLevelDetails.PHASE_I to 35,
            EvidenceLevelDetails.PHASE_0 to 30,
            EvidenceLevelDetails.CLINICAL_STUDY to 30,
            EvidenceLevelDetails.CASE_REPORTS_SERIES to 25,
            EvidenceLevelDetails.PRECLINICAL to 10
        )
    )
)
