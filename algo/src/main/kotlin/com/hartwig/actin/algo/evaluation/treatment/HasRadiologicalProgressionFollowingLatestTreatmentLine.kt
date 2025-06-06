package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation

//TODO: Implement according to README
class HasRadiologicalProgressionFollowingLatestTreatmentLine(
    private val mustBeRadiological: Boolean
) :
    EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val undeterminedMessage = "Radiological progression following latest treatment line undetermined"

        val treatmentHistory = record.oncologicalHistory
        val systemicTreatments =
            treatmentHistory.filter { SystemicTreatmentAnalyser.treatmentHistoryEntryIsSystemic(it) }
        val (systemicTreatmentsWithStartDate, systemicTreatmentsWithoutStartDate) = systemicTreatments.partition { it.startYear != null }
        val lastTreatment = SystemicTreatmentAnalyser.lastSystemicTreatment(systemicTreatmentsWithStartDate)
        val lastTreatmentResultedInPD =
            lastTreatment?.let { ProgressiveDiseaseFunctions.treatmentResultedInPD(it) } == true

        return when {
            lastTreatmentResultedInPD -> {
                EvaluationFactory.pass("Last systemic treatment resulted in radiological progression.")
            }
            // or stop year is provided, and PD assumed to be radiological
            lastTreatment?.treatmentHistoryDetails?.stopYear != null && mustBeRadiological -> {
                EvaluationFactory.pass("Last systemic treatment stopped and radiological progression is assumed.")
            }

            else -> {
                EvaluationFactory.undetermined(undeterminedMessage);
            }
        }
    }
}