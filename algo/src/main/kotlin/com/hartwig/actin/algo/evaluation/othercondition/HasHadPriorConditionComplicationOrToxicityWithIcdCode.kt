package com.hartwig.actin.algo.evaluation.othercondition

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.toxicity.ToxicityFunctions
import com.hartwig.actin.algo.evaluation.util.Format
import com.hartwig.actin.algo.othercondition.OtherConditionSelector
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
        val relevantConditions = OtherConditionSelector.selectClinicallyRelevant(record.priorOtherConditions)
        val relevantToxicities = ToxicityFunctions.selectRelevantToxicities(record, icdModel, referenceDate, emptyList())
            .filter { toxicity -> (toxicity.grade ?: 0) >= 2 || (toxicity.source == ToxicitySource.QUESTIONNAIRE) }

        val icdMatches = icdModel.findInstancesMatchingAnyIcdCode(
            relevantConditions + record.complications + relevantToxicities,
            targetIcdCodes
        )

        return when {
            icdMatches.fullMatches.isNotEmpty() -> {
                val messages = setOf(PriorConditionMessages.passGeneral(icdMatches.fullMatches.map { it.display() }))
                Evaluation(
                    result = EvaluationResult.PASS,
                    recoverable = false,
                    passSpecificMessages = messages,
                    passGeneralMessages = messages
                )
            }

            icdMatches.mainCodeMatchesWithUnknownExtension.isNotEmpty() -> EvaluationFactory.undetermined(
                "Has history of ${Format.concatItemsWithAnd(icdMatches.mainCodeMatchesWithUnknownExtension)} " +
                        "but undetermined if history of $diseaseDescription"
            )

            else -> EvaluationFactory.fail(PriorConditionMessages.failSpecific(diseaseDescription), PriorConditionMessages.failGeneral())
        }
    }
}