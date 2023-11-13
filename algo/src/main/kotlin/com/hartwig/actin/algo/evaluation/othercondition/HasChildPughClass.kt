package com.hartwig.actin.algo.evaluation.othercondition

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.doid.DoidConstants
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.othercondition.OtherConditionSelector
import com.hartwig.actin.doid.DoidModel

class HasChildPughClass(private val doidModel: DoidModel) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        for (condition in OtherConditionSelector.selectClinicallyRelevant(record.clinical().priorOtherConditions())) {
            if (condition.doids().any { doidModel.doidWithParents(it).contains(DoidConstants.LIVER_CIRRHOSIS_DOID) }) {
                return EvaluationFactory.undetermined(
                    "Currently Child-Pugh class cannot be determined",
                    "Undetermined Child-Pugh class"
                )
            }
        }
        return EvaluationFactory.notEvaluated(
            "Child Pugh Score not relevant since liver cirrhosis not present in medical history",
            "Child Pugh not relevant since liver cirrhosis not present"
        )
    }
}