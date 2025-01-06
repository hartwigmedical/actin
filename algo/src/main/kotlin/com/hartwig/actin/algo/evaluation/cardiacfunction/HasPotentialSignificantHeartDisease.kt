package com.hartwig.actin.algo.evaluation.cardiacfunction

import com.hartwig.actin.algo.doid.DoidConstants
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.util.Format.concatStringsWithAnd
import com.hartwig.actin.algo.evaluation.util.ValueComparison.stringCaseInsensitivelyMatchesQueryCollection
import com.hartwig.actin.algo.othercondition.OtherConditionSelector
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.doid.DoidModel

class HasPotentialSignificantHeartDisease(private val doidModel: DoidModel) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val ecg = record.clinicalStatus.ecg
        if (ecg != null && ecg.hasSigAberrationLatestECG) {
            return EvaluationFactory.pass("Potentially significant cardiac disease (ECG abnormalities present)")
        }
        val heartConditions = OtherConditionSelector.selectClinicallyRelevant(record.priorOtherConditions)
            .filter { condition -> isPotentiallyHeartDisease(condition.name) || containsPotentialHeartDiseaseDoid(condition.doids) }
            .map { it.name }.toSet()

        return if (heartConditions.isNotEmpty()) {
            EvaluationFactory.pass("Potentially significant cardiac disease (history of " + concatStringsWithAnd(heartConditions))
        } else EvaluationFactory.fail("No potential significant cardiac disease")
    }

    private fun containsPotentialHeartDiseaseDoid(doids: Collection<String>): Boolean {
        val expanded = doids.flatMap { doidModel.doidWithParents(it) }.toSet()
        return HEART_DISEASE_DOIDS.any { expanded.contains(it) }
    }

    companion object {
        val HEART_DISEASE_DOIDS =
            setOf(DoidConstants.HEART_DISEASE_DOID, DoidConstants.CORONARY_ARTERY_DISEASE_DOID)
        val HEART_DISEASE_TERMS = setOf("angina", "pacemaker", "ICD", "Cardioverter-defibrillator", "cardioversion")

        private fun isPotentiallyHeartDisease(name: String): Boolean {
            return stringCaseInsensitivelyMatchesQueryCollection(name, HEART_DISEASE_TERMS)
        }
    }
}