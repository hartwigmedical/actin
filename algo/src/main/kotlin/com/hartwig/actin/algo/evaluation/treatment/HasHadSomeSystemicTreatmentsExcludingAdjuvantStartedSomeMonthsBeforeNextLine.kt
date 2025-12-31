package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.treatment.SystemicTreatmentAnalyser.treatmentHistoryEntryIsSystemic
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.treatment.history.Intent
import com.hartwig.actin.datamodel.clinical.treatment.history.StopReason
import com.hartwig.actin.datamodel.clinical.treatment.history.TreatmentHistoryEntry
import java.time.LocalDate
import java.time.temporal.ChronoUnit

class HasHadSomeSystemicTreatmentsExcludingAdjuvantStartedSomeMonthsBeforeNextLine(
    private val minSystemicTreatments: Int,
    private val maxMonthsBeforeNextLine: Int,
    private val referenceDate: LocalDate
) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {

        val curativeNeoAdjuvantOrAdjuvant = setOf(Intent.CURATIVE, Intent.NEOADJUVANT, Intent.ADJUVANT)
        val sortedSystemicHistory =
            record.oncologicalHistory.filter(::treatmentHistoryEntryIsSystemic).sortedWith(TreatmentHistoryEntryStartDateComparator())
        val filteredHistory = sortedSystemicHistory.filterIndexed { index, entry ->
            val nextLine = sortedSystemicHistory.getOrNull(index + 1)
            val passOnIntent = entry.intents?.intersect(curativeNeoAdjuvantOrAdjuvant).isNullOrEmpty()
            val passOnStopReason = entry.treatmentHistoryDetails?.stopReason == StopReason.TOXICITY
            val passOnDate = entry.startedWithinMaxMonthsBeforeNextLine(nextLine, maxMonthsBeforeNextLine)
            passOnIntent || passOnStopReason || passOnDate != TreatmentTiming.OUTSIDE
        }
        val minSystemicCount = SystemicTreatmentAnalyser.minSystemicTreatments(filteredHistory)
        val maxSystemicCount = SystemicTreatmentAnalyser.maxSystemicTreatments(filteredHistory)

        return when {
            minSystemicCount >= minSystemicTreatments -> {
                EvaluationFactory.pass("Received at least $minSystemicTreatments systemic treatments")
            }

            maxSystemicCount >= minSystemicTreatments -> {
                EvaluationFactory.undetermined("Undetermined if received at least $minSystemicTreatments systemic treatments")
            }

            else -> {
                EvaluationFactory.fail("Has not received at least $minSystemicTreatments systemic treatments")
            }
        }
    }

    private fun TreatmentHistoryEntry.startedWithinMaxMonthsBeforeNextLine(
        nextLine: TreatmentHistoryEntry?,
        maxMonthsBeforeNextLine: Int
    ): TreatmentTiming {
        return when {
            this.startYear == null -> TreatmentTiming.UNKNOWN

            nextLine != null && nextLine.startYear == null -> TreatmentTiming.UNKNOWN

            else -> {
                val (referenceMinDate, referenceMaxDate) =
                    if (nextLine == null) {
                        referenceDate to referenceDate
                    } else {
                        dateRange(nextLine.startYear!!, nextLine.startMonth)
                    }

                //TODO() add 6 months margin to entry start date since we are checking start dates instead of stop dates (duration of adjuvant line is considered 6 months)
                val (entryStartMinDate, entryStartMaxDate) = dateRange(this.startYear!!, this.startMonth)
                val minMonthsBetween = ChronoUnit.MONTHS.between(entryStartMaxDate, referenceMinDate)
                val maxMonthsBetween = ChronoUnit.MONTHS.between(entryStartMinDate, referenceMaxDate)

                when {
                    minMonthsBetween > maxMonthsBeforeNextLine -> TreatmentTiming.OUTSIDE
                    maxMonthsBetween <= maxMonthsBeforeNextLine -> TreatmentTiming.WITHIN
                    else -> TreatmentTiming.UNKNOWN
                }
            }
        }
    }

    private fun dateRange(year: Int, month: Int?): Pair<LocalDate, LocalDate> {
        return when {
            month == null -> LocalDate.of(year, 1, 1) to LocalDate.of(year, 12, 31)
            else -> {
                val date = LocalDate.of(year, month, 1)
                date to date
            }
        }
    }

    private enum class TreatmentTiming {
        WITHIN,
        OUTSIDE,
        AMBIGUOUS,
        UNKNOWN;
    }
}