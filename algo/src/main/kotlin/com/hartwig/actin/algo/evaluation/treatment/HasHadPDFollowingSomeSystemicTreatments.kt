package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
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
        val lastTreatment = SystemicTreatmentAnalyser.lastSystemicTreatment(treatmentHistory)

        return when {
            maxSystemicCount < minSystemicTreatments -> {
                EvaluationFactory.fail("Has not received at least $minSystemicTreatments systemic treatments with PD")
            }

            minSystemicCount < minSystemicTreatments -> {
                EvaluationFactory.undetermined("Undetermined if received at least $minSystemicTreatments systemic treatments")
            }

            treatmentHistory.all { it.startYear == null && ProgressiveDiseaseFunctions.treatmentResultedInPD(it) == true } -> {
                EvaluationFactory.pass("Has received at least $minSystemicTreatments systemic treatments with PD")
            }

            lastTreatment?.let { ProgressiveDiseaseFunctions.treatmentResultedInPD(it) } == true -> {
                val radiologicalNote = if (mustBeRadiological) " (assumed PD is radiological)" else ""
                EvaluationFactory.pass("Last systemic treatment resulted in PD$radiologicalNote")
            }

            lastTreatment?.treatmentHistoryDetails?.stopYear == null || lastTreatment.treatmentHistoryDetails?.stopReason == StopReason.TOXICITY -> {
                EvaluationFactory.undetermined(
                    "Has had at least $minSystemicTreatments systemic treatments but undetermined if PD after last line"
                )
            }

            else -> {
                EvaluationFactory.fail("Has not received at least $minSystemicTreatments systemic treatments with PD")
            }
        }
    }
}