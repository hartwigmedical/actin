package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation

class HasHadPDFollowingSomeSystemicTreatments(
    private val minSystemicTreatments: Int,
    private val mustBeRadiological: Boolean
) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val treatmentHistory = record.oncologicalHistory
        val minSystemicCount = SystemicTreatmentAnalyser.minSystemicTreatments(treatmentHistory)
        val maxSystemicCount = SystemicTreatmentAnalyser.maxSystemicTreatments(record.oncologicalHistory)
        val systemicTreatments = treatmentHistory.filter(SystemicTreatmentAnalyser::treatmentHistoryEntryIsSystemic)

        return when {
            minSystemicTreatments in (minSystemicCount + 1)..maxSystemicCount -> {
                EvaluationFactory.undetermined("Undetermined if received at least $minSystemicTreatments systemic treatments")
            }

            minSystemicCount < minSystemicTreatments -> {
                EvaluationFactory.fail("Has not received at least $minSystemicTreatments systemic treatments with PD")
            }

            systemicTreatments.count { ProgressiveDiseaseFunctions.treatmentResultedInPD(it) == true } >= minSystemicTreatments -> {
                val radiologicalNote = if (mustBeRadiological) " (assumed PD is radiological)" else ""
                EvaluationFactory.pass(
                    "Has received $minSystemicTreatments systemic treatments with PD$radiologicalNote"
                )
            }

            systemicTreatments.count { ProgressiveDiseaseFunctions.treatmentResultedInPD(it) != false } >= minSystemicTreatments -> {
                EvaluationFactory.undetermined("Has had at least $minSystemicTreatments systemic treatments but undetermined if PD")
            }

            else -> {
                EvaluationFactory.fail("Has not received at least $minSystemicTreatments systemic treatments with PD")
            }
        }
    }
}