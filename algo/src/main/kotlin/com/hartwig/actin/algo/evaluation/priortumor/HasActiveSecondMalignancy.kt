package com.hartwig.actin.algo.evaluation.priortumor

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.TumorStatus

class HasActiveSecondMalignancy : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        return if (record.priorPrimaries.any { it.status == TumorStatus.ACTIVE }) {
            EvaluationFactory.pass("Presence of active second malignancy")
        } else if (record.priorPrimaries.any { it.status == TumorStatus.EXPECTATIVE }) {
            EvaluationFactory.warn("Presence of second malignancy with expectative status")
        } else {
            EvaluationFactory.fail("No active second malignancy")
        }
    }
}