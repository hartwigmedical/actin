package com.hartwig.actin.clinical.sort

import com.hartwig.actin.clinical.datamodel.treatment.history.TreatmentHistoryDetails
import com.hartwig.actin.clinical.datamodel.treatment.history.TreatmentHistoryEntry
import java.util.function.Function

class TreatmentHistoryAscendingDateComparator : Comparator<TreatmentHistoryEntry> {
    override fun compare(entry1: TreatmentHistoryEntry, entry2: TreatmentHistoryEntry): Int {
        val nullSafeComparator = Comparator.nullsLast(Comparator.naturalOrder<Int?>())
        if (stopsBeforeWithNullStart(entry1, entry2)) {
            return -1
        } else if (stopsBeforeWithNullStart(entry2, entry1)) {
            return 1
        }
        return Comparator.comparing({ obj: TreatmentHistoryEntry -> obj.startYear() }, nullSafeComparator)
            .thenComparing({ obj: TreatmentHistoryEntry -> obj.startMonth() }, nullSafeComparator)
            .thenComparing(
                { treatmentHistoryEntry: TreatmentHistoryEntry -> stopYearForHistoryEntry(treatmentHistoryEntry) },
                nullSafeComparator
            )
            .thenComparing(
                { treatmentHistoryEntry: TreatmentHistoryEntry -> stopMonthForHistoryEntry(treatmentHistoryEntry) },
                nullSafeComparator
            )
            .thenComparing { obj: TreatmentHistoryEntry -> obj.treatmentName() }
            .compare(entry1, entry2)
    }

    companion object {
        private fun stopsBeforeWithNullStart(entryA: TreatmentHistoryEntry, entryB: TreatmentHistoryEntry): Boolean {
            val startYearA = entryA.startYear()
            val stopYearA = stopYearForHistoryEntry(entryA)
            val stopMonthA = stopMonthForHistoryEntry(entryA)
            val startYearB = entryB.startYear()
            val startMonthB = entryB.startMonth()
            return if (startYearA == null && stopYearA != null && startYearB != null) {
                startYearB > stopYearA || startYearB == stopYearA && (startMonthB == null || stopMonthA == null || startMonthB >= stopMonthA)
            } else false
        }

        private fun stopYearForHistoryEntry(treatmentHistoryEntry: TreatmentHistoryEntry): Int? {
            return treatmentDetailField(treatmentHistoryEntry) { obj: TreatmentHistoryDetails? -> obj!!.stopYear() }
        }

        private fun stopMonthForHistoryEntry(treatmentHistoryEntry: TreatmentHistoryEntry): Int? {
            return treatmentDetailField(treatmentHistoryEntry) { obj: TreatmentHistoryDetails? -> obj!!.stopMonth() }
        }

        private fun treatmentDetailField(
            treatmentHistoryEntry: TreatmentHistoryEntry,
            getField: Function<TreatmentHistoryDetails?, Int?>
        ): Int? {
            return if (treatmentHistoryEntry.treatmentHistoryDetails() == null) null else getField.apply(treatmentHistoryEntry.treatmentHistoryDetails())
        }
    }
}
