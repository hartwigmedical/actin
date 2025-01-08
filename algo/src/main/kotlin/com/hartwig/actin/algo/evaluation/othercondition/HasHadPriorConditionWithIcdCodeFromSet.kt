package com.hartwig.actin.algo.evaluation.othercondition

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.othercondition.OtherConditionSelector
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.IcdCode
import com.hartwig.actin.icd.IcdModel

class HasHadPriorConditionWithIcdCodeFromSet(
    private val icdModel: IcdModel, private val targetIcdCodes: Set<IcdCode>, private val priorOtherConditionTerm: String
) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val icdMatches = icdModel.findInstancesMatchingAnyIcdCode(
            OtherConditionSelector.selectClinicallyRelevant(record.priorOtherConditions),
            targetIcdCodes
        )

        return when {
            icdMatches.fullMatches.isNotEmpty() -> {
                val display = icdMatches.fullMatches.map { it.display() }.toSet()
                EvaluationFactory.pass(PriorConditionMessages.pass(display))
            }

            icdMatches.mainCodeMatchesWithUnknownExtension.isNotEmpty() -> {
                EvaluationFactory.undetermined(
                    "Has history of ${icdMatches.mainCodeMatchesWithUnknownExtension.map { it.display() }} " +
                            "but undetermined if history of $priorOtherConditionTerm"
                )
            }

            else -> {
                EvaluationFactory.fail(
                    PriorConditionMessages.fail(priorOtherConditionTerm),
                )
            }
        }
    }
}