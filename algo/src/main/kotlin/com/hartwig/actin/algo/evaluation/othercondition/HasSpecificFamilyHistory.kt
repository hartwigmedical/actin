package com.hartwig.actin.algo.evaluation.othercondition

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.util.Format
import com.hartwig.actin.algo.icd.IcdConstants.FAMILY_HISTORY_OF_OTHER_SPECIFIED_HEALTH_PROBLEMS_CODE
import com.hartwig.actin.algo.icd.IcdConstants.FAMILY_HISTORY_OF_UNSPECIFIED_HEALTH_PROBLEMS_CODE
import com.hartwig.actin.algo.othercondition.OtherConditionSelector
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.icd.IcdModel

class HasSpecificFamilyHistory(
    private val icdModel: IcdModel,
    private val conditionDescription: String,
    private val passFamilyConditions: PassFamilyConditions = PassFamilyConditions("", emptyList()),
    private val undeterminedFamilyConditions: UndeterminedFamilyConditions = UndeterminedFamilyConditions("", emptyList())
) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val (passFamilyConditions, unspecifiedFamilyHistory, undeterminedFamilyHistoryConditions) =
            listOf(
                passFamilyConditions.icdCodes,
                listOf(FAMILY_HISTORY_OF_UNSPECIFIED_HEALTH_PROBLEMS_CODE, FAMILY_HISTORY_OF_OTHER_SPECIFIED_HEALTH_PROBLEMS_CODE),
                undeterminedFamilyConditions.icdCodes
            ).map { targetCodes ->
                OtherConditionSelector
                    .selectClinicallyRelevant(record.priorOtherConditions)
                    .flatMap {
                        PriorOtherConditionFunctions.findPriorOtherConditionsMatchingAnyIcdCode(icdModel, record, targetCodes).fullMatches
                            .map { it.name }
                    }
            }

        return when {
            passFamilyConditions.isNotEmpty() -> {
                EvaluationFactory.pass("Has family history of $conditionDescription")
            }

            undeterminedFamilyHistoryConditions.isNotEmpty() -> {
                createUndetermined(undeterminedFamilyConditions.description, undeterminedFamilyHistoryConditions)
            }

            unspecifiedFamilyHistory.isNotEmpty() -> createUndetermined("unspecified disease", unspecifiedFamilyHistory)

            else -> EvaluationFactory.fail("No presence of family history of $conditionDescription")
        }
    }

    private fun createUndetermined(diseaseType: String, conditions: List<String>): Evaluation {
        return EvaluationFactory.undetermined(
            "Has family history of $diseaseType (${Format.concatWithCommaAndAnd(conditions)}) - undetermined if $conditionDescription"
        )
    }
}

data class UndeterminedFamilyConditions(val description: String, val icdCodes: List<String>)
data class PassFamilyConditions(val description: String, val icdCodes: List<String>)