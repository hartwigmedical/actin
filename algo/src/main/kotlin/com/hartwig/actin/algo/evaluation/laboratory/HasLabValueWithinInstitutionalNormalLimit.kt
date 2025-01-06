package com.hartwig.actin.algo.evaluation.laboratory

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.clinical.interpretation.LabMeasurement
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.LabValue

class HasLabValueWithinInstitutionalNormalLimit: LabEvaluationFunction {

    override fun evaluate(record: PatientRecord, labMeasurement: LabMeasurement, labValue: LabValue): Evaluation {
        val isOutsideRef = labValue.isOutsideRef
            ?: return EvaluationFactory.recoverableUndetermined("Undetermined if ${labMeasurement.display()} is within institutional normal limits")

        return if (isOutsideRef) {
            EvaluationFactory.recoverableFail(
                "${
                    labMeasurement.display().replaceFirstChar { it.uppercase() }
                } exceeds institutional normal limits"
            )
        } else {
            EvaluationFactory.recoverablePass(
                "${
                    labMeasurement.display().replaceFirstChar { it.uppercase() }
                } within institutional normal limits"
            )
        }
    }
}