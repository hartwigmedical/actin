package com.hartwig.actin.evidence

import com.hartwig.actin.datamodel.molecular.evidence.TreatmentEvidence

class TreatmentScorer {

    fun score(treatment: TreatmentEvidence): Int {
        val onLabel = treatment.isOnLabel
        val exactVariant = treatment.molecularMatch.isCategoryEvent
        val scoring = create()
        
    }

}