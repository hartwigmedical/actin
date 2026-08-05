package com.hartwig.actin.algo.evaluation.comorbidity

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.EvaluationLabels
import com.hartwig.actin.algo.icd.IcdConstants
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.IcdCode
import com.hartwig.actin.icd.IcdModel

class HasInheritedPredispositionToBleedingOrThrombosis(private val icdModel: IcdModel, private val labels: EvaluationLabels.Comorbidity) :
    EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val icdMatchingComorbidities = icdModel.findInstancesMatchingAnyIcdCode(
            record.comorbidities,
            setOf(IcdCode(IcdConstants.HEREDITARY_THROMBOPHILIA_CODE), IcdCode(IcdConstants.HEREDITARY_BLEEDING_DISORDER_BLOCK))
        ).fullMatches

        val hasMatchingName = NAME_INDICATING_INHERITED_PREDISPOSITION_TO_BLEEDING_OR_THROMBOSIS.lowercase().let { query ->
            record.comorbidities.any { it.name?.lowercase()?.contains(query) == true }
        }

        val conditionString = icdMatchingComorbidities.joinToString(", ") { it.name ?: "Unknown" }

        return if (icdMatchingComorbidities.isNotEmpty()) {
            EvaluationFactory.pass(labels.hasInheritedPredispositionToBleedingOrThrombosisPass(conditionString))
        } else if (hasMatchingName) {
            EvaluationFactory.pass(
                labels.hasInheritedPredispositionToBleedingOrThrombosisPass(
                    NAME_INDICATING_INHERITED_PREDISPOSITION_TO_BLEEDING_OR_THROMBOSIS
                )
            )
        } else {
            EvaluationFactory.fail(labels.hasInheritedPredispositionToBleedingOrThrombosisFail())
        }
    }

    companion object {
        const val NAME_INDICATING_INHERITED_PREDISPOSITION_TO_BLEEDING_OR_THROMBOSIS = "Factor V Leiden"
    }
}