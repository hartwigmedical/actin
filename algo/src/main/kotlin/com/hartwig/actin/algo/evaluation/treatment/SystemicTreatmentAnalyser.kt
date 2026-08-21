package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.datamodel.clinical.treatment.Treatment
import com.hartwig.actin.datamodel.clinical.treatment.history.TreatmentHistoryEntry
import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.ChronoUnit

object SystemicTreatmentAnalyser {

    private const val ASSUMED_MINIMUM_TREATMENT_DURATION_IN_MONTHS = 3L
    private const val MIN_MONTH = 1
    private const val MAX_MONTH = 12

    data class TimingEvaluatedEntry(val entry: TreatmentHistoryEntry, val timing: TreatmentTiming)

    enum class TreatmentTiming {
        WITHIN,
        OUTSIDE,
        AMBIGUOUS,
        UNKNOWN
    }

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
                        { it.stopYear() },
                        { it.stopMonth() },
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

    fun firstSystemicTreatment(treatmentHistory: List<TreatmentHistoryEntry>): TreatmentHistoryEntry? {
        return treatmentHistory.filter(::treatmentHistoryEntryIsSystemic)
            .minWithOrNull(TreatmentHistoryEntryStartDateComparator())
    }

    fun treatmentHistoryEntryIsSystemic(treatmentHistoryEntry: TreatmentHistoryEntry): Boolean {
        return treatmentHistoryEntry.allTreatments().any(Treatment::isSystemic)
    }

    fun evaluateTreatmentTimingRelativeToNextLine(
        history: List<TreatmentHistoryEntry>, maxMonthsBeforeNextLine: Int, referenceDate: LocalDate
    ): List<TimingEvaluatedEntry> {
        val sortedHistory = history.sortedWith(TreatmentHistoryEntryStartDateComparator())

        return sortedHistory.mapIndexed { index, entry ->
            val nextLine = sortedHistory.getOrNull(index + 1)
            TimingEvaluatedEntry(entry, entry.stoppedWithinMaxMonthsBeforeNextLine(nextLine, maxMonthsBeforeNextLine, referenceDate))
        }
    }

    private fun TreatmentHistoryEntry.stoppedWithinMaxMonthsBeforeNextLine(
        nextLine: TreatmentHistoryEntry?,
        maxMonthsBeforeNextLine: Int,
        referenceDate: LocalDate
    ): TreatmentTiming {
        val (nextLineMin, nextLineMax) = when {
            nextLine == null -> referenceDate to referenceDate
            nextLine.startYear != null -> dateRange(nextLine.startYear!!, nextLine.startMonth)
            else -> null to null
        }

        return when {
            nextLine != null && nextLine.startYear == null -> TreatmentTiming.UNKNOWN

            this.stopYear() != null -> {
                val (stopMin, stopMax) = dateRange(this.stopYear()!!, this.stopMonth())
                val minMonthsBetween = ChronoUnit.MONTHS.between(stopMax, nextLineMin)
                val maxMonthsBetween = ChronoUnit.MONTHS.between(stopMin, nextLineMax)

                when {
                    minMonthsBetween > maxMonthsBeforeNextLine -> TreatmentTiming.OUTSIDE
                    maxMonthsBetween <= maxMonthsBeforeNextLine -> TreatmentTiming.WITHIN
                    else -> TreatmentTiming.AMBIGUOUS
                }
            }

            this.startYear == null -> TreatmentTiming.UNKNOWN

            else -> {
                val startMax = YearMonth.of(this.startYear!!, this.startMonth ?: MAX_MONTH)
                val assumedStopDateLowerBound = startMax.plusMonths(ASSUMED_MINIMUM_TREATMENT_DURATION_IN_MONTHS)

                when {
                    ChronoUnit.MONTHS.between(assumedStopDateLowerBound, nextLineMin) > maxMonthsBeforeNextLine -> TreatmentTiming.OUTSIDE
                    else -> TreatmentTiming.AMBIGUOUS
                }
            }
        }
    }

    private fun dateRange(year: Int, month: Int?): Pair<YearMonth, YearMonth> =
        if (month == null) {
            YearMonth.of(year, MIN_MONTH) to YearMonth.of(year, MAX_MONTH)
        } else {
            YearMonth.of(year, month) to YearMonth.of(year, month)
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