package com.hartwig.actin.algo.evaluation.othercondition

import com.hartwig.actin.algo.doid.DoidConstants
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.util.ValueComparison.stringCaseInsensitivelyMatchesQueryCollection
import com.hartwig.actin.algo.othercondition.OtherConditionSelector
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.ToxicitySource
import com.hartwig.actin.doid.DoidModel

class HasHistoryOfPneumonitis(private val doidModel: DoidModel) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        for (condition in OtherConditionSelector.selectClinicallyRelevant(record.priorOtherConditions)) {
            for (doid in condition.doids) {
                if (doidModel.doidWithParents(doid).contains(DoidConstants.PNEUMONITIS_DOID)) {
                    return EvaluationFactory.pass("History of pneumonitis: " + doidModel.resolveTermForDoid(doid))
                }
            }
        }
        for (toxicity in record.toxicities) {
            if (toxicity.source == ToxicitySource.QUESTIONNAIRE || (toxicity.grade ?: 0) >= 2) {
                if (stringCaseInsensitivelyMatchesQueryCollection(toxicity.name, TOXICITIES_CAUSING_PNEUMONITIS)) {
                    return EvaluationFactory.pass("History of pneumonitis: " + toxicity.name)
                }
            }
        }
        return EvaluationFactory.fail("No history of pneumonitis")
    }

    companion object {
        val TOXICITIES_CAUSING_PNEUMONITIS = setOf("pneumonia", "pneumonitis")
    }
}