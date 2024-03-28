package com.hartwig.actin.algo.evaluation.othercondition

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.othercondition.OtherConditionSelector

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
                        EvaluationFactory.pass(
                            "Patient has had an organ transplant at some point in or after $minYear",
                            "Patient had organ transplant in or after $minYear"
                        )
                    } else {
                        EvaluationFactory.pass("Patient has had an organ transplant", "Has had organ transplant")
                    }
                }
            }
        }
        return if (hasOrganTransplantWithUnknownYear) {
            EvaluationFactory.undetermined(
                "Patient has had organ transplant but in unclear year",
                "Date of previous organ transplant unknown"
            )
        } else
            EvaluationFactory.fail("Patient has not had an organ transplant", "No organ transplant")
    }

    companion object {
        const val ORGAN_TRANSPLANT_CATEGORY: String = "Organ transplant"
    }
}