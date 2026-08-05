package com.hartwig.actin.algo.evaluation.cardiacfunction

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.EvaluationLabels
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation

class HasNormalCardiacFunctionByMugaOrTte(private val labels: EvaluationLabels.CardiacFunction) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val lvef = record.clinicalStatus.lvef

        return if (lvef != null && lvef < 0.5) {
            EvaluationFactory.warn(labels.hasNormalCardiacFunctionByMugaOrTteWarn())
        } else {
            EvaluationFactory.undetermined(labels.hasNormalCardiacFunctionByMugaOrTteUndetermined())
        }
    }
}