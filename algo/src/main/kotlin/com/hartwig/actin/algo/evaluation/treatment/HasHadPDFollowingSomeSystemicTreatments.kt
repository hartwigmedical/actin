package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction

class HasHadPDFollowingSomeSystemicTreatments(
    private val minSystemicTreatments: Int,
    private val mustBeRadiological: Boolean
) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val priorTumorTreatments = record.clinical().priorTumorTreatments()
        val minSystemicCount = SystemicTreatmentAnalyser.minSystemicTreatments(priorTumorTreatments)
        val maxSystemicCount = SystemicTreatmentAnalyser.maxSystemicTreatments(priorTumorTreatments)
        val lastTreatment = SystemicTreatmentAnalyser.lastSystemicTreatment(priorTumorTreatments)
        if (minSystemicCount >= minSystemicTreatments) {
            return when {
                lastTreatment?.let { ProgressiveDiseaseFunctions.treatmentResultedInPDOption(it) } == true -> {
                    val radiologicalNote = if (mustBeRadiological) " (assumed PD is radiological)" else ""
                    EvaluationFactory.pass(
                        "Has received $minSystemicTreatments systemic treatments with PD$radiologicalNote"
                    )
                }

                lastTreatment?.stopYear() == null || lastTreatment.stopReason()
                    ?.contains("toxicity", ignoreCase = true) == true ->
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