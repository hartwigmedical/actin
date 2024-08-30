package com.hartwig.actin.clinical.sort

import com.hartwig.actin.datamodel.clinical.BodyWeight

class BodyWeightDescendingDateComparator : Comparator<BodyWeight> {

    private val comparator = Comparator.comparing(BodyWeight::date, reverseOrder())
        .thenComparing(BodyWeight::value, reverseOrder())
        .thenComparing(BodyWeight::unit)
    
    override fun compare(bodyWeight1: BodyWeight, bodyWeight2: BodyWeight): Int {
        return comparator.compare(bodyWeight1, bodyWeight2)
    }
}
