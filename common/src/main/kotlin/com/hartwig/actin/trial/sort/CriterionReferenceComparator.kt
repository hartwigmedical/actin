package com.hartwig.actin.trial.sort

import com.hartwig.actin.trial.datamodel.CriterionReference

class CriterionReferenceComparator : Comparator<CriterionReference> {

    private val comparator = Comparator.comparing({ reference: CriterionReference -> reference.id.startsWith("I") }, reverseOrder())
        .thenComparing(CriterionReference::id)
        .thenComparing(CriterionReference::text)

    override fun compare(reference1: CriterionReference, reference2: CriterionReference): Int {
        return comparator.compare(reference1, reference2)
    }
}
