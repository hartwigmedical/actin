package com.hartwig.actin.algo.evaluation.othercondition

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.doid.DoidConstants
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.othercondition.OtherConditionSelector
import com.hartwig.actin.doid.DoidModel

class HasInheritedPredispositionToBleedingOrThrombosis (private val doidModel: DoidModel) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        for (condition in OtherConditionSelector.selectClinicallyRelevant(record.clinical.priorOtherConditions)) {
            for (doid in condition.doids) {
                for (doidToMatch in DOID_CONSTANTS_INDICATING_INHERITED_PREDISPOSITION_TO_BLEEDING_OR_THROMBOSIS) {
                    if (doidModel.doidWithParents(doid).contains(doidToMatch)) {
                        return EvaluationFactory.pass(
                            "Patient has inherited predisposition to bleeding or thrombosis: " + doidModel.resolveTermForDoid(doid),
                            "History of inherited predisposition to bleeding or thrombosis : " + doidModel.resolveTermForDoid(doid)
                        )
                    }
                }
            }
        }
        return EvaluationFactory.fail(
            "Patient has no inherited predisposition to bleeding or thrombosis",
            "No inherited predisposition to bleeding or thrombosis"
        )
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
    }
}