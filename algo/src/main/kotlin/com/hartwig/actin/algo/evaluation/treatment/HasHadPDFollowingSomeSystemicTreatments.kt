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
        if (minSystemicCount >= minSystemicTreatments) {
            return when {
                lastTreatment?.let { ProgressiveDiseaseFunctions.treatmentResultedInPD(it) } == true -> {
                    val radiologicalNote = if (mustBeRadiological) " (assumed PD is radiological)" else ""
                    EvaluationFactory.pass(
                        "Has received $minSystemicTreatments systemic treatments with PD$radiologicalNote"
                    )
                }

                lastTreatment?.treatmentHistoryDetails?.stopYear == null
                        || lastTreatment.treatmentHistoryDetails?.stopReason == StopReason.TOXICITY ->
                    EvaluationFactory.undetermined("Has had at least $minSystemicTreatments systemic treatments but undetermined if PD")

                else ->
                    EvaluationFactory.pass("Has received at least $minSystemicTreatments systemic treatments and PD is assumed")
            }
        } else if (maxSystemicCount >= minSystemicTreatments) {
            return EvaluationFactory.undetermined(
                "Undetermined if patient received at least $minSystemicTreatments systemic treatments",
                "Undetermined if at least $minSystemicTreatments systemic treatments"
            )
        }
        return EvaluationFactory.fail(
            "Patient did not receive at least $minSystemicTreatments systemic treatments",
            "Nr of systemic treatments with PD is less than $minSystemicTreatments"
        )
    }
}