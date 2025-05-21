package com.hartwig.actin.clinical.sort

import com.hartwig.actin.datamodel.clinical.Surgery
import java.time.LocalDate

class SurgeryDescendingDateComparator : Comparator<Surgery> {

    private val comparator = compareBy<Surgery> { it.endDate ?: LocalDate.MIN }.reversed()

    override fun compare(surgery1: Surgery, surgery2: Surgery): Int {
        return comparator.compare(surgery1, surgery2)
    }
}