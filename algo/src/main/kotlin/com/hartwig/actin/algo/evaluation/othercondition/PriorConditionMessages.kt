package com.hartwig.actin.algo.evaluation.othercondition

import com.hartwig.actin.algo.evaluation.util.Format.concat

internal object PriorConditionMessages {
    fun failSpecific(doidTerm: String?): String {
        return "Patient has no other condition belonging to category $doidTerm"
    }

    fun failGeneral(): String {
        return "No relevant non-oncological condition"
    }

    fun passGeneral(doidTerm: String?): String {
        return "Relevant non-oncological condition $doidTerm"
    }

    fun passSpecific(characteristic: Characteristic, matches: Iterable<String>, doidTerm: String?): String {
        return String.format("Patient has %s %s, which is indicative of %s", characteristic.displayText, concat(matches), doidTerm)
    }

    internal enum class Characteristic(val displayText: String) {
        CONDITION("condition(s)"), TOXICITY("toxicity(ies)"), COMPLICATION("complication(s)")
    }
}