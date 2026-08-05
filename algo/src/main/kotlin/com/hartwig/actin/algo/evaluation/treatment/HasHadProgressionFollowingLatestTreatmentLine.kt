package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.EvaluationLabels
import com.hartwig.actin.clinical.interpretation.ProgressiveDiseaseFunctions
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation


class HasHadProgressionFollowingLatestTreatmentLine(
    private val mustBeRadiological: Boolean = true,
    private val labels: EvaluationLabels.Treatment
) :
    EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val treatmentHistory = record.oncologicalHistory
        val systemicTreatments =
            treatmentHistory.filter { SystemicTreatmentAnalyser.treatmentHistoryEntryIsSystemic(it) }
        val (systemicTreatmentsWithStartDate, systemicTreatmentsWithoutStartDate) = systemicTreatments.partition { it.startYear != null }
        val lastTreatment = SystemicTreatmentAnalyser.lastSystemicTreatment(systemicTreatmentsWithStartDate)
        val lastTreatmentResultedInPD = lastTreatment?.let { ProgressiveDiseaseFunctions.treatmentResultedInPD(it) }
        val treatmentWithoutDateDiffersInPDStatusFromLastTreatment = systemicTreatmentsWithoutStartDate.any {
            (ProgressiveDiseaseFunctions.treatmentResultedInPD(it) == true) != lastTreatmentResultedInPD
        }

        return when {
            systemicTreatments.isEmpty() -> {
                EvaluationFactory.fail(labels.hasHadProgressionFollowingLatestTreatmentLineFailNoSystemic())
            }

            systemicTreatments.all { ProgressiveDiseaseFunctions.treatmentResultedInPD(it) == true } -> {
                EvaluationFactory.pass(labels.hasHadProgressionFollowingLatestTreatmentLinePassAllPd())
            }

            treatmentWithoutDateDiffersInPDStatusFromLastTreatment -> {
                EvaluationFactory.undetermined(labels.hasHadProgressionFollowingLatestTreatmentLineUndeterminedNoStartDate())
            }

            lastTreatmentResultedInPD == true -> {
                val radiologicalNote = if (mustBeRadiological) " (assumed PD is radiological)" else ""
                EvaluationFactory.pass(labels.hasHadProgressionFollowingLatestTreatmentLinePass(radiologicalNote))
            }

            lastTreatmentResultedInPD == false -> {
                EvaluationFactory.fail(labels.hasHadProgressionFollowingLatestTreatmentLineFail())
            }

            else -> {
                EvaluationFactory.recoverableUndetermined(labels.hasHadProgressionFollowingLatestTreatmentLineRecoverableUndetermined())
            }
        }
    }
}
