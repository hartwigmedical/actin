package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.clinical.datamodel.treatment.DrugType

class HasReceivedHER2TargetingADC : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val priorHER2TargetingADC = record.clinical().oncologicalHistory()
            .filter {
                it.isOfType(DrugType.ANTIBODY_DRUG_CONJUGATE_TARGETED_THERAPY) == true && it.isOfType(DrugType.HER2_ANTIBODY) == true
            }

        return when {
            priorHER2TargetingADC.isNotEmpty() -> {
                EvaluationFactory.pass(
                    "Patient has received a HER2 targeting ADC as prior therapy (${
                        priorHER2TargetingADC.map {
                            it.treatmentDisplay()
                        }.joinToString(", ")
                    })",
                    "Has received prior HER2 targeting ADC ($priorHER2TargetingADC)"
                )
            }

            else -> {
                EvaluationFactory.fail(
                    "Patient has not received a HER2 targeting ADC as prior therapy",
                    "Has not received any prior HER2 targeting ADC"
                )
            }
        }
    }
}