package com.hartwig.actin.evidence

import com.hartwig.actin.datamodel.molecular.evidence.TreatmentEvidence

class TreatmentScorer {

    fun score(treatment: TreatmentEvidence): Int {
        val onLabel = if (treatment.isOnLabel) TumorMatch.ON_LABEL else TumorMatch.OFF_LABEL
        val exactVariant = if (treatment.molecularMatch.isCategoryEvent) VariantMatch.CATEGORY else VariantMatch.CATEGORY
        val config = create()
        val factor = config.levels[ScoringMatch(onLabel, exactVariant)] ?: 0
        val score = config.level.scoring[treatment.evidenceLevelDetails] ?: 0
        return factor * score
    }
}