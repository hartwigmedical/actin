package com.hartwig.actin.molecular.util

import com.hartwig.actin.datamodel.molecular.evidence.ClinicalEvidence

internal object ExtractionUtil {

    fun noEvidence(): ClinicalEvidence {
        return ClinicalEvidence(treatmentEvidence = emptySet(), eligibleTrials = emptySet())
    }

    fun keep3Digits(input: Double): Double {
        return Math.round(input * 1000) / 1000.0
    }
}