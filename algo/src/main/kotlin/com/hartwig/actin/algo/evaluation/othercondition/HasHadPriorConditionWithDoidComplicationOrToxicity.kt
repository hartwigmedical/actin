package com.hartwig.actin.algo.evaluation.othercondition

import com.hartwig.actin.algo.evaluation.EvaluationFactory.fail
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.complication.ComplicationFunctions.findComplicationNamesMatchingAnyCategory
import com.hartwig.actin.algo.evaluation.util.ValueComparison.stringCaseInsensitivelyMatchesQueryCollection
import com.hartwig.actin.algo.othercondition.OtherConditionSelector
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.ToxicitySource
import com.hartwig.actin.doid.DoidModel

class HasHadPriorConditionWithDoidComplicationOrToxicity(
    private val doidModel: DoidModel,
    private val doidToFind: String,
    private val complicationCategoryToFind: String,
    private val toxicityCategoryToFind: String
) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val doidTerm = doidModel.resolveTermForDoid(doidToFind) ?: "unknown doid"
        val matchingConditions =
            OtherConditionSelector.selectConditionsMatchingDoid(record.priorOtherConditions, doidToFind, doidModel)
        val matchingComplications = findComplicationNamesMatchingAnyCategory(record, listOf(complicationCategoryToFind))
        val matchingToxicities = record.toxicities.filter { toxicity ->
            (toxicity.grade ?: 0) >= 2 || (toxicity.source == ToxicitySource.QUESTIONNAIRE)
        }
            .filter { stringCaseInsensitivelyMatchesQueryCollection(toxicityCategoryToFind, it.categories) }
            .map { it.name }
            .toSet()

        return if (matchingConditions.isNotEmpty() || matchingComplications.isNotEmpty() || matchingToxicities.isNotEmpty()) {
            Evaluation(
                result = EvaluationResult.PASS,
                recoverable = false,
                passMessages = setOf(
                    PriorConditionMessages.pass(matchingConditions + matchingComplications + matchingToxicities)
                )
            )
        } else fail(PriorConditionMessages.failGeneral())
    }
}