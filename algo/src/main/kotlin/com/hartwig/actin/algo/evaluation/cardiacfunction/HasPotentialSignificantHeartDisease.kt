package com.hartwig.actin.algo.evaluation.cardiacfunction

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.doid.DoidConstants
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.util.Format.concatLowercaseWithAndAbbreviationsInCapital
import com.hartwig.actin.algo.evaluation.util.ValueComparison.stringCaseInsensitivelyMatchesQueryCollection
import com.hartwig.actin.algo.othercondition.OtherConditionSelector
import com.hartwig.actin.doid.DoidModel

class HasPotentialSignificantHeartDisease internal constructor(private val doidModel: DoidModel) : EvaluationFunction {
    override fun evaluate(record: PatientRecord): Evaluation {
        val ecg = record.clinical().clinicalStatus().ecg()
        if (ecg != null && ecg.hasSigAberrationLatestECG()) {
            return EvaluationFactory.pass(
                "Patient has significant aberration on latest ECG and therefore potentially significant cardiac disease",
                "Potentially significant cardiac disease: present ECG aberrations"
            )
        }
        val heartConditions = OtherConditionSelector.selectClinicallyRelevant(record.clinical().priorOtherConditions())
            .filter { condition -> isPotentiallyHeartDisease(condition.name()) || containsPotentialHeartDiseaseDoid(condition.doids()) }
            .map { it.name() }.toSet()

        return if (heartConditions.isNotEmpty()) {
            EvaluationFactory.pass(
                "Patient has " + concatLowercaseWithAndAbbreviationsInCapital(heartConditions) + " and therefore potentially significant cardiac disease",
                "Potentially significant cardiac disease: history of " + concatLowercaseWithAndAbbreviationsInCapital(heartConditions)
            )
        } else EvaluationFactory.fail(
            "Patient has no potential significant cardiac disease", "No potential significant cardiac disease"
        )
    }

    private fun containsPotentialHeartDiseaseDoid(doids: Collection<String>): Boolean {
        val expanded = doids.flatMap { doidModel.doidWithParents(it) }.toSet()
        return HEART_DISEASE_DOIDS.any { expanded.contains(it) }
    }

    companion object {
        val HEART_DISEASE_DOIDS =
            setOf(DoidConstants.HEART_DISEASE_DOID, DoidConstants.HYPERTENSION_DOID, DoidConstants.CORONARY_ARTERY_DISEASE_DOID)
        val HEART_DISEASE_TERMS = setOf("angina", "pacemaker")

        private fun isPotentiallyHeartDisease(name: String): Boolean {
            return stringCaseInsensitivelyMatchesQueryCollection(name, HEART_DISEASE_TERMS)
        }
    }
}