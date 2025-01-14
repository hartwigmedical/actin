package com.hartwig.actin.algo.evaluation.othercondition

import com.hartwig.actin.algo.evaluation.util.Format

internal object OtherConditionMessages {

    fun fail(icdTitle: String?): String {
        return "Has no other condition belonging to category $icdTitle"
    }

    fun pass(matches: Iterable<String>): String {
        return "Has history of ${Format.concat(matches)}"
    }
}