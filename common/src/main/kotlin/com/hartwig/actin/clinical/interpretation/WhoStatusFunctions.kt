package com.hartwig.actin.clinical.interpretation

import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.WhoStatus
import com.hartwig.actin.datamodel.clinical.WhoStatusPrecision

fun WhoStatus.asText() =
    when (precision) {
        WhoStatusPrecision.EXACT -> "$status"
        WhoStatusPrecision.AT_LEAST -> ">=$status"
        WhoStatusPrecision.AT_MOST -> "<=$status"
    }

fun WhoStatus.asRange(): IntRange {
    return when (precision) {
        WhoStatusPrecision.EXACT -> status..status
        WhoStatusPrecision.AT_MOST -> 0..status
        WhoStatusPrecision.AT_LEAST -> status..5
    }
}

fun WhoStatus.isEqualTo(requestedWho: Int): EvaluationResult {
    return when {
        precision == WhoStatusPrecision.EXACT && status == requestedWho -> EvaluationResult.PASS
        precision == WhoStatusPrecision.AT_LEAST && status <= requestedWho -> EvaluationResult.UNDETERMINED
        precision == WhoStatusPrecision.AT_MOST && status >= requestedWho -> EvaluationResult.UNDETERMINED
        else -> EvaluationResult.FAIL
    }
}

fun WhoStatus.isAtMost(requestedWho: Int): EvaluationResult {
    return when {
        precision in setOf(WhoStatusPrecision.EXACT, WhoStatusPrecision.AT_MOST) && status <= requestedWho -> EvaluationResult.PASS
        precision == WhoStatusPrecision.EXACT || (precision == WhoStatusPrecision.AT_LEAST && status > requestedWho) -> EvaluationResult.FAIL
        precision in setOf(WhoStatusPrecision.AT_MOST, WhoStatusPrecision.AT_LEAST) -> EvaluationResult.UNDETERMINED
        else -> throw IllegalStateException("Illegal evaluation of isAtMost")
    }
}

fun WhoStatus.isAtLeast(requestedWho: Int): EvaluationResult {
    return when {
        precision in setOf(WhoStatusPrecision.EXACT, WhoStatusPrecision.AT_LEAST) && status >= requestedWho -> EvaluationResult.PASS
        precision == WhoStatusPrecision.EXACT || (precision == WhoStatusPrecision.AT_MOST && status < requestedWho) -> EvaluationResult.FAIL
        precision in setOf(WhoStatusPrecision.AT_MOST, WhoStatusPrecision.AT_LEAST) -> EvaluationResult.UNDETERMINED
        else -> throw IllegalStateException("Illegal evaluation of isAtLeast")
    }
}