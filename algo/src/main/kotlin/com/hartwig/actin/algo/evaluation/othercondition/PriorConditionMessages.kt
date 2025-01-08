package com.hartwig.actin.algo.evaluation.othercondition

import com.hartwig.actin.algo.evaluation.util.Format.concat

internal object PriorConditionMessages {

    fun fail(icdTitle: String?): String {
        return "Has no other condition belonging to category $icdTitle"
    }

    fun pass(characteristic: Characteristic, matches: Iterable<String>, icdTitle: String?): String {
        return String.format(
            "Has history of %s %s which is indicative of %s",
            characteristic.displayText,
            concat(matches),
            icdTitle
        )
    }

    internal enum class Characteristic(val displayText: String) {
        CONDITION("condition(s)"), TOXICITY("toxicity(ies)"), COMPLICATION("complication(s)")
    }
}