package com.hartwig.actin.trial.sort

import com.hartwig.actin.datamodel.trial.Eligibility
import com.hartwig.actin.datamodel.trial.EligibilityFunction

class EligibilityComparator : Comparator<Eligibility> {

    private val functionComparator: Comparator<EligibilityFunction> = EligibilityFunctionComparator()

    override fun compare(eligibility1: Eligibility, eligibility2: Eligibility): Int {
        if (eligibility1.references.isEmpty() && eligibility2.references.isEmpty()) {
            return functionComparator.compare(eligibility1.function, eligibility2.function)
        } else if (eligibility1.references.isEmpty()) {
            return 1
        } else if (eligibility2.references.isEmpty()) {
            return -1
        }

        val referenceCompare = compareReferences(eligibility1.references.first(), eligibility2.references.first())
        return if (referenceCompare == 0) {
            functionComparator.compare(eligibility1.function, eligibility2.function)
        } else {
            referenceCompare
        }
    }

    private fun compareReferences(reference1: String, reference2: String): Int {
        if (reference1.startsWith("I") && !reference2.startsWith("I")) {
            return -1
        } else if (!reference1.startsWith("I") && reference2.startsWith("I")) {
            return 1
        } 
        
        return reference1.compareTo(reference2)
        
    }
}
