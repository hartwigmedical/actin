package com.hartwig.actin.clinical.sort

import com.hartwig.actin.clinical.datamodel.PriorSecondPrimary

class PriorSecondPrimaryDiagnosedDateComparator : Comparator<PriorSecondPrimary> {

    private val nullSafeComparator = Comparator.nullsLast(Comparator.naturalOrder<Int?>())
    private val comparator = Comparator.comparing(PriorSecondPrimary::diagnosedYear, nullSafeComparator)
        .thenComparing(PriorSecondPrimary::diagnosedMonth, nullSafeComparator)
        .thenComparing(PriorSecondPrimary::tumorLocation)
    
    override fun compare(secondPrimary1: PriorSecondPrimary, secondPrimary2: PriorSecondPrimary): Int {
        return comparator.compare(secondPrimary1, secondPrimary2)
    }
}
