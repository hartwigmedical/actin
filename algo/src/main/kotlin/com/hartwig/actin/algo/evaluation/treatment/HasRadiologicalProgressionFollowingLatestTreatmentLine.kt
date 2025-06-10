package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.treatment.history.StopReason



class HasRadiologicalProgressionFollowingLatestTreatmentLine(
    private val canAssumePDIfStopYearProvided: Boolean = true,
    private val mustBeRadiological: Boolean = true
) :
    EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val treatmentHistory = record.oncologicalHistory
        val systemicTreatments =
            treatmentHistory.filter { SystemicTreatmentAnalyser.treatmentHistoryEntryIsSystemic(it) }
        val (systemicTreatmentsWithStartDate, systemicTreatmentsWithoutStartDate) = systemicTreatments.partition { it.startYear != null }
        val lastTreatment = SystemicTreatmentAnalyser.lastSystemicTreatment(systemicTreatmentsWithStartDate)
        val lastTreatmentResultedInPD =
            lastTreatment?.let { ProgressiveDiseaseFunctions.treatmentResultedInPD(it) } == true

        return when {
            systemicTreatments.isEmpty() -> {
                EvaluationFactory.fail("No systemic treatments found in treatment history.")
            }

            systemicTreatments.all { ProgressiveDiseaseFunctions.treatmentResultedInPD(it) == true } -> {
                EvaluationFactory.pass("All systemic treatments resulted in progressive disease.")
            }

            // check if all treatments without start date agree on PD status.
            systemicTreatmentsWithoutStartDate.any { it -> (ProgressiveDiseaseFunctions.treatmentResultedInPD(it) == true) != lastTreatmentResultedInPD } -> {
                EvaluationFactory.undetermined("Unable to determine radiological progression following latest treatment line due to treatments without start date.")
            }

            // treatments without start date agree with last treatment in terms of PD status past last condition.
            lastTreatmentResultedInPD -> {
                val radiologicalNote = if (mustBeRadiological) " (assumed PD is radiological)" else ""
                EvaluationFactory.pass("Last systemic treatment resulted in PD$radiologicalNote")
            }

            // or stop year is provided, and PD assumed to be radiological
            lastTreatment?.treatmentHistoryDetails?.stopYear != null && canAssumePDIfStopYearProvided -> {
                EvaluationFactory.pass("Last systemic treatment stopped and radiological progression is assumed.")
            }

            lastTreatment?.treatmentHistoryDetails?.stopReason == StopReason.TOXICITY || lastTreatment?.treatmentHistoryDetails?.stopYear == null -> {
                EvaluationFactory.fail("Last systemic treament did not result in progressive disease.")
            }

            else -> {
                EvaluationFactory.undetermined("Radiological progression following latest treatment line undetermined.");
            }
        }
    }
}
