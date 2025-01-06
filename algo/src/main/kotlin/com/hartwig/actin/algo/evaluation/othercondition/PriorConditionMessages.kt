package com.hartwig.actin.algo.evaluation.othercondition

import com.hartwig.actin.algo.evaluation.util.Format
import com.hartwig.actin.algo.evaluation.util.Format.concat

internal object PriorConditionMessages {

    fun failSpecific(doidTerm: String?): String {
        return "Patient has no other condition belonging to category $doidTerm"
    }

    @JvmStatic
    fun failGeneral(): String {
        return "No relevant non-oncological condition"
    }

    fun pass(matches: Iterable<String>): String {
        return "History of ${Format.concatStringsWithAnd(matches)}"
    }

    fun passSpecific(characteristic: Characteristic, matches: Iterable<String>, doidTerm: String?): String {
        return String.format("Patient has history of %s %s, which is indicative of %s", characteristic.displayText, concat(matches), doidTerm)
    }

    internal enum class Characteristic(val displayText: String) {
        CONDITION("condition(s)"), TOXICITY("toxicity(ies)"), COMPLICATION("complication(s)")
    }
}