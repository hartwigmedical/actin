package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.datamodel.clinical.treatment.Treatment
import com.hartwig.actin.datamodel.clinical.treatment.history.TreatmentHistoryEntry

object SystemicTreatmentAnalyser {

    fun maxSystemicTreatments(treatmentHistory: List<TreatmentHistoryEntry>): Int {
        return treatmentHistory.count(::treatmentHistoryEntryIsSystemic)
    }

    fun minSystemicTreatments(treatments: List<TreatmentHistoryEntry>): Int {
        val systemicByName = treatments.filter(::treatmentHistoryEntryIsSystemic).groupBy(TreatmentHistoryEntry::treatmentName)

        return systemicByName.map { entry ->
            if (entry.value.size == 1) 1 else {
                val otherTreatments = treatments.filterNot { it.treatmentName() == entry.key }
                val sortedWithName = entry.value.sortedWith(
                    compareBy(
                        TreatmentHistoryEntry::startYear,
                        TreatmentHistoryEntry::startMonth,
                        { it.treatmentHistoryDetails?.stopYear },
                        { it.treatmentHistoryDetails?.stopMonth },
                        TreatmentHistoryEntry::treatmentName
                    )
                )
                (1 until sortedWithName.size).map {
                    if (isInterrupted(sortedWithName[it], sortedWithName[it - 1], otherTreatments)) 1 else 0
                }.sum() + 1
            }
        }.sum()
    }

    fun lastSystemicTreatment(treatmentHistory: List<TreatmentHistoryEntry>): TreatmentHistoryEntry? {
        return treatmentHistory.filter(::treatmentHistoryEntryIsSystemic)
            .maxWithOrNull(TreatmentHistoryEntryStartDateComparator())
    }

    private fun treatmentHistoryEntryIsSystemic(treatmentHistoryEntry: TreatmentHistoryEntry): Boolean {
        return treatmentHistoryEntry.allTreatments().any(Treatment::isSystemic)
    }

    private fun isInterrupted(
        current: TreatmentHistoryEntry, previous: TreatmentHistoryEntry,
        otherTreatments: List<TreatmentHistoryEntry>
    ): Boolean {
        // Treatments with ambiguous timeline are never considered interrupted.
        return isAfter(current, previous) && otherTreatments.any { treatment ->
            isAfter(treatment, previous) && isBefore(treatment, current)
        }
    }

    private fun isBefore(first: TreatmentHistoryEntry, second: TreatmentHistoryEntry): Boolean {
        return if (isLower(first.startYear, second.startYear)) {
            true
        } else {
            isEqual(first.startYear, second.startYear) && isLower(first.startMonth, second.startMonth)
        }
    }

    private fun isAfter(first: TreatmentHistoryEntry, second: TreatmentHistoryEntry): Boolean {
        return if (isHigher(first.startYear, second.startYear)) {
            true
        } else {
            isEqual(first.startYear, second.startYear) && isHigher(first.startMonth, second.startMonth)
        }
    }

    private fun isHigher(int1: Int?, int2: Int?): Boolean {
        return int1 != null && int2 != null && int1 > int2
    }

    private fun isLower(int1: Int?, int2: Int?): Boolean {
        return int1 != null && int2 != null && int1 < int2
    }

    private fun isEqual(int1: Int?, int2: Int?): Boolean {
        return int1 != null && int2 != null && int1 == int2
    }
}