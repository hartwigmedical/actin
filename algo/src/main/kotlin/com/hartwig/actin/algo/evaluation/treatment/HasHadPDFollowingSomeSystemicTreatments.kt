package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.treatment.ProgressiveDiseaseFunctions.treatmentResultedInPD
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.treatment.history.StopReason

class HasHadPDFollowingSomeSystemicTreatments(
    private val minSystemicTreatments: Int,
    private val mustBeRadiological: Boolean
) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val treatmentHistory = record.oncologicalHistory
        val systemicTreatments = treatmentHistory.filter(SystemicTreatmentAnalyser::treatmentHistoryEntryIsSystemic)
        val (systemicTreatmentsWithStartDate, systemicTreatmentsWithoutStartDate) = systemicTreatments.partition { it.startYear != null }
        val lastTreatment = SystemicTreatmentAnalyser.lastSystemicTreatment(systemicTreatmentsWithStartDate)
        val lastTreatmentResultedInPD = lastTreatment?.let { treatmentResultedInPD(it) } == true
        val undeterminedMessage = "Has had at least $minSystemicTreatments systemic treatments but undetermined if PD after last line"

        return when {
            SystemicTreatmentAnalyser.maxSystemicTreatments(treatmentHistory) < minSystemicTreatments -> {
                EvaluationFactory.fail("Has not received at least $minSystemicTreatments systemic treatments with PD")
            }

            SystemicTreatmentAnalyser.minSystemicTreatments(treatmentHistory) < minSystemicTreatments -> {
                EvaluationFactory.undetermined("Undetermined if received at least $minSystemicTreatments systemic treatments")
            }

            systemicTreatments.all { treatmentResultedInPD(it) == true } -> {
                EvaluationFactory.pass("Has received at least $minSystemicTreatments systemic treatments with PD")
            }

            systemicTreatmentsWithoutStartDate.any { entry -> (treatmentResultedInPD(entry) == true) != lastTreatmentResultedInPD } -> {
                EvaluationFactory.undetermined(undeterminedMessage)
            }

            lastTreatmentResultedInPD -> {
                val radiologicalNote = if (mustBeRadiological) " (assumed PD is radiological)" else ""
                EvaluationFactory.pass("Last systemic treatment resulted in PD$radiologicalNote")
            }

            lastTreatment?.let {
                it.treatmentHistoryDetails?.stopReason == StopReason.TOXICITY || it.treatmentHistoryDetails?.stopYear == null
            } == true -> {
                EvaluationFactory.undetermined(undeterminedMessage)
            }

            else -> {
                EvaluationFactory.fail("Has not received at least $minSystemicTreatments systemic treatments with PD")
            }
        }
    }
}