package com.hartwig.actin.clinical.sort

import com.hartwig.actin.datamodel.clinical.PriorOtherCondition

class PriorOtherConditionDescendingDateComparator : Comparator<PriorOtherCondition> {

    override fun compare(condition1: PriorOtherCondition, condition2: PriorOtherCondition): Int {
        val yearComparison = compareValues(condition2.year, condition1.year)
        return if (yearComparison != 0) yearComparison else compareValues(condition2.month, condition1.month)
    }
}