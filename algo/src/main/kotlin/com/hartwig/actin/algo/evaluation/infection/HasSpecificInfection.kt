package com.hartwig.actin.algo.evaluation.infection

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.othercondition.PriorOtherConditionFunctions
import com.hartwig.actin.algo.othercondition.OtherConditionSelector
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.IcdCode
import com.hartwig.actin.icd.IcdModel

class HasSpecificInfection(
    private val icdModel: IcdModel, private val icdCodes: Set<IcdCode>, private val term: String
) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {

        val matchingConditions = OtherConditionSelector.selectClinicallyRelevant(record.priorOtherConditions)
            .flatMap {
                PriorOtherConditionFunctions.findPriorOtherConditionsMatchingAnyIcdCode(
                    icdModel,
                    record,
                    icdCodes
                ).fullMatches
            }

        return when {
            matchingConditions.isNotEmpty() -> EvaluationFactory.pass("Prior $term infection in history")

            else -> EvaluationFactory.fail("No prior $term infection")
        }
    }
}