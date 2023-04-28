package com.hartwig.actin.algo.evaluation.laboratory

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.clinical.datamodel.LabValue

class HasLabValueOutsideRefLimitUp internal constructor() : LabEvaluationFunction {
    override fun evaluate(record: PatientRecord, labValue: LabValue): Evaluation {
        val refLimitUp = labValue.refLimitUp()
            ?: return EvaluationFactory.undetermined(
                "Could not determine whether " + labValue.code() + " is outside ref limit up",
                "Undetermined if " + labValue.code() + " is outside ref limit up"
            )
        return if (labValue.value().compareTo(refLimitUp) > 0) {
            EvaluationFactory.pass(labValue.code() + " is outside ref limit up", labValue.code() + " out of range")
        } else {
            EvaluationFactory.fail(labValue.code() + " is below ref limit up", labValue.code() + " within range")
        }
    }
}