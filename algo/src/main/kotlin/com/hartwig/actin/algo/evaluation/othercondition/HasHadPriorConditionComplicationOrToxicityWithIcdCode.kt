package com.hartwig.actin.algo.evaluation.othercondition

import com.hartwig.actin.algo.evaluation.EvaluationFactory.fail
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.complication.ComplicationFunctions
import com.hartwig.actin.algo.evaluation.othercondition.PriorConditionMessages.Characteristic
import com.hartwig.actin.algo.evaluation.toxicity.ToxicityFunctions
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.ToxicitySource
import com.hartwig.actin.icd.IcdModel
import java.time.LocalDate

class HasHadPriorConditionComplicationOrToxicityWithIcdCode(
    private val icdModel: IcdModel,
    private val targetIcdTitles: List<String>,
    private val diseaseDescription: String,
    private val referenceDate: LocalDate
) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val targetIcdCodes = targetIcdTitles.map { icdModel.titleToCodeMap[it]!! }

        val matchingPriorConditions =
            PriorOtherConditionFunctions.findPriorOtherConditionsMatchingAnyIcdCode(icdModel, record, targetIcdCodes).fullMatches
                .map { it.name }.toSet()

        val matchingComplications =
            ComplicationFunctions.findComplicationsMatchingAnyIcdCode(record, targetIcdCodes, icdModel).map { it.name }.toSet()
        val matchingToxicities = ToxicityFunctions.selectRelevantToxicities(record, icdModel, referenceDate, emptyList())
            .filter { toxicity -> (toxicity.grade ?: 0) >= 2 || (toxicity.source == ToxicitySource.QUESTIONNAIRE) }
            .filter { ToxicityFunctions.hasIcdMatch(it, targetIcdCodes, icdModel) }.map { it.name }.toSet()

        return if (matchingPriorConditions.isNotEmpty() || matchingComplications.isNotEmpty() || matchingToxicities.isNotEmpty()) {
            Evaluation(
                result = EvaluationResult.PASS,
                recoverable = false,
                passSpecificMessages = passSpecificMessages(
                    diseaseDescription,
                    matchingPriorConditions,
                    matchingComplications,
                    matchingToxicities
                ),
                passGeneralMessages = setOf(
                    PriorConditionMessages.passGeneral(matchingPriorConditions + matchingComplications + matchingToxicities)
                )
            )
        } else fail(PriorConditionMessages.failSpecific(diseaseDescription), PriorConditionMessages.failGeneral())
    }

    companion object {
        private fun passSpecificMessages(
            targetIcdTitle: String, matchingConditions: Set<String>, matchingComplications: Set<String>, matchingToxicities: Set<String>
        ): Set<String> {
            return listOf(
                matchingConditions to Characteristic.CONDITION,
                matchingComplications to Characteristic.COMPLICATION,
                matchingToxicities to Characteristic.TOXICITY
            ).filter { it.first.isNotEmpty() }.map { PriorConditionMessages.passSpecific(it.second, it.first, targetIcdTitle) }.toSet()
        }
    }
}