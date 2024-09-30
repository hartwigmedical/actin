package com.hartwig.actin.clinical.sort

import com.hartwig.actin.datamodel.clinical.Surgery

class SurgeryDescendingDateComparator : Comparator<Surgery> {

    private val comparator = Comparator.comparing(Surgery::endDate, reverseOrder())

    override fun compare(surgery1: Surgery, surgery2: Surgery): Int {
        return comparator.compare(surgery1, surgery2)
    }
}