package com.hartwig.actin.algo.evaluation.cardiacfunction

import com.hartwig.actin.algo.doid.DoidConstants
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.othercondition.OtherConditionSelector
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.doid.DoidModel

class HasLongQTSyndrome(private val doidModel: DoidModel) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        for (condition in OtherConditionSelector.selectClinicallyRelevant(record.priorOtherConditions)) {
            if (condition.doids.any { doidModel.doidWithParents(it).contains(DoidConstants.LONG_QT_SYNDROME_DOID) }) {
                return EvaluationFactory.pass("Presence of long QT syndrome")
            }
        }
        return EvaluationFactory.fail("No presence of long QT syndrome")
    }
}