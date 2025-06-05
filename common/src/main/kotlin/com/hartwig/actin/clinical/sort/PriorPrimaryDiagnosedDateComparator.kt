package com.hartwig.actin.clinical.sort

import com.hartwig.actin.datamodel.clinical.PriorPrimary

class PriorPrimaryDiagnosedDateComparator : Comparator<PriorPrimary> {

    private val nullSafeComparator = Comparator.nullsLast(Comparator.naturalOrder<Int?>())
    private val comparator = Comparator.comparing(PriorPrimary::diagnosedYear, nullSafeComparator)
        .thenComparing(PriorPrimary::diagnosedMonth, nullSafeComparator)
        .thenComparing(PriorPrimary::tumorLocation)

    override fun compare(priorPrimary1: PriorPrimary, priorPrimary2: PriorPrimary): Int {
        return comparator.compare(priorPrimary1, priorPrimary2)
    }
}
