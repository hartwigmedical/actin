package com.hartwig.actin.clinical.sort

import com.hartwig.actin.datamodel.clinical.VitalFunction

class VitalFunctionDescendingDateComparator : Comparator<VitalFunction> {

    private val comparator = Comparator.comparing(VitalFunction::date, reverseOrder())
        .thenComparing(VitalFunction::category)
        .thenComparing(VitalFunction::subcategory)
        .thenComparing(VitalFunction::unit)
        .thenComparing(VitalFunction::value)
    
    override fun compare(vitalFunction1: VitalFunction, vitalFunction2: VitalFunction): Int {
        return comparator.compare(vitalFunction1, vitalFunction2)
    }
}
