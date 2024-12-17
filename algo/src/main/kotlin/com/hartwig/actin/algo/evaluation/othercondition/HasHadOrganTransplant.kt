package com.hartwig.actin.algo.evaluation.othercondition

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.icd.IcdConstants
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.IcdCode
import com.hartwig.actin.icd.IcdModel

class HasHadOrganTransplant(private val icdModel: IcdModel, private val minYear: Int?) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val matchingConditions =
            PriorOtherConditionFunctions.findRelevantPriorConditionsMatchingAnyIcdCode(
                icdModel,
                record,
                IcdConstants.TRANSPLANTATION_SET.map { IcdCode(it) }.toSet()
            ).fullMatches

        val grouped = matchingConditions.groupBy { condition -> minYear == null || condition.year?.let { it >= minYear } == true }
        val passesDateRequirement = grouped[true] ?: emptyList()
        val withUnknownDate = grouped[false]?.filter { it.year == null }

        return when {
            passesDateRequirement.isNotEmpty() -> {
                val dateMessage = minYear?.let { " since $minYear" } ?: ""
                EvaluationFactory.pass("Has had an organ transplant$dateMessage")
            }

            !withUnknownDate.isNullOrEmpty() -> {
                EvaluationFactory.undetermined("Has had an organ transplant but unclear if after $minYear (date unknown)")
            }

            else -> EvaluationFactory.fail("Has not had an organ transplant", "No organ transplant")
        }
    }
}