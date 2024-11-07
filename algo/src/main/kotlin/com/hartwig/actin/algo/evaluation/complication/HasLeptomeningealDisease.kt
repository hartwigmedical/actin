package com.hartwig.actin.algo.evaluation.complication

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.util.Format.concat
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation

class HasLeptomeningealDisease : EvaluationFunction {
    override fun evaluate(record: PatientRecord): Evaluation {
        val leptomeningealComplications = ComplicationFunctions.findComplicationNamesMatchingAnyCategory(
            record, LEPTOMENINGEAL_DISEASE_CATEGORY_PATTERNS
        )
        val tumorDetails = record.tumor
        val hasConfirmedOrSuspectedCnsLesions =
            listOf(tumorDetails.hasConfirmedCnsLesions(), tumorDetails.hasSuspectedCnsLesions).any { it == true }
        val otherLesions = listOfNotNull(tumorDetails.otherLesions, tumorDetails.otherSuspectedLesions).flatten()

        val potentialMeningealLesions = if (hasConfirmedOrSuspectedCnsLesions && otherLesions.isNotEmpty()) {
            otherLesions.filter { isPotentialLeptomeningealLesion(it) }.toSet()
        } else emptySet()

        return when {
            leptomeningealComplications.isNotEmpty() -> {
                return EvaluationFactory.pass(
                    "Patient has complication " + concat(leptomeningealComplications), "Present " + concat(leptomeningealComplications)
                )
            }
            potentialMeningealLesions.isNotEmpty() -> {
                EvaluationFactory.warn(
                    "Patient has lesions indicating potential leptomeningeal disease: " + concat(potentialMeningealLesions),
                    "Presence of lesions potentially indicating leptomeningeal disease"
                )
            }
            else -> EvaluationFactory.fail(
                "Patient does not have leptomeningeal disease", "No leptomeningeal disease"
            )
        }
    }

    companion object {
        private val LEPTOMENINGEAL_DISEASE_CATEGORY_PATTERNS = listOf("leptomeningeal disease", "leptomeningeal metastases")
        private val LESION_WARNING_PATTERNS = setOf(listOf("leptomeningeal"), listOf("carcinomatous", "meningitis"))

        private fun isPotentialLeptomeningealLesion(lesion: String): Boolean {
            return PatternMatcher.isMatch(lesion, LESION_WARNING_PATTERNS)
        }
    }
}