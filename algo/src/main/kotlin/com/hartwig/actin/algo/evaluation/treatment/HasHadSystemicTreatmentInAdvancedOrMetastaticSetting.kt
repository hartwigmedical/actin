package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.util.Format.concatWithCommaAndAnd
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.treatment.history.Intent

class HasHadSystemicTreatmentInAdvancedOrMetastaticSetting : EvaluationFunction {
    override fun evaluate(record: PatientRecord): Evaluation {
        val priorSystemicTreatments = record.oncologicalHistory.filter { entry -> entry.treatments.any { it.isSystemic } }
        val palliativeIntentTreatments = priorSystemicTreatments.filter { it.intents?.contains(Intent.PALLIATIVE) ?: false }
        val treatmentsWithUncertainSetting = priorSystemicTreatments.filter { treatment ->
            setOf(Intent.INDUCTION, Intent.CONSOLIDATION, Intent.MAINTENANCE).any { treatment.intents?.contains(it) ?: true }
        }

            return when {
                palliativeIntentTreatments.isNotEmpty() -> {
                    val messageEnding = "had prior systemic treatment in advanced or metastatic setting " +
                            "(${concatWithCommaAndAnd(palliativeIntentTreatments.map { it.treatmentDisplay() })})"
                    EvaluationFactory.pass("Patient has $messageEnding", "Has $messageEnding")
                }

                treatmentsWithUncertainSetting.isNotEmpty() -> {
                    val messageEnding = "had prior systemic treatment but undetermined if in advanced or metastatic setting " +
                            "(${concatWithCommaAndAnd(treatmentsWithUncertainSetting.map { it.treatmentDisplay() })})"
                    EvaluationFactory.undetermined("Patient has $messageEnding", "Has $messageEnding")
                }

                else -> {
                    EvaluationFactory.fail(
                        "Patient has not had prior systemic treatment in advanced or metastatic setting",
                        "No prior systemic treatment in advanced or metastatic setting"
                    )
                }
            }
        }
    }