package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.treatment.SystemicTreatmentAnalyser.treatmentHistoryEntryIsSystemic
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.treatment.history.Intent
import java.time.LocalDate

class HasHadSomeSystemicTreatmentsExcludingCurativeNeoadjuvantOrAdjuvantStoppedSomeMonthsBeforeNextLine(
    private val minSystemicTreatments: Int,
    private val maxMonthsBeforeNextLine: Int,
    private val referenceDate: LocalDate
) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {

        val timingEvaluatedHistory = TreatmentHistoryEntryFunctions.evaluateTreatmentTimingRelativeToNextLine(
            record.oncologicalHistory.filter(::treatmentHistoryEntryIsSystemic),
            maxMonthsBeforeNextLine, referenceDate
        )

        val (certainlyCountingEntries, potentiallyCountingCurativeAndNeoAdjuvantEntries) = timingEvaluatedHistory.partition {
            val passOnIntent = it.entry.intents?.intersect(Intent.curativeAdjuvantNeoadjuvantSet()).isNullOrEmpty()
            passOnIntent || it.timing == TreatmentHistoryEntryFunctions.TreatmentTiming.WITHIN
        }

        val curativeAdjuvantOrNeoadjuvantEntriesWithAmbiguousTiming = potentiallyCountingCurativeAndNeoAdjuvantEntries.filter {
            it.timing in setOf(
                TreatmentHistoryEntryFunctions.TreatmentTiming.AMBIGUOUS,
                TreatmentHistoryEntryFunctions.TreatmentTiming.UNKNOWN
            )
        }

        val minCertainCount = SystemicTreatmentAnalyser.minSystemicTreatments(certainlyCountingEntries.map { it.entry })
        val maxPotentialCount = SystemicTreatmentAnalyser.maxSystemicTreatments(
            (certainlyCountingEntries + curativeAdjuvantOrNeoadjuvantEntriesWithAmbiguousTiming).map { it.entry }
        )

        return when {
            minCertainCount >= minSystemicTreatments ->
                EvaluationFactory.pass("Received at least $minSystemicTreatments systemic treatments")

            maxPotentialCount >= minSystemicTreatments -> {
                val undeterminedMessageEnding = curativeAdjuvantOrNeoadjuvantEntriesWithAmbiguousTiming.takeIf { it.isNotEmpty() }
                    ?.let { " (incomplete date information)" } ?: ""
                EvaluationFactory.undetermined(
                    "Undetermined if received at least $minSystemicTreatments systemic treatments since it is unclear if (neo)adjuvant " +
                            "treatment(s) resulted in PD within $maxMonthsBeforeNextLine months after stopping$undeterminedMessageEnding"
                )
            }

            else -> EvaluationFactory.fail("Has not received at least $minSystemicTreatments systemic treatments")
        }
    }
}