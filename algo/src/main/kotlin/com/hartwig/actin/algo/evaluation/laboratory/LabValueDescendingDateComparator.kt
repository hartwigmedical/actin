package com.hartwig.actin.algo.evaluation.laboratory

import com.hartwig.actin.datamodel.clinical.LabValue

class LabValueDescendingDateComparator : Comparator<LabValue> {

    // In case a lab measurement has been measured twice on the same date -> put the highest value first.
    private val comparator = Comparator.comparing(LabValue::date, reverseOrder())
        .thenComparing(LabValue::measurement)
        .thenComparing(LabValue::value, reverseOrder())

    override fun compare(lab1: LabValue, lab2: LabValue): Int {
        return comparator.compare(lab1, lab2)
    }
}
