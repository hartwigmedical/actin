package com.hartwig.actin.algo.evaluation.priortumor

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.TumorStatus

class HasActiveSecondMalignancy : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        return if (record.priorPrimaries.any { it.status == TumorStatus.ACTIVE }) {
            EvaluationFactory.pass("Active second malignancy in provided history")
        } else if (record.priorPrimaries.any { it.status == TumorStatus.EXPECTATIVE }) {
            EvaluationFactory.warn("Second malignancy with expectative status in provided history")
        } else {
            EvaluationFactory.fail("No active second malignancy")
        }
    }
}