package com.hartwig.actin.trial.datamodel

import com.hartwig.actin.trial.sort.CriterionReferenceComparator

data class CriterionReference(
    val id: String,
    val text: String
) : Comparable<CriterionReference> {

    override fun compareTo(other: CriterionReference): Int {
        return CriterionReferenceComparator().compare(this, other)
    }
}
