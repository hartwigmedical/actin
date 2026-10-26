package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.tumor.TumorEvaluationFunctions.isStageMatch
import com.hartwig.actin.algo.evaluation.treatment.TreatmentHistoryEntryStartDateComparator
import com.hartwig.actin.clinical.interpretation.ProgressiveDiseaseFunctions
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.TumorStage
import com.hartwig.actin.datamodel.clinical.treatment.history.TreatmentHistoryEntry

class HasOligoprogressiveDisease : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val history = record.oncologicalHistory

        return when {
            history.isEmpty() -> evaluateWithoutPriorTreatment(record.tumor.stage)

            possiblyProgressiveLatestLineCandidates(history).isEmpty() -> {
                EvaluationFactory.fail("No oligoprogressive disease")
            }

            history.none { it.startYear == null } -> {
                EvaluationFactory.undetermined("Undetermined if oligoprogressive disease")
            }

            else -> {
                EvaluationFactory.undetermined(
                    "Undetermined if oligoprogressive disease (unclear which treatment line is most recent)"
                )
            }
        }
    }

    private fun evaluateWithoutPriorTreatment(stage: TumorStage?): Evaluation {
        return if (stage == null || isStageMatch(stage, STAGES_AT_LEAST_III)) {
            EvaluationFactory.undetermined("Undetermined if oligoprogressive disease")
        } else {
            EvaluationFactory.fail("No oligoprogressive disease")
        }
    }

    private fun possiblyProgressiveLatestLineCandidates(history: List<TreatmentHistoryEntry>): List<TreatmentHistoryEntry> {
        val (historyWithoutDates, historyWithDates) = history.partition { it.startYear == null }
        val mostRecentWithDate = historyWithDates.maxWithOrNull(TreatmentHistoryEntryStartDateComparator())

        return (listOfNotNull(mostRecentWithDate) + historyWithoutDates)
            .filter { ProgressiveDiseaseFunctions.treatmentResultedInPD(it, history) != false }
    }

    companion object {
        private val STAGES_AT_LEAST_III = setOf(TumorStage.III, TumorStage.IV)
    }
}
