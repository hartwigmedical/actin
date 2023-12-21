package com.hartwig.actin.clinical.sort

import com.hartwig.actin.clinical.datamodel.VitalFunction

class VitalFunctionDescendingDateComparator : Comparator<VitalFunction> {
    override fun compare(vitalFunction1: VitalFunction, vitalFunction2: VitalFunction): Int {
        val dateCompare = vitalFunction2.date().compareTo(vitalFunction1.date())
        if (dateCompare != 0) {
            return dateCompare
        }
        val categoryCompare = vitalFunction1.category().compareTo(vitalFunction2.category())
        if (categoryCompare != 0) {
            return categoryCompare
        }
        val subcategoryCompare = vitalFunction1.subcategory().compareTo(vitalFunction2.subcategory())
        if (subcategoryCompare != 0) {
            return subcategoryCompare
        }
        val unitCompare = vitalFunction1.unit().compareTo(vitalFunction2.unit())
        return if (unitCompare != 0) {
            unitCompare
        } else java.lang.Double.compare(vitalFunction1.value(), vitalFunction2.value())
    }
}
