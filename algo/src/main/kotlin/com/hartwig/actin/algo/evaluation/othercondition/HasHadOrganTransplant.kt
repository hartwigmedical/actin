package com.hartwig.actin.algo.evaluation.othercondition

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.othercondition.OtherConditionSelector
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation

class HasHadOrganTransplant(private val minYear: Int?) : EvaluationFunction {
    
    override fun evaluate(record: PatientRecord): Evaluation {
        var hasOrganTransplantWithUnknownYear = false
        for (condition in OtherConditionSelector.selectClinicallyRelevant(record.priorOtherConditions)) {
            if (condition.category == ORGAN_TRANSPLANT_CATEGORY) {
                var isPass = minYear == null
                if (minYear != null) {
                    val conditionYear = condition.year
                    if (conditionYear == null) {
                        hasOrganTransplantWithUnknownYear = true
                    } else {
                        isPass = conditionYear >= minYear
                    }
                }
                if (isPass) {
                    return if (minYear != null) {
                        EvaluationFactory.pass("Has had organ transplant in or after $minYear")
                    } else {
                        EvaluationFactory.pass("Has had organ transplant")
                    }
                }
            }
        }
        return if (hasOrganTransplantWithUnknownYear) {
            EvaluationFactory.undetermined("Has had organ transplant but year unknown")
        } else
            EvaluationFactory.fail("No history of organ transplant")
    }

    companion object {
        const val ORGAN_TRANSPLANT_CATEGORY: String = "Organ transplant"
    }
}