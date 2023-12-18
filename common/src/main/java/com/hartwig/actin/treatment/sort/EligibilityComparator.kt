package com.hartwig.actin.treatment.sort

import com.hartwig.actin.treatment.datamodel.CriterionReference
import com.hartwig.actin.treatment.datamodel.Eligibility
import com.hartwig.actin.treatment.datamodel.EligibilityFunction

class EligibilityComparator : Comparator<Eligibility> {

    override fun compare(eligibility1: Eligibility, eligibility2: Eligibility): Int {
        if (eligibility1.references.isEmpty() && eligibility2.references.isEmpty()) {
            return FUNCTION_COMPARATOR.compare(eligibility1.function, eligibility2.function)
        } else if (eligibility1.references.isEmpty()) {
            return 1
        } else if (eligibility2.references.isEmpty()) {
            return -1
        }
        val referenceCompare: Int = CRITERION_COMPARATOR.compare(eligibility1.references.first(), eligibility2.references.first())
        if (referenceCompare == 0) {
            return FUNCTION_COMPARATOR.compare(eligibility1.function, eligibility2.function)
        } else {
            return referenceCompare
        }
    }

    companion object {
        private val CRITERION_COMPARATOR: Comparator<CriterionReference> = CriterionReferenceComparator()
        private val FUNCTION_COMPARATOR: Comparator<EligibilityFunction> = EligibilityFunctionComparator()
    }
}
