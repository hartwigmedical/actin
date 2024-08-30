package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.datamodel.clinical.treatment.history.TreatmentHistoryEntry

class TreatmentHistoryEntryStartDateComparator : Comparator<TreatmentHistoryEntry> {
    override fun compare(treatment1: TreatmentHistoryEntry, treatment2: TreatmentHistoryEntry): Int {
        val yearComparison = compareValues(treatment1.startYear, treatment2.startYear)
        return if (yearComparison != 0) yearComparison else compareValues(treatment1.startMonth, treatment2.startMonth)
    }
}