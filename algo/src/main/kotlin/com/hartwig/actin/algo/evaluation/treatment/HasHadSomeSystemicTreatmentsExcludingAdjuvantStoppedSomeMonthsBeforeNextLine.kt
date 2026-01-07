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

private const val ASSUMED_MINIMAL_NEOADJUVANT_OR_ADJUVANT_TREATMENT_DURATION_IN_MONTHS = 3L
private const val ASSUMED_MAXIMAL_NEOADJUVANT_OR_ADJUVANT_TREATMENT_DURATION_IN_MONTHS = 9L

class HasHadSomeSystemicTreatmentsExcludingAdjuvantStoppedSomeMonthsBeforeNextLine(
    private val minSystemicTreatments: Int,
    private val maxMonthsBeforeNextLine: Int,
    private val referenceDate: LocalDate
) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {

        val sortedSystemicHistory =
            record.oncologicalHistory.filter(::treatmentHistoryEntryIsSystemic).sortedWith(TreatmentHistoryEntryStartDateComparator())

        val timingEvaluatedHistory = sortedSystemicHistory.mapIndexed { index, entry ->
            val nextLine = sortedSystemicHistory.getOrNull(index + 1)
            TimingEvaluatedEntry(entry, entry.stoppedWithinMaxMonthsBeforeNextLine(nextLine, maxMonthsBeforeNextLine))
        }

        val includedEntries = timingEvaluatedHistory.filter {
            val passOnIntent = it.entry.intents?.intersect(Intent.curativeAdjuvantNeoadjuvantSet()).isNullOrEmpty()
            val passOnStopReason = it.entry.treatmentHistoryDetails?.stopReason == StopReason.TOXICITY
            passOnIntent || passOnStopReason || it.timing != TreatmentTiming.OUTSIDE
        }

        val certainlyCountingEntries = includedEntries.filter { it.timing == TreatmentTiming.WITHIN }
        val potentialCountingEntries = includedEntries.filter { it.timing in setOf(TreatmentTiming.AMBIGUOUS, TreatmentTiming.UNKNOWN) }

        val minCertainCount = SystemicTreatmentAnalyser.minSystemicTreatments(certainlyCountingEntries.map { it.entry })
        val maxPotentialCount =
            SystemicTreatmentAnalyser.maxSystemicTreatments((certainlyCountingEntries + potentialCountingEntries).map { it.entry })

        return when {
            minCertainCount >= minSystemicTreatments ->
                EvaluationFactory.pass("Received at least $minSystemicTreatments systemic treatments")

            maxPotentialCount >= minSystemicTreatments -> {
                EvaluationFactory.undetermined(
                    "Undetermined if received at least $minSystemicTreatments systemic treatments since it is unclear if (neo)adjuvant " +
                            "treatment(s) resulted in PD within $maxMonthsBeforeNextLine months after stopping (dates missing)"
                )
            }

            else -> EvaluationFactory.fail("Has not received at least $minSystemicTreatments systemic treatments")
        }
    }

    private fun TreatmentHistoryEntry.stoppedWithinMaxMonthsBeforeNextLine(
        nextLine: TreatmentHistoryEntry?,
        maxMonthsBeforeNextLine: Int
    ): TreatmentTiming {

        val (referenceMin, referenceMax) =
            when {
                nextLine == null -> referenceDate to referenceDate
                nextLine.startYear != null -> dateRange(nextLine.startYear!!, nextLine.startMonth)
                else -> null to null
            }

        return when {
            nextLine != null && nextLine.startYear == null -> TreatmentTiming.UNKNOWN

            this.stopYear() != null -> {
                val (stopMin, stopMax) = dateRange(this.stopYear()!!, this.stopMonth())
                val minMonthsBetween = ChronoUnit.MONTHS.between(stopMax, referenceMin)
                val maxMonthsBetween = ChronoUnit.MONTHS.between(stopMin, referenceMax)

                when {
                    minMonthsBetween > maxMonthsBeforeNextLine -> TreatmentTiming.OUTSIDE
                    maxMonthsBetween <= maxMonthsBeforeNextLine -> TreatmentTiming.WITHIN
                    else -> TreatmentTiming.AMBIGUOUS
                }
            }

            this.startYear == null -> TreatmentTiming.UNKNOWN

            else -> {
                val (startMin, startMax) = dateRange(this.startYear!!, this.startMonth)

                val hardStopMax = startMax.plusMonths(ASSUMED_MINIMAL_NEOADJUVANT_OR_ADJUVANT_TREATMENT_DURATION_IN_MONTHS)
                val softStopMin = startMin.plusMonths(ASSUMED_MAXIMAL_NEOADJUVANT_OR_ADJUVANT_TREATMENT_DURATION_IN_MONTHS)

                when {
                    ChronoUnit.MONTHS.between(hardStopMax, referenceMin) > maxMonthsBeforeNextLine -> TreatmentTiming.OUTSIDE
                    ChronoUnit.MONTHS.between(softStopMin, referenceMax) <= maxMonthsBeforeNextLine -> TreatmentTiming.WITHIN
                    else -> TreatmentTiming.AMBIGUOUS
                }
            }
        }
    }

    private fun dateRange(year: Int, month: Int?): Pair<LocalDate, LocalDate> =
        if (month == null) {
            LocalDate.of(year, 1, 1) to LocalDate.of(year, 12, 31)
        } else {
            val date = LocalDate.of(year, month, 1)
            date to date
        }

    private enum class TreatmentTiming {
        WITHIN,
        OUTSIDE,
        AMBIGUOUS,
        UNKNOWN
    }

    private data class TimingEvaluatedEntry(val entry: TreatmentHistoryEntry, val timing: TreatmentTiming)
}