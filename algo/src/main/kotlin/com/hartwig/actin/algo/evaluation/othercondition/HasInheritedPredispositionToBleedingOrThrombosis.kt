package com.hartwig.actin.algo.evaluation.othercondition

import com.hartwig.actin.algo.doid.DoidConstants
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.othercondition.OtherConditionSelector
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.PriorOtherCondition
import com.hartwig.actin.doid.DoidModel

class HasInheritedPredispositionToBleedingOrThrombosis(private val doidModel: DoidModel) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val matchingDoid = OtherConditionSelector.selectClinicallyRelevant(record.priorOtherConditions)
            .flatMap(PriorOtherCondition::doids)
            .find {
                doidModel.doidWithParents(it).any(DOID_CONSTANTS_INDICATING_INHERITED_PREDISPOSITION_TO_BLEEDING_OR_THROMBOSIS::contains)
            }

        val hasMatchingName = OtherConditionSelector.selectClinicallyRelevant(record.priorOtherConditions)
            .any { it.name.lowercase().contains(NAME_INDICATING_INHERITED_PREDISPOSITION_TO_BLEEDING_OR_THROMBOSIS.lowercase()) }

        val baseMessage = "(typically) inherited predisposition to bleeding or thrombosis"

        return if (matchingDoid != null) {
            val matchingDoidTerm = doidModel.resolveTermForDoid(matchingDoid) ?: "DOID $matchingDoid"
            EvaluationFactory.pass("Patient has $baseMessage: $matchingDoidTerm", "History of $baseMessage: $matchingDoidTerm")
        } else if (hasMatchingName) {
            EvaluationFactory.pass(
                "Patient has $baseMessage: $NAME_INDICATING_INHERITED_PREDISPOSITION_TO_BLEEDING_OR_THROMBOSIS",
                "History of $baseMessage: $NAME_INDICATING_INHERITED_PREDISPOSITION_TO_BLEEDING_OR_THROMBOSIS"
            )
        } else {
            EvaluationFactory.fail("Patient has no $baseMessage", "No history of $baseMessage")
        }
    }

    companion object {
        val DOID_CONSTANTS_INDICATING_INHERITED_PREDISPOSITION_TO_BLEEDING_OR_THROMBOSIS = setOf(
            DoidConstants.AUTOSOMAL_HEMOPHILIA_A_DOID,
            DoidConstants.HEMOPHILIA_B_DOID,
            DoidConstants.VON_WILLEBRANDS_DISEASE_DOID,
            DoidConstants.FACTOR_V_DEFICIENCY_DOID,
            DoidConstants.PROTEIN_C_DEFICIENCY_DOID,
            DoidConstants.PROTEIN_S_DEFICIENCY_DOID,
            DoidConstants.ANTITHROMBIN_III_DEFICIENCY_DOID
        )
        const val NAME_INDICATING_INHERITED_PREDISPOSITION_TO_BLEEDING_OR_THROMBOSIS = "Factor V Leiden"
    }
}