package com.hartwig.actin.algo.evaluation.cardiacfunction

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation

class HasNormalCardiacFunctionByMUGAOrTTE: EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val lvef = record.clinicalStatus.lvef

        return if (lvef != null && lvef < 0.5) {
            EvaluationFactory.warn("LVEF < 50%, uncertain if cardiac function by MUGA or TTE is considered normal")
        } else {
            EvaluationFactory.recoverableUndetermined("Normal cardiac function by MUGA or TTE undetermined")
        }
    }
}