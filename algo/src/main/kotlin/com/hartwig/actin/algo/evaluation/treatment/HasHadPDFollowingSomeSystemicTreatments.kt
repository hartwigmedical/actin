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
        val minSystemicCount = SystemicTreatmentAnalyser.minSystemicTreatments(treatmentHistory)
        val maxSystemicCount = SystemicTreatmentAnalyser.maxSystemicTreatments(record.oncologicalHistory)
        val systemicTreatments = treatmentHistory.filter(SystemicTreatmentAnalyser::treatmentHistoryEntryIsSystemic)
        val (systemicTreatmentsWithStartDate, systemicTreatmentsWithoutStartDate) = systemicTreatments.partition { it.startYear != null }
        val lastTreatment = SystemicTreatmentAnalyser.lastSystemicTreatment(systemicTreatmentsWithStartDate)
        val undeterminedMessage = "Has had at least $minSystemicTreatments systemic treatments but undetermined if PD after last line"

        return when {
            maxSystemicCount < minSystemicTreatments -> {
                EvaluationFactory.fail("Has not received at least $minSystemicTreatments systemic treatments with PD")
            }

            minSystemicCount < minSystemicTreatments -> {
                EvaluationFactory.undetermined("Undetermined if received at least $minSystemicTreatments systemic treatments")
            }

            systemicTreatments.all { treatmentResultedInPD(it) == true } -> {
                EvaluationFactory.pass("Has received at least $minSystemicTreatments systemic treatments with PD")
            }

            systemicTreatmentsWithoutStartDate.isNotEmpty() && systemicTreatments.any { treatmentResultedInPD(it) == true }
                    && (systemicTreatmentsWithoutStartDate.any { treatmentResultedInPD(it) != true }
                    || lastTreatment?.let { treatmentResultedInPD(it) } != true) -> {
                EvaluationFactory.undetermined(undeterminedMessage)
            }

            lastTreatment?.let { treatmentResultedInPD(it) } == true -> {
                val radiologicalNote = if (mustBeRadiological) " (assumed PD is radiological)" else ""
                EvaluationFactory.pass("Last systemic treatment resulted in PD$radiologicalNote")
            }

            lastTreatment?.let { it.treatmentHistoryDetails?.stopReason == StopReason.TOXICITY || it.treatmentHistoryDetails?.stopYear == null } == true -> {
                EvaluationFactory.undetermined(undeterminedMessage)
            }

            else -> {
                EvaluationFactory.fail("Has not received at least $minSystemicTreatments systemic treatments with PD")
            }
        }
    }
}