package com.hartwig.actin.algo.evaluation.othercondition

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.icd.IcdConstants
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.icd.IcdModel
import com.hartwig.actin.trial.input.datamodel.NyhaClass

class HasHistoryOfCongestiveHeartFailureWithNYHA(private val minimalClass: NyhaClass, private val icdModel: IcdModel): EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {

        val allExtensionCodes = listOf(
            IcdConstants.NYHA_CLASS_1_CODE,
            IcdConstants.NYHA_CLASS_2_CODE,
            IcdConstants.NYHA_CLASS_3_CODE,
            IcdConstants.NYHA_CLASS_4_CODE
        )

        val extensionCodes = allExtensionCodes.drop(minimalClass.ordinal)

        val matches = PriorOtherConditionFunctions.findPriorOtherConditionsMatchingAnyIcdCode(
            icdModel, record, listOf(IcdConstants.CONGESTIVE_HEART_FAILURE_CODE), extensionCodes
        )

        return when {
            matches.fullMatches.isNotEmpty() -> {
                EvaluationFactory.pass("Has history of congestive heart failure with at least NYHA class ${minimalClass.name}")
            }

            matches.mainCodeMatchesWithUnknownExtension.isNotEmpty() -> {
                EvaluationFactory.undetermined(
                    "Has history of congestive heart failure but undetermined if at least NYHA class ${minimalClass.name} (NYHA unknown)"
                )
            }

            else -> {
                EvaluationFactory.fail("No history of congestive heart failure with at least NYHA class ${minimalClass.name}")
            }
        }
    }
}