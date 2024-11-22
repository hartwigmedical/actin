package com.hartwig.actin.algo.evaluation.othercondition

import com.hartwig.actin.algo.doid.DoidConstants
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.util.ValueComparison.stringCaseInsensitivelyMatchesQueryCollection
import com.hartwig.actin.algo.othercondition.OtherConditionSelector
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.doid.DoidModel

class HasContraindicationToMRI(private val doidModel: DoidModel) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        for (condition in OtherConditionSelector.selectClinicallyRelevant(record.priorOtherConditions)) {
            for (doid in condition.doids) {
                if (doidModel.doidWithParents(doid).contains(DoidConstants.KIDNEY_DISEASE_DOID)) {
                    return EvaluationFactory.pass(
                        "Patient has a potential contraindication to MRI due to " + doidModel.resolveTermForDoid(doid),
                        "Potential MRI contraindication: " + doidModel.resolveTermForDoid(doid)
                    )
                }
            }
            if (stringCaseInsensitivelyMatchesQueryCollection(condition.name, OTHER_CONDITIONS_BEING_CONTRAINDICATIONS_TO_MRI)) {
                return EvaluationFactory.pass(
                    "Patient has a potential contraindication to MRI due to condition " + condition.name,
                    "Potential MRI contraindication: " + condition.name
                )
            }
        }
        for (intolerance in record.intolerances) {
            if (stringCaseInsensitivelyMatchesQueryCollection(intolerance.name, INTOLERANCES_BEING_CONTRAINDICATIONS_TO_MRI)) {
                return EvaluationFactory.pass(
                    "Patient has a potential contraindication to MRI due to intolerance " + intolerance.name,
                    "Potential MRI contraindication: " + intolerance.name
                )
            }
        }
        return EvaluationFactory.fail("No potential contraindications to MRI identified", "No potential contraindications to MRI")
    }

    companion object {
        val OTHER_CONDITIONS_BEING_CONTRAINDICATIONS_TO_MRI = listOf("implant", "claustrophobia")
        val INTOLERANCES_BEING_CONTRAINDICATIONS_TO_MRI = listOf("contrast agent")
    }
}