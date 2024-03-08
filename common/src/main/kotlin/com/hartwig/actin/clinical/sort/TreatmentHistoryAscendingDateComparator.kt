package com.hartwig.actin.clinical.sort

import com.hartwig.actin.clinical.datamodel.treatment.history.TreatmentHistoryEntry

class TreatmentHistoryAscendingDateComparator : Comparator<TreatmentHistoryEntry> {

    private val nullSafeComparator = Comparator.nullsLast(Comparator.naturalOrder<Int?>())
    private val comparator = Comparator.comparing(TreatmentHistoryEntry::startYear, nullSafeComparator)
        .thenComparing(TreatmentHistoryEntry::startMonth, nullSafeComparator)
        .thenComparing(::stopYearForHistoryEntry, nullSafeComparator)
        .thenComparing(::stopMonthForHistoryEntry, nullSafeComparator)
        .thenComparing(TreatmentHistoryEntry::treatmentName)

    override fun compare(entry1: TreatmentHistoryEntry, entry2: TreatmentHistoryEntry): Int {
        if (stopsBeforeWithNullStart(entry1, entry2)) {
            return -1
        } else if (stopsBeforeWithNullStart(entry2, entry1)) {
            return 1
        }
        return comparator.compare(entry1, entry2)
    }

    private fun stopsBeforeWithNullStart(entryA: TreatmentHistoryEntry, entryB: TreatmentHistoryEntry): Boolean {
        val startYearA = entryA.startYear
        val stopYearA = stopYearForHistoryEntry(entryA)
        val stopMonthA = stopMonthForHistoryEntry(entryA)
        val startYearB = entryB.startYear
        val startMonthB = entryB.startMonth
        return if (startYearA == null && stopYearA != null && startYearB != null) {
            startYearB > stopYearA || startYearB == stopYearA &&
                    (startMonthB == null || stopMonthA == null || startMonthB >= stopMonthA)
        } else false
    }

    private fun stopYearForHistoryEntry(treatmentHistoryEntry: TreatmentHistoryEntry): Int? {
        return treatmentHistoryEntry.treatmentHistoryDetails?.stopYear
    }

    private fun stopMonthForHistoryEntry(treatmentHistoryEntry: TreatmentHistoryEntry): Int? {
        return treatmentHistoryEntry.treatmentHistoryDetails?.stopMonth
    }
}
