package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.treatment.SystemicTreatmentAnalyser
import com.hartwig.actin.algo.evaluation.treatment.TreatmentHistoryEntryStartDateComparator
import com.hartwig.actin.clinical.interpretation.ProgressiveDiseaseFunctions
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.doid.DoidModel

class HasOligoprogressiveDisease(private val doidModel: DoidModel) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val metastaticCancerEvaluation = MetastaticCancerEvaluator.isMetastatic(record, doidModel)
        val systemicHistory = record.oncologicalHistory.filter(SystemicTreatmentAnalyser::treatmentHistoryEntryIsSystemic)
        val (historyWithoutDates, historyWithDates) = systemicHistory.partition { it.startYear == null }
        val mostRecentWithDate = historyWithDates.maxWithOrNull(TreatmentHistoryEntryStartDateComparator())
        val candidatesPossiblyProgressive = (listOfNotNull(mostRecentWithDate) + historyWithoutDates)
            .filter { ProgressiveDiseaseFunctions.treatmentResultedInPD(it, systemicHistory) != false }

        return when {
            metastaticCancerEvaluation == MetastaticCancerEvaluation.NON_METASTATIC -> {
                EvaluationFactory.fail("No oligoprogressive disease (stage ${record.tumor.stage?.display()})")
            }

            systemicHistory.isEmpty() -> {
                EvaluationFactory.fail("No oligoprogressive disease (no prior systemic treatment)")
            }

            candidatesPossiblyProgressive.isEmpty() -> {
                EvaluationFactory.fail("No oligoprogressive disease (most recent systemic treatment line did not result in PD)")
            }

            metastaticCancerEvaluation == MetastaticCancerEvaluation.DATA_MISSING -> {
                EvaluationFactory.undetermined("Undetermined if oligoprogressive disease (tumor stage missing)")
            }

            historyWithoutDates.isEmpty() -> {
                EvaluationFactory.undetermined("Undetermined if oligoprogressive disease")
            }

            else -> {
                EvaluationFactory.undetermined(
                    "Undetermined if oligoprogressive disease (unclear which treatment line is most recent)"
                )
            }
        }
    }
}
