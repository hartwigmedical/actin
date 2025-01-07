package com.hartwig.actin.algo.evaluation.othercondition

import com.hartwig.actin.algo.evaluation.util.Format.concat

internal object PriorConditionMessages {

    fun fail(doidTerm: String?): String {
        return "Has no other condition belonging to category $doidTerm"
    }

    fun pass(characteristic: Characteristic, matches: Iterable<String>, doidTerm: String?): String {
        return String.format(
            "Has history of %s %s which is indicative of %s",
            characteristic.displayText,
            concat(matches),
            doidTerm
        )
    }

    internal enum class Characteristic(val displayText: String) {
        CONDITION("condition(s)"), TOXICITY("toxicity(ies)"), COMPLICATION("complication(s)")
    }
}