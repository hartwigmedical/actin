package com.hartwig.actin.algo.evaluation.comorbidity

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.EvaluationLabels
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
    private val undeterminedFamilyConditions: UndeterminedFamilyConditions = UndeterminedFamilyConditions("", emptySet()),
    private val labels: EvaluationLabels.Comorbidity
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
                icdModel.findInstancesMatchingAnyIcdCode(record.comorbidities, targetCodes).fullMatches
            }

        return when {
            passFamilyConditions.isNotEmpty() -> {
                EvaluationFactory.pass(labels.hasSpecificFamilyHistoryPass(conditionDescription))
            }

            undeterminedFamilyHistoryConditions.isNotEmpty() -> {
                createUndetermined(undeterminedFamilyConditions.description, undeterminedFamilyHistoryConditions)
            }

            unspecifiedFamilyHistory.isNotEmpty() -> createUndetermined(labels.descriptionUnspecifiedDisease(), unspecifiedFamilyHistory)

            else -> EvaluationFactory.fail(labels.hasSpecificFamilyHistoryFail(conditionDescription))
        }
    }

    private fun createUndetermined(diseaseType: String, conditions: List<Displayable>): Evaluation {
        return EvaluationFactory.undetermined(
            labels.hasSpecificFamilyHistoryUndetermined(diseaseType, Format.concatItemsWithAnd(conditions), conditionDescription)
        )
    }
}

data class UndeterminedFamilyConditions(val description: String, val icdCodes: Set<IcdCode>)
data class PassFamilyConditions(val description: String, val icdCodes: Set<IcdCode>)