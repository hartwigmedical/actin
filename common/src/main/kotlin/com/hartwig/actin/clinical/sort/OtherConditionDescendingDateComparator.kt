package com.hartwig.actin.clinical.sort

import com.hartwig.actin.datamodel.clinical.OtherCondition

class OtherConditionDescendingDateComparator : Comparator<OtherCondition> {

    override fun compare(condition1: OtherCondition, condition2: OtherCondition): Int {
        val yearComparison = compareValues(condition2.year, condition1.year)
        return if (yearComparison != 0) yearComparison else compareValues(condition2.month, condition1.month)
    }
}