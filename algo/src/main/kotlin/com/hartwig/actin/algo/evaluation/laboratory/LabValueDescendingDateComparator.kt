package com.hartwig.actin.algo.evaluation.laboratory

import com.hartwig.actin.datamodel.clinical.LabValue

class LabValueDescendingDateComparator(highestFirst: Boolean) : Comparator<LabValue> {

    private val comparator = Comparator.comparing(LabValue::date, reverseOrder())
        .thenComparing(LabValue::measurement)
        .thenComparing(LabValue::value, if (highestFirst) reverseOrder() else naturalOrder())

    override fun compare(lab1: LabValue, lab2: LabValue): Int {
        return comparator.compare(lab1, lab2)
    }
}
