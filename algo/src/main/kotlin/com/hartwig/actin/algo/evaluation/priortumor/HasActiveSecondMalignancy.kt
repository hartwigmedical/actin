package com.hartwig.actin.algo.evaluation.priortumor

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.clinical.datamodel.TumorStatus

class HasActiveSecondMalignancy internal constructor() : EvaluationFunction {
    override fun evaluate(record: PatientRecord): Evaluation {
        return if (record.priorSecondPrimaries.any { it.status == TumorStatus.ACTIVE }) {
            EvaluationFactory.pass("Patient has second malignancy considered active", "Presence of second malignancy considered active")
        } else if (record.priorSecondPrimaries.any { it.status == TumorStatus.EXPECTATIVE }) {
            EvaluationFactory.warn(
                "Patient has second malignancy considered expectative",
                "Presence of second malignancy considered expectative"
            )
        } else {
            EvaluationFactory.fail("Patient has no active second malignancy", "No active second malignancy")
        }
    }
}