package com.hartwig.actin.algo.evaluation.laboratory

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.clinical.datamodel.LabValue

class HasLabValueWithinInstitutionalNormalLimit internal constructor() : LabEvaluationFunction {

    override fun evaluate(record: PatientRecord, labValue: LabValue): Evaluation {
        val isOutsideRef = labValue.isOutsideRef
            ?: return EvaluationFactory.recoverableUndetermined(
                "Could not determine whether ${labValue.code()} is within institutional normal limits",
                "Undetermined if ${labValue.code()} is within institutional normal limits"
            )

        return if (isOutsideRef) {
            EvaluationFactory.recoverableFail(
                "${labValue.code()} exceeds institutional normal limits",
                "${labValue.code()} exceeds normal limits"
            )
        } else {
            EvaluationFactory.recoverablePass(
                "${labValue.code()} within institutional normal limits",
                "${labValue.code()} within normal limits"
            )
        }
    }
}