package com.hartwig.actin.algo.evaluation.comorbidity

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.util.Format
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.algo.StaticMessage
import com.hartwig.actin.datamodel.clinical.IcdCode
import com.hartwig.actin.icd.IcdModel

class HasHadComorbidityWithIcdCode(
    private val icdModel: IcdModel,
    private val targetIcdCodes: Set<IcdCode>,
    private val diseaseDescription: String,
) : EvaluationFunction {
    override fun evaluate(record: PatientRecord): Evaluation {
        val icdMatches = icdModel.findInstancesMatchingAnyIcdCode(
            record.comorbidities,
            targetIcdCodes
        )

        return when {
            icdMatches.fullMatches.isNotEmpty() -> {
                val messages = setOf(OtherConditionMessages.pass(icdMatches.fullMatches.map { it.display() }))
                Evaluation(
                    result = EvaluationResult.PASS,
                    recoverable = false,
                    passMessages = messages.map { StaticMessage(it) }.toSet()
                )
            }

            icdMatches.mainCodeMatchesWithUnknownExtension.isNotEmpty() -> EvaluationFactory.undetermined(
                "Has history of ${Format.concatItemsWithAnd(icdMatches.mainCodeMatchesWithUnknownExtension, true)} " +
                        "but undetermined if history of $diseaseDescription"
            )

            else -> EvaluationFactory.fail(OtherConditionMessages.fail(diseaseDescription))
        }
    }
}