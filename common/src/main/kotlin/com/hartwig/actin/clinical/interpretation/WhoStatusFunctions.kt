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
    return when (precision) {
        WhoStatusPrecision.EXACT -> if (status == requestedWho) EvaluationResult.PASS else EvaluationResult.FAIL
        WhoStatusPrecision.AT_LEAST -> if (status <= requestedWho) EvaluationResult.UNDETERMINED else EvaluationResult.FAIL
        WhoStatusPrecision.AT_MOST -> if (status >= requestedWho) EvaluationResult.UNDETERMINED else EvaluationResult.FAIL
    }
}

fun WhoStatus.isAtMost(requestedWho: Int): EvaluationResult {
    return when (precision) {
        WhoStatusPrecision.EXACT -> if (status <= requestedWho) EvaluationResult.PASS else EvaluationResult.FAIL
        WhoStatusPrecision.AT_MOST -> if (status <= requestedWho) EvaluationResult.PASS else EvaluationResult.UNDETERMINED
        WhoStatusPrecision.AT_LEAST -> if (status > requestedWho) EvaluationResult.FAIL else EvaluationResult.UNDETERMINED
    }
}

fun WhoStatus.isAtLeast(requestedWho: Int): EvaluationResult {
    return when (precision) {
        WhoStatusPrecision.EXACT -> if (status >= requestedWho) EvaluationResult.PASS else EvaluationResult.FAIL
        WhoStatusPrecision.AT_MOST -> if (status < requestedWho) EvaluationResult.FAIL else EvaluationResult.UNDETERMINED
        WhoStatusPrecision.AT_LEAST -> if (status >= requestedWho) EvaluationResult.PASS else EvaluationResult.UNDETERMINED
    }
}