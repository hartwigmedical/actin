package com.hartwig.actin.clinical.sort

import com.hartwig.actin.clinical.datamodel.BodyWeight

class BodyWeightDescendingDateComparator : Comparator<BodyWeight> {
    override fun compare(bodyWeight1: BodyWeight, bodyWeight2: BodyWeight): Int {
        val dateCompare = bodyWeight2.date().compareTo(bodyWeight1.date())
        if (dateCompare != 0) {
            return dateCompare
        }
        val valueCompare = java.lang.Double.compare(bodyWeight2.value(), bodyWeight1.value())
        return if (valueCompare != 0) {
            valueCompare
        } else bodyWeight1.unit().compareTo(bodyWeight2.unit())
    }
}
