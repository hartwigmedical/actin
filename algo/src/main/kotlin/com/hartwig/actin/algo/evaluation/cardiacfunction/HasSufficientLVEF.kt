package com.hartwig.actin.algo.evaluation.cardiacfunction

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.EvaluationLabels
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation

class HasSufficientLVEF(private val minLVEF: Double, private val labels: EvaluationLabels.CardiacFunction) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val lvef = record.clinicalStatus.lvef

        return when {
            lvef == null -> EvaluationFactory.recoverableUndetermined(labels.hasSufficientLvefRecoverableUndetermined())
            lvef >= minLVEF -> EvaluationFactory.recoverablePass(labels.hasSufficientLvefRecoverablePass(lvef, minLVEF))
            else -> EvaluationFactory.recoverableFail(labels.hasSufficientLvefRecoverableFail(lvef, minLVEF))
        }
    }
}