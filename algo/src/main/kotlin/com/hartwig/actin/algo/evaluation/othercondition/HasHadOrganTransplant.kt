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
        val relevant = OtherConditionSelector.selectClinicallyRelevant(record.priorOtherConditions)
        val matchingConditions =
            icdModel.findInstancesMatchingAnyIcdCode(relevant, IcdConstants.TRANSPLANTATION_SET.map { IcdCode(it) }.toSet()).fullMatches

        val grouped = matchingConditions.groupBy { condition ->
            if (minYear == null) true else condition.year?.let { it >= minYear }
        }
        val passesDateRequirement = grouped[true] ?: emptyList()
        val withUnknownDate = grouped[null]

        return when {
            passesDateRequirement.isNotEmpty() -> {
                val dateMessage = minYear?.let { " since $minYear" } ?: ""
                EvaluationFactory.pass("Has had an organ transplant$dateMessage")
            }

            !withUnknownDate.isNullOrEmpty() -> {
                EvaluationFactory.undetermined("Has had an organ transplant but unclear if after $minYear (date unknown)")
            }

            else -> EvaluationFactory.fail("No history of organ transplant")
        }
    }
}