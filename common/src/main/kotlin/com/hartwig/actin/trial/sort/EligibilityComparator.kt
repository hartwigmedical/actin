package com.hartwig.actin.trial.sort

import com.hartwig.actin.trial.datamodel.CriterionReference
import com.hartwig.actin.trial.datamodel.Eligibility
import com.hartwig.actin.trial.datamodel.EligibilityFunction

class EligibilityComparator : Comparator<Eligibility> {

    private val criterionComparator: Comparator<CriterionReference> = CriterionReferenceComparator()
    private val functionComparator: Comparator<EligibilityFunction> = EligibilityFunctionComparator()

    override fun compare(eligibility1: Eligibility, eligibility2: Eligibility): Int {
        if (eligibility1.references.isEmpty() && eligibility2.references.isEmpty()) {
            return functionComparator.compare(eligibility1.function, eligibility2.function)
        } else if (eligibility1.references.isEmpty()) {
            return 1
        } else if (eligibility2.references.isEmpty()) {
            return -1
        }
        val referenceCompare: Int = criterionComparator.compare(eligibility1.references.first(), eligibility2.references.first())
        return if (referenceCompare == 0) {
            functionComparator.compare(eligibility1.function, eligibility2.function)
        } else {
            referenceCompare
        }
    }
}
