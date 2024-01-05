package com.hartwig.actin.algo.evaluation.cardiacfunction

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction

class HasNormalCardiacFunctionByMUGAOrTTE internal constructor() : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val lvef = record.clinical.clinicalStatus.lvef

        return if (lvef != null && lvef < 0.5) {
            EvaluationFactory.warn(
                "LVEF of $lvef below 50%, uncertain if patient has normal cardiac function by MUGA or TTE",
                "LVEF < 50%, uncertain if cardiac function is considered normal"
            )
        } else {
            EvaluationFactory.notEvaluated(
                "Normal cardiac function by MUGA or TTE cannot be determined",
                "Undetermined normal cardiac function by MUGA or TTE"
            )
        }
    }
}