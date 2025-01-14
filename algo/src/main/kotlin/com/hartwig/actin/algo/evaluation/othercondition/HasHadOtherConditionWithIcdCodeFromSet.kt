package com.hartwig.actin.algo.evaluation.othercondition

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.IcdCode
import com.hartwig.actin.icd.IcdModel

class HasHadOtherConditionWithIcdCodeFromSet(
    private val icdModel: IcdModel, private val targetIcdCodes: Set<IcdCode>, private val otherConditionTerm: String
) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val icdMatches = icdModel.findInstancesMatchingAnyIcdCode(record.otherConditions, targetIcdCodes)

        return when {
            icdMatches.fullMatches.isNotEmpty() -> {
                val display = icdMatches.fullMatches.map { it.display() }.toSet()
                EvaluationFactory.pass(OtherConditionMessages.pass(display))
            }

            icdMatches.mainCodeMatchesWithUnknownExtension.isNotEmpty() -> {
                EvaluationFactory.undetermined(
                    "Has history of ${icdMatches.mainCodeMatchesWithUnknownExtension.map { it.display() }} " +
                            "but undetermined if history of $otherConditionTerm"
                )
            }

            else -> {
                EvaluationFactory.fail(
                    OtherConditionMessages.fail(otherConditionTerm),
                )
            }
        }
    }
}