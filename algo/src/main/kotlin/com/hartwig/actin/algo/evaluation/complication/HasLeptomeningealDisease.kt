package com.hartwig.actin.algo.evaluation.complication

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.util.Format.concat

class HasLeptomeningealDisease internal constructor() : EvaluationFunction {
    override fun evaluate(record: PatientRecord): Evaluation {
        val leptomeningealComplications = ComplicationFunctions.findComplicationNamesMatchingAnyCategory(
            record, listOf(
                LEPTOMENINGEAL_DISEASE_CATEGORY_PATTERN
            )
        )
        if (leptomeningealComplications.isNotEmpty()) {
            return EvaluationFactory.pass(
                "Patient has complication " + concat(leptomeningealComplications), "Present " + concat(leptomeningealComplications)
            )
        }
        val hasCnsLesions = record.clinical.tumor.hasCnsLesions
        val otherLesions = record.clinical.tumor.otherLesions
        val potentialMeningealLesions = if (hasCnsLesions != null && otherLesions != null && hasCnsLesions) {
            otherLesions.filter { isPotentialLeptomeningealLesion(it) }.toSet()
        } else emptySet()

        return if (potentialMeningealLesions.isNotEmpty()) {
            EvaluationFactory.warn(
                "Patient has lesions indicating potential leptomeningeal disease: " + concat(potentialMeningealLesions),
                "Presence of lesions potentially indicating leptomeningeal disease"
            )
        } else EvaluationFactory.fail(
            "Patient does not have leptomeningeal disease", "No leptomeningeal disease"
        )
    }

    companion object {
        private const val LEPTOMENINGEAL_DISEASE_CATEGORY_PATTERN = "leptomeningeal disease"
        private val LESION_WARNING_PATTERNS = setOf(listOf("leptomeningeal"), listOf("carcinomatous", "meningitis"))

        private fun isPotentialLeptomeningealLesion(lesion: String): Boolean {
            return PatternMatcher.isMatch(lesion, LESION_WARNING_PATTERNS)
        }
    }
}