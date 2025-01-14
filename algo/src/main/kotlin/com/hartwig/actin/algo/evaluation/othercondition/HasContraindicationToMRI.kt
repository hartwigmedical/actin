package com.hartwig.actin.algo.evaluation.othercondition

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.util.Format
import com.hartwig.actin.algo.evaluation.util.ValueComparison.stringCaseInsensitivelyMatchesQueryCollection
import com.hartwig.actin.algo.icd.IcdConstants
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.IcdCode
import com.hartwig.actin.icd.IcdModel

class HasContraindicationToMRI(private val icdModel: IcdModel) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val targetCodes = setOf(IcdCode(IcdConstants.KIDNEY_FAILURE_BLOCK), IcdCode(IcdConstants.PRESENCE_OF_DEVICE_IMPLANT_OR_GRAFT_BLOCK))

        val matchingComorbidities = icdModel.findInstancesMatchingAnyIcdCode(record.comorbidities, targetCodes).fullMatches

        val comorbiditiesMatchingString = record.comorbidities.filter {
            stringCaseInsensitivelyMatchesQueryCollection(
                it.name, OTHER_CONDITIONS_BEING_CONTRAINDICATIONS_TO_MRI + INTOLERANCES_BEING_CONTRAINDICATIONS_TO_MRI
            )
        }

        val conditionString = Format.concatItemsWithAnd(matchingComorbidities)
        val messageStart = "Potential MRI contraindication: "

        return when {
            matchingComorbidities.isNotEmpty() -> EvaluationFactory.recoverablePass(messageStart + conditionString)

            comorbiditiesMatchingString.isNotEmpty() -> {
                EvaluationFactory.recoverablePass(messageStart + Format.concatItemsWithAnd(comorbiditiesMatchingString))
            }

            else -> EvaluationFactory.fail("No potential contraindications to MRI")
        }
    }

    companion object {
        val OTHER_CONDITIONS_BEING_CONTRAINDICATIONS_TO_MRI = listOf("claustrophobia")
        val INTOLERANCES_BEING_CONTRAINDICATIONS_TO_MRI = listOf("contrast agent")
    }
}