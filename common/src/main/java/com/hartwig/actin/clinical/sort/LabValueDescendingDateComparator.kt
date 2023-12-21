package com.hartwig.actin.clinical.sort

import com.google.common.primitives.Doubles
import com.hartwig.actin.clinical.datamodel.LabValue

class LabValueDescendingDateComparator : Comparator<LabValue> {
    override fun compare(lab1: LabValue, lab2: LabValue): Int {
        // Descending on date
        val dateCompare = lab2.date().compareTo(lab1.date())
        if (dateCompare != 0) {
            return dateCompare
        }
        val codeCompare = lab1.code().compareTo(lab2.code())
        return if (codeCompare != 0) {
            codeCompare
        } else Doubles.compare(lab2.value(), lab1.value())

        // In case a code has been measured twice on the same date -> put the highest value first.
    }
}
