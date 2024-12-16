package com.hartwig.actin.algo.evaluation.othercondition

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.icd.IcdConstants
import com.hartwig.actin.algo.othercondition.OtherConditionSelector
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.IcdCode
import com.hartwig.actin.icd.IcdModel

class HasHadOrganTransplant(private val icdModel: IcdModel, private val minYear: Int?) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        var hasOrganTransplantWithUnknownYear = false

        val matchingConditions =
            PriorOtherConditionFunctions.findPriorOtherConditionsMatchingAnyIcdCode(
                icdModel,
                record,
                IcdConstants.TRANSPLANTATION_SET.map { IcdCode(it) }.toSet()
            ).fullMatches

        for (condition in OtherConditionSelector.selectClinicallyRelevant(matchingConditions)) {

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