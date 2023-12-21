package com.hartwig.actin.clinical.sort

import com.hartwig.actin.clinical.datamodel.PriorSecondPrimary

class PriorSecondPrimaryDiagnosedDateComparator : Comparator<PriorSecondPrimary> {
    override fun compare(secondPrimary1: PriorSecondPrimary, secondPrimary2: PriorSecondPrimary): Int {
        val nullSafeComparator = Comparator.nullsLast(Comparator.naturalOrder<Int?>())
        return Comparator.comparing({ obj: PriorSecondPrimary -> obj.diagnosedYear() }, nullSafeComparator)
            .thenComparing({ obj: PriorSecondPrimary -> obj.diagnosedMonth() }, nullSafeComparator)
            .thenComparing { obj: PriorSecondPrimary -> obj.tumorLocation() }
            .compare(secondPrimary1, secondPrimary2)
    }
}
