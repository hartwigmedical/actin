package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.treatment.SystemicTreatmentAnalyser.treatmentHistoryEntryIsSystemic
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.treatment.history.Intent
import com.hartwig.actin.datamodel.clinical.treatment.history.TreatmentHistoryEntry
import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.ChronoUnit

private const val ASSUMED_MINIMUM_TREATMENT_DURATION_IN_MONTHS = 3L
private const val MIN_MONTH = 1
private const val MAX_MONTH = 12

private data class TimingEvaluatedEntry(val entry: TreatmentHistoryEntry, val timing: TreatmentTiming)

private enum class TreatmentTiming {
    WITHIN,
    OUTSIDE,
    AMBIGUOUS,
    UNKNOWN
}

class HasHadSystemicLinesOnlyIncludingNeoOrAdjuvantIfNextLineWithinMonths(
    private val referenceTreatmentCount: Int,
    private val maxMonthsBeforeNextLine: Int,
    private val referenceDate: LocalDate,
    private val atLeast: Boolean
): EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {

        val timingEvaluatedHistory = evaluateTreatmentTimingRelativeToNextLine(
            record.oncologicalHistory.filter(::treatmentHistoryEntryIsSystemic),
            maxMonthsBeforeNextLine, referenceDate
        )

        val (certainlyCountingEntries, potentiallyCountingCurativeAndNeoAdjuvantEntries) = timingEvaluatedHistory.partition {
            val passOnIntent = it.entry.intents?.intersect(Intent.curativeAdjuvantNeoadjuvantSet()).isNullOrEmpty()
            passOnIntent || it.timing == TreatmentTiming.WITHIN
        }

        val curativeAdjuvantOrNeoadjuvantEntriesWithAmbiguousTiming = potentiallyCountingCurativeAndNeoAdjuvantEntries.filter {
            it.timing in setOf(TreatmentTiming.AMBIGUOUS,TreatmentTiming.UNKNOWN)
        }

        val minCertainCount = SystemicTreatmentAnalyser.minSystemicTreatments(certainlyCountingEntries.map { it.entry })
        val maxPotentialCount = SystemicTreatmentAnalyser.maxSystemicTreatments(
            (certainlyCountingEntries + curativeAdjuvantOrNeoadjuvantEntriesWithAmbiguousTiming).map { it.entry }
        )

        val comparator: (Int, Int) -> Boolean = if (atLeast) { a, b -> a >= b } else { a, b -> a <= b }
        val comparatorMessage = if (atLeast) "least" else "most"

        return when {
            comparator(minCertainCount, referenceTreatmentCount) ->
                EvaluationFactory.pass("Received at $comparatorMessage $referenceTreatmentCount systemic treatments")


            comparator(maxPotentialCount, referenceTreatmentCount) -> {
                val undeterminedMessageEnding = curativeAdjuvantOrNeoadjuvantEntriesWithAmbiguousTiming.takeIf { it.isNotEmpty() }
                    ?.let { " since it is unclear if (neo)adjuvant treatment(s) resulted in PD within $maxMonthsBeforeNextLine months after " +
                            "stopping (incomplete date information)" } ?: ""
                EvaluationFactory.undetermined(
                    "Undetermined if received at $comparatorMessage $referenceTreatmentCount systemic treatments$undeterminedMessageEnding"
                )
            }

            else -> EvaluationFactory.fail("Has not received at $comparatorMessage $referenceTreatmentCount systemic treatments")
        }
    }

    private fun evaluateTreatmentTimingRelativeToNextLine(
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
}