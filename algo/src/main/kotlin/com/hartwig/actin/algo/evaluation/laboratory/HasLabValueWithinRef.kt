package com.hartwig.actin.algo.evaluation.laboratory

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.clinical.datamodel.LabValue

class HasLabValueWithinRef internal constructor() : LabEvaluationFunction {
    override fun evaluate(record: PatientRecord, labValue: LabValue): Evaluation {
        val isOutsideRef = labValue.isOutsideRef
            ?: return EvaluationFactory.undetermined(
                "Could not determine whether " + labValue.code() + " is within ref range",
                "Undetermined if " + labValue.code() + " is within ref range"
            )
        return if (isOutsideRef) {
            EvaluationFactory.fail(labValue.code() + " is not within reference values", labValue.code() + " out of range")
        } else {
            EvaluationFactory.pass(labValue.code() + " is within reference values", labValue.code() + " within range")
        }
    }
}