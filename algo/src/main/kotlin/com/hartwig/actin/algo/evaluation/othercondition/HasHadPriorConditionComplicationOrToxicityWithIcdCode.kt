package com.hartwig.actin.algo.evaluation.othercondition

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.toxicity.ToxicityFunctions
import com.hartwig.actin.algo.evaluation.util.Format
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.IcdCode
import com.hartwig.actin.datamodel.clinical.ToxicitySource
import com.hartwig.actin.icd.IcdModel
import java.time.LocalDate

class HasHadPriorConditionComplicationOrToxicityWithIcdCode(
    private val icdModel: IcdModel,
    private val targetIcdCodes: Set<IcdCode>,
    private val diseaseDescription: String,
    private val referenceDate: LocalDate
) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val relevantToxicities = ToxicityFunctions.selectRelevantToxicities(record, icdModel, referenceDate, emptyList())
            .filter { toxicity -> (toxicity.grade ?: 0) >= 2 || toxicity.source == ToxicitySource.QUESTIONNAIRE }

        val icdMatches = icdModel.findInstancesMatchingAnyIcdCode(
            record.priorOtherConditions + record.complications + relevantToxicities,
            targetIcdCodes
        )

        return when {
            icdMatches.fullMatches.isNotEmpty() -> {
                val messages = setOf(PriorConditionMessages.pass(icdMatches.fullMatches.map { it.display() }))
                Evaluation(
                    result = EvaluationResult.PASS,
                    recoverable = false,
                    passMessages = messages
                )
            }

            icdMatches.mainCodeMatchesWithUnknownExtension.isNotEmpty() -> EvaluationFactory.undetermined(
                "Has history of ${Format.concatItemsWithAnd(icdMatches.mainCodeMatchesWithUnknownExtension)} " +
                        "but undetermined if history of $diseaseDescription"
            )

            else -> EvaluationFactory.fail(PriorConditionMessages.fail(diseaseDescription))
        }
    }
}