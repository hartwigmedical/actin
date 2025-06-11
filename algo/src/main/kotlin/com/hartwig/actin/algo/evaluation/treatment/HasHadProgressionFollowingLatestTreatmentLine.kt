package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.treatment.history.StopReason


class HasHadProgressionFollowingLatestTreatmentLine(
    private val mustBeRadiological: Boolean = true
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
                EvaluationFactory.fail("No systemic treatments found in treatment history.")
            }

            systemicTreatments.all { ProgressiveDiseaseFunctions.treatmentResultedInPD(it) == true } -> {
                EvaluationFactory.pass("All systemic treatments resulted in progressive disease.")
            }

            treatmentWithoutDateDiffersInPDStatusFromLastTreatment -> {
                EvaluationFactory.undetermined("Unable to determine radiological progression following latest treatment line due to treatments without start date.")
            }

            lastTreatmentResultedInPD == true -> {
                val radiologicalNote = if (mustBeRadiological) " (assumed PD is radiological)" else ""
                EvaluationFactory.pass("Last systemic treatment resulted in PD$radiologicalNote.")
            }

            lastTreatmentResultedInPD == false -> {
                EvaluationFactory.fail("Last systemic treatment did not result in progressive disease.")
            }

            else -> {
                EvaluationFactory.undetermined("Radiological progression following latest treatment line undetermined.");
            }
        }
    }
}
