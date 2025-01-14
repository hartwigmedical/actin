package com.hartwig.actin.algo.evaluation.othercondition

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.icd.IcdConstants
import com.hartwig.actin.algo.othercondition.OtherConditionSelector
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.IcdCode
import com.hartwig.actin.icd.IcdModel

class HasInheritedPredispositionToBleedingOrThrombosis(private val icdModel: IcdModel) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val icdMatchingConditions = icdModel.findInstancesMatchingAnyIcdCode(
            OtherConditionSelector.selectClinicallyRelevant(record.priorOtherConditions),
            setOf(IcdCode(IcdConstants.HEREDITARY_THROMBOPHILIA_CODE), IcdCode(IcdConstants.HEREDITARY_BLEEDING_DISORDER_BLOCK))
        ).fullMatches

        val hasMatchingName = OtherConditionSelector.selectClinicallyRelevant(record.priorOtherConditions)
            .any { it.name.lowercase().contains(NAME_INDICATING_INHERITED_PREDISPOSITION_TO_BLEEDING_OR_THROMBOSIS.lowercase()) }

        val baseMessage = "(typically) inherited predisposition to bleeding or thrombosis"
        val conditionString = icdMatchingConditions.joinToString(", ") { it.name }

        return if (icdMatchingConditions.isNotEmpty()) {
            EvaluationFactory.pass("Has history of $baseMessage: $conditionString")
        } else if (hasMatchingName) {
            EvaluationFactory.pass(
                "Has history of $baseMessage: $NAME_INDICATING_INHERITED_PREDISPOSITION_TO_BLEEDING_OR_THROMBOSIS"
            )
        } else {
            EvaluationFactory.fail("No history of $baseMessage")
        }
    }

    companion object {
        const val NAME_INDICATING_INHERITED_PREDISPOSITION_TO_BLEEDING_OR_THROMBOSIS = "Factor V Leiden"
    }
}