package com.hartwig.actin.molecular.util

import com.hartwig.actin.datamodel.molecular.evidence.ClinicalEvidence
import kotlin.math.roundToInt

internal object ExtractionUtil {

    fun noEvidence(): ClinicalEvidence {
        return ClinicalEvidence(treatmentEvidence = emptySet(), eligibleTrials = emptySet())
    }

    fun keep3Digits(input: Double): Double {
        return (input * 1000).roundToInt() / 1000.0
    }
}