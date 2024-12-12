package com.hartwig.actin.algo.evaluation.othercondition

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.icd.IcdConstants
import com.hartwig.actin.algo.othercondition.OtherConditionSelector
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.IcdCode
import com.hartwig.actin.icd.IcdModel

class HasChildPughClass(private val icdModel: IcdModel) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val hasLiverCirrhosis = OtherConditionSelector.selectClinicallyRelevant(PriorOtherConditionFunctions.findPriorOtherConditionsMatchingAnyIcdCode(
            icdModel,
            record,
            setOf(IcdCode(IcdConstants.LIVER_CIRRHOSIS_CODE))
        ).fullMatches).isNotEmpty()

        return if (hasLiverCirrhosis) {
            EvaluationFactory.undetermined(
                "Currently Child-Pugh class cannot be determined",
                "Undetermined Child-Pugh class"
            )
        } else {
            EvaluationFactory.notEvaluated(
                "Child Pugh Score not relevant since liver cirrhosis not present in medical history",
                "Child Pugh not relevant since liver cirrhosis not present"
            )
        }
    }
}