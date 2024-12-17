package com.hartwig.actin.algo.evaluation.othercondition

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.util.Format
import com.hartwig.actin.algo.icd.IcdConstants.FAMILY_HISTORY_OF_OTHER_SPECIFIED_HEALTH_PROBLEMS_CODE
import com.hartwig.actin.algo.icd.IcdConstants.FAMILY_HISTORY_OF_UNSPECIFIED_HEALTH_PROBLEMS_CODE
import com.hartwig.actin.datamodel.Displayable
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.IcdCode
import com.hartwig.actin.icd.IcdModel

class HasSpecificFamilyHistory(
    private val icdModel: IcdModel,
    private val conditionDescription: String,
    private val passFamilyConditions: PassFamilyConditions = PassFamilyConditions("", emptySet()),
    private val undeterminedFamilyConditions: UndeterminedFamilyConditions = UndeterminedFamilyConditions("", emptySet())
) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val (passFamilyConditions, unspecifiedFamilyHistory, undeterminedFamilyHistoryConditions) =
            setOf(
                passFamilyConditions.icdCodes,
                setOf(
                    IcdCode(FAMILY_HISTORY_OF_UNSPECIFIED_HEALTH_PROBLEMS_CODE),
                    IcdCode(FAMILY_HISTORY_OF_OTHER_SPECIFIED_HEALTH_PROBLEMS_CODE)
                ),
                undeterminedFamilyConditions.icdCodes
            ).map { targetCodes ->
                PriorOtherConditionFunctions.findRelevantPriorConditionsMatchingAnyIcdCode(
                    icdModel,
                    record,
                    targetCodes
                ).fullMatches
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

    private fun createUndetermined(diseaseType: String, conditions: List<Displayable>): Evaluation {
        return EvaluationFactory.undetermined(
            "Has family history of $diseaseType (${Format.concatItemsWithAnd(conditions)}) - undetermined if $conditionDescription"
        )
    }
}

data class UndeterminedFamilyConditions(val description: String, val icdCodes: Set<IcdCode>)
data class PassFamilyConditions(val description: String, val icdCodes: Set<IcdCode>)