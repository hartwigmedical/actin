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
        val otherLesions = listOfNotNull(tumorDetails.otherLesions, tumorDetails.otherSuspectedLesions).flatten()
        val (hasSuspectedPotentialMeningealLesions, hasConfirmedPotentialMeningealLesions) = listOf(
            tumorDetails.hasSuspectedCnsLesions,
            tumorDetails.hasConfirmedCnsLesions()
        ).map { filterPotentiallyMeningealLesions(it, otherLesions).isNotEmpty() }

        return when {
            leptomeningealComplications.isNotEmpty() -> {
                return EvaluationFactory.pass(
                    "Patient has complication " + concat(leptomeningealComplications), "Present " + concat(leptomeningealComplications)
                )
            }

            hasConfirmedPotentialMeningealLesions -> createWarnEvaluation(suspected = false, otherLesions)

            hasSuspectedPotentialMeningealLesions -> createWarnEvaluation(suspected = true, otherLesions)

            else -> EvaluationFactory.fail(
                "Patient does not have leptomeningeal disease", "No leptomeningeal disease"
            )
        }
    }

    companion object {
        private val LEPTOMENINGEAL_DISEASE_CATEGORY_PATTERNS = listOf("leptomeningeal disease", "leptomeningeal metastases")
        private val LESION_WARNING_PATTERNS = setOf(listOf("leptomeningeal"), listOf("carcinomatous", "meningitis"))

        private fun filterPotentiallyMeningealLesions(hasLesions: Boolean?, otherLesions: List<String>): Set<String> {
            return if (hasLesions == true && otherLesions.isNotEmpty()) {
                otherLesions.filter { lesion -> PatternMatcher.isMatch(lesion, LESION_WARNING_PATTERNS) }.toSet()
            } else emptySet()
        }

        private fun createWarnEvaluation(suspected: Boolean, lesions: List<String>): Evaluation {
            val suspectedString = if (suspected) " suspected" else ""
            return EvaluationFactory.warn(
                "Patient has$suspectedString lesion indicating potential leptomeningeal disease: " + concat(lesions),
                "Presence of$suspectedString lesions potentially indicating leptomeningeal disease"
            )
        }
    }
}