package com.hartwig.actin.algo.evaluation.laboratory

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.clinical.datamodel.LabValue
import com.hartwig.actin.clinical.interpretation.LabMeasurement

class HasLabValueWithinInstitutionalNormalLimit: LabEvaluationFunction {

    override fun evaluate(record: PatientRecord, labMeasurement: LabMeasurement, labValue: LabValue): Evaluation {
        val isOutsideRef = labValue.isOutsideRef
            ?: return EvaluationFactory.recoverableUndetermined(
                "Could not determine whether ${labMeasurement.display()} is within institutional normal limits",
                "Undetermined if ${labMeasurement.display()} is within institutional normal limits"
            )

        return if (isOutsideRef) {
            EvaluationFactory.recoverableFail(
                "${labMeasurement.display().replaceFirstChar { it.uppercase() }} exceeds institutional normal limits",
                "${labMeasurement.display().replaceFirstChar { it.uppercase() }} exceeds normal limits"
            )
        } else {
            EvaluationFactory.recoverablePass(
                "${labMeasurement.display().replaceFirstChar { it.uppercase() }} within institutional normal limits",
                "${labMeasurement.display().replaceFirstChar { it.uppercase() }} within normal limits"
            )
        }
    }
}