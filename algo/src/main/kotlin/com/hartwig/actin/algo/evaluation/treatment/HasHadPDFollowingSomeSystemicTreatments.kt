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
        val treatmentHistory = record.clinical().treatmentHistory()
        val minSystemicCount = SystemicTreatmentAnalyser.minSystemicTreatments(treatmentHistory)
        val maxSystemicCount = SystemicTreatmentAnalyser.maxSystemicTreatments(record.clinical().treatmentHistory())
        if (minSystemicCount >= minSystemicTreatments) {
            return SystemicTreatmentAnalyser.lastSystemicTreatment(treatmentHistory)
                ?.let { ProgressiveDiseaseFunctions.treatmentResultedInPDOption(it) }
                ?.let { treatmentResultedInPD: Boolean? ->
                    when (treatmentResultedInPD) {
                        true -> {
                            if (!mustBeRadiological) {
                                EvaluationFactory.pass(
                                    "Patient received at least $minSystemicTreatments systemic treatments ending with PD",
                                    "Has received $minSystemicTreatments systemic treatments with PD"
                                )
                            } else {
                                EvaluationFactory.undetermined(
                                    "Patient received at least " + minSystemicTreatments
                                            + " systemic treatments ending with PD, undetermined if there is currently radiological progression",
                                    "Undetermined if currently there is radiological progression"
                                )
                            }
                        }

                        else -> {
                            EvaluationFactory.fail(
                                "Patient received at least $minSystemicTreatments systemic treatments with no PD",
                                "At least $minSystemicTreatments systemic treatments but not with PD"
                            )
                        }
                    }
                } ?: EvaluationFactory.undetermined(
                "Patient received at least $minSystemicTreatments systemic treatments but unclear PD status",
                "Has had at least $minSystemicTreatments systemic treatments but undetermined if PD"
            )
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