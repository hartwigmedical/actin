package com.hartwig.actin.algo.evaluation.othercondition

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.othercondition.PriorConditionMessages.Characteristic
import com.hartwig.actin.algo.evaluation.toxicity.ToxicityFunctions
import com.hartwig.actin.algo.evaluation.util.Format
import com.hartwig.actin.algo.othercondition.OtherConditionSelector
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.Complication
import com.hartwig.actin.datamodel.clinical.IcdCodeEntity
import com.hartwig.actin.datamodel.clinical.PriorOtherCondition
import com.hartwig.actin.datamodel.clinical.Toxicity
import com.hartwig.actin.datamodel.clinical.ToxicitySource
import com.hartwig.actin.icd.IcdModel
import java.time.LocalDate

class HasHadPriorConditionComplicationOrToxicityWithIcdCode(
    private val icdModel: IcdModel,
    private val targetIcdTitles: Set<String>,
    private val diseaseDescription: String,
    private val referenceDate: LocalDate
) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val targetIcdCodes = targetIcdTitles.map { icdModel.resolveCodeForTitle(it)!! }.toSet()
        val relevantConditions = OtherConditionSelector.selectClinicallyRelevant(record.priorOtherConditions)
        val relevantToxicities = ToxicityFunctions.selectRelevantToxicities(record, icdModel, referenceDate, emptyList())
            .filter { toxicity -> (toxicity.grade ?: 0) >= 2 || (toxicity.source == ToxicitySource.QUESTIONNAIRE) }
        val (fullMatches, undeterminedMatches) = icdModel.findInstancesMatchingAnyIcdCode(
            relevantConditions + (record.complications ?: emptyList()) + relevantToxicities,
            targetIcdCodes,
        )

        return when {
            fullMatches.isNotEmpty() -> {
                Evaluation(
                    result = EvaluationResult.PASS,
                    recoverable = false,
                    passSpecificMessages = passSpecificMessages(diseaseDescription, fullMatches.toSet()),
                    passGeneralMessages =
                    setOf(PriorConditionMessages.passGeneral(fullMatches.map { it.display() }.toSet()))
                )
            }

            undeterminedMatches.isNotEmpty() -> EvaluationFactory.undetermined(
                "Has history of ${Format.concatStringsWithAnd(undeterminedMatches.map { it.display() })} " +
                        "but undetermined if history of $diseaseDescription"
            )

            else -> EvaluationFactory.fail(PriorConditionMessages.failSpecific(diseaseDescription), PriorConditionMessages.failGeneral())
        }
    }

    companion object {
        private fun passSpecificMessages(targetIcdTitle: String, matches: Set<IcdCodeEntity>): Set<String> {
            return listOf(
                matches.filterIsInstance<PriorOtherCondition>() to Characteristic.CONDITION,
                matches.filterIsInstance<Complication>() to Characteristic.COMPLICATION,
                matches.filterIsInstance<Toxicity>() to Characteristic.TOXICITY
            ).filter { it.first.isNotEmpty() }
                .map { PriorConditionMessages.passSpecific(it.second, it.first.map { c -> c.display() }, targetIcdTitle) }.toSet()
        }
    }
}