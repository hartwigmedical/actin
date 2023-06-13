package com.hartwig.actin.algo.evaluation.othercondition

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationFactory.fail
import com.hartwig.actin.algo.evaluation.EvaluationFactory.unrecoverable
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.complication.ComplicationFunctions.findComplicationNamesMatchingAnyCategory
import com.hartwig.actin.algo.evaluation.othercondition.PriorConditionMessages.Characteristic
import com.hartwig.actin.algo.evaluation.util.ValueComparison.stringCaseInsensitivelyMatchesQueryCollection
import com.hartwig.actin.algo.othercondition.OtherConditionSelector
import com.hartwig.actin.clinical.datamodel.ToxicitySource
import com.hartwig.actin.doid.DoidModel

class HasHadPriorConditionWithDoidComplicationOrToxicity internal constructor(
    private val doidModel: DoidModel,
    private val doidToFind: String,
    private val complicationCategoryToFind: String,
    private val toxicityCategoryToFind: String
) : EvaluationFunction {
    override fun evaluate(record: PatientRecord): Evaluation {
        val doidTerm = doidModel.resolveTermForDoid(doidToFind) ?: "unknown doid"
        val matchingConditions =
            OtherConditionSelector.selectConditionsMatchingDoid(record.clinical().priorOtherConditions(), doidToFind, doidModel)
        val matchingComplications: Set<String> = findComplicationNamesMatchingAnyCategory(
            record, listOf(
                complicationCategoryToFind
            )
        )
        val matchingToxicities =
            record.clinical().toxicities().filter { (it.grade() ?: 0) >= 2 || (it.source() == ToxicitySource.QUESTIONNAIRE) }
                .filter { stringCaseInsensitivelyMatchesQueryCollection(toxicityCategoryToFind, it.categories()) }.map { it.name() }.toSet()

        return if (matchingConditions.isNotEmpty() || matchingComplications.isNotEmpty() || matchingToxicities.isNotEmpty()) {
            unrecoverable().result(EvaluationResult.PASS).addAllPassSpecificMessages(
                passSpecificMessages(
                    doidTerm, matchingConditions, matchingComplications, matchingToxicities
                )
            ).addPassGeneralMessages(PriorConditionMessages.passGeneral(matchingConditions + matchingComplications + matchingToxicities))
                .build()
        } else fail(PriorConditionMessages.failSpecific(doidTerm), PriorConditionMessages.failGeneral())
    }

    companion object {
        private fun passSpecificMessages(
            doidTerm: String, matchingConditions: Set<String>, matchingComplications: Set<String>, matchingToxicities: Set<String>
        ): List<String> {
            return listOf(
                matchingConditions to Characteristic.CONDITION,
                matchingComplications to Characteristic.COMPLICATION,
                matchingToxicities to Characteristic.TOXICITY
            ).filter { it.first.isNotEmpty() }.map { PriorConditionMessages.passSpecific(it.second, it.first, doidTerm) }
        }
    }
}