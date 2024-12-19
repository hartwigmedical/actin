package com.hartwig.actin.algo.evaluation.infection

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.othercondition.OtherConditionSelector
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.IcdCode
import com.hartwig.actin.icd.IcdModel

class HasSpecificInfection(
    private val icdModel: IcdModel, private val icdCodes: Set<IcdCode>, private val term: String
) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {

        val matchingConditions =
            icdModel.findInstancesMatchingAnyIcdCode(OtherConditionSelector.selectClinicallyRelevant(record.priorOtherConditions), icdCodes)

        return when {
            matchingConditions.fullMatches.isNotEmpty() -> EvaluationFactory.pass("Prior $term infection in history")
            matchingConditions.mainCodeMatchesWithUnknownExtension.isNotEmpty() -> {
                EvaluationFactory.undetermined("Prior infection in history but undetermined if $term")
            }

            else -> EvaluationFactory.fail("No prior $term infection")
        }
    }
}