package com.hartwig.actin.algo.evaluation.othercondition

import com.hartwig.actin.algo.doid.DoidConstants
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.util.ValueComparison.stringCaseInsensitivelyMatchesQueryCollection
import com.hartwig.actin.algo.othercondition.OtherConditionSelector
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.doid.DoidModel

class HasContraindicationToCT(private val doidModel: DoidModel) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        for (condition in OtherConditionSelector.selectClinicallyRelevant(record.priorOtherConditions)) {
            for (doid in condition.doids) {
                if (doidModel.doidWithParents(doid).contains(DoidConstants.KIDNEY_DISEASE_DOID)) {
                    return EvaluationFactory.pass("Potential CT contraindication: " + doidModel.resolveTermForDoid(doid))
                }
            }
            if (stringCaseInsensitivelyMatchesQueryCollection(condition.name, OTHER_CONDITIONS_BEING_CONTRAINDICATIONS_TO_CT)) {
                return EvaluationFactory.pass("Potential CT contraindication due to condition: " + condition.name)
            }
        }
        for (intolerance in record.intolerances) {
            if (stringCaseInsensitivelyMatchesQueryCollection(intolerance.name, INTOLERANCES_BEING_CONTRAINDICATIONS_TO_CT)) {
                return EvaluationFactory.pass("Potential CT contraindication due to intolerance: " + intolerance.name)
            }
        }
        for (complication in record.complications ?: emptyList()) {
            if (stringCaseInsensitivelyMatchesQueryCollection(complication.name, COMPLICATIONS_BEING_CONTRAINDICATIONS_TO_CT)) {
                return EvaluationFactory.pass("Potential CT contraindication due to complication: " + complication.name)
            }
        }
        return EvaluationFactory.fail("No potential contraindications to CT")
    }

    companion object {
        val OTHER_CONDITIONS_BEING_CONTRAINDICATIONS_TO_CT = setOf("claustrophobia")
        val INTOLERANCES_BEING_CONTRAINDICATIONS_TO_CT = setOf("contrast agent")
        val COMPLICATIONS_BEING_CONTRAINDICATIONS_TO_CT = setOf("hyperthyroidism")
    }
}