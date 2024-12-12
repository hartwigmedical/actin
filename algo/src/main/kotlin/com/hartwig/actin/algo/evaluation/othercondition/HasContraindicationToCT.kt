package com.hartwig.actin.algo.evaluation.othercondition

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.complication.ComplicationFunctions
import com.hartwig.actin.algo.evaluation.util.Format
import com.hartwig.actin.algo.evaluation.util.ValueComparison.stringCaseInsensitivelyMatchesQueryCollection
import com.hartwig.actin.algo.icd.IcdConstants
import com.hartwig.actin.algo.othercondition.OtherConditionSelector
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.IcdCode
import com.hartwig.actin.icd.IcdModel

class HasContraindicationToCT(private val icdModel: IcdModel) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val targetIcdCode = setOf(IcdCode(IcdConstants.KIDNEY_FAILURE_BLOCK))
        val relevantConditions = OtherConditionSelector.selectClinicallyRelevant(record.priorOtherConditions)
        val conditionsMatchingCode = relevantConditions.flatMap {
            PriorOtherConditionFunctions.findPriorOtherConditionsMatchingAnyIcdCode(icdModel, record, targetIcdCode).fullMatches
        }.map { it.name }
        val conditionsMatchingString = relevantConditions.filter {
            stringCaseInsensitivelyMatchesQueryCollection(it.name, OTHER_CONDITIONS_BEING_CONTRAINDICATIONS_TO_CT)
        }
        val intolerances =
            record.intolerances.filter {
                stringCaseInsensitivelyMatchesQueryCollection(
                    it.name,
                    INTOLERANCES_BEING_CONTRAINDICATIONS_TO_CT
                )
            }

        val complications =
            ComplicationFunctions.findComplicationsMatchingAnyIcdCode(icdModel, record, targetIcdCode).fullMatches.map { it.name }

        val conditionString = Format.concatWithCommaAndAnd(conditionsMatchingCode)
        val messageStart = "Potential CT contraindication: "

        return when {
            conditionsMatchingCode.isNotEmpty() -> EvaluationFactory.recoverablePass(messageStart + conditionString)

            conditionsMatchingString.isNotEmpty() -> EvaluationFactory.recoverablePass(
                messageStart + Format.concatWithCommaAndAnd(
                    conditionsMatchingString.map { it.name })
            )

            intolerances.isNotEmpty() -> EvaluationFactory.recoverablePass(messageStart + Format.concatWithCommaAndAnd(intolerances.map { it.name }))

            complications.isNotEmpty() -> EvaluationFactory.recoverablePass(messageStart + Format.concatWithCommaAndAnd(complications))

            else -> EvaluationFactory.fail("No potential contraindications to CT identified", "No potential contraindications to CT")
        }
    }

    companion object {
        val OTHER_CONDITIONS_BEING_CONTRAINDICATIONS_TO_CT = setOf("claustrophobia")
        val INTOLERANCES_BEING_CONTRAINDICATIONS_TO_CT = setOf("contrast agent")
    }
}