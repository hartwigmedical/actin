package com.hartwig.actin.algo.evaluation.complication

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.util.Format.concat
import com.hartwig.actin.algo.icd.IcdConstants
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.IcdCode
import com.hartwig.actin.icd.IcdModel

class HasLeptomeningealDisease(private val icdModel: IcdModel) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val hasConfirmedLeptomeningealDisease = icdModel.findInstancesMatchingAnyIcdCode(
            record.comorbidities, setOf(IcdCode(IcdConstants.LEPTOMENINGEAL_METASTASES_CODE))
        ).fullMatches.isNotEmpty()

        val tumorDetails = record.tumor
        val otherLesions = listOfNotNull(tumorDetails.otherLesions, tumorDetails.otherSuspectedLesions).flatten()

        return when {
            hasConfirmedLeptomeningealDisease -> {
                EvaluationFactory.pass("Has leptomeningeal disease")
            }

            filterPotentiallyMeningealLesions(tumorDetails.hasConfirmedCnsLesions(), otherLesions).isNotEmpty() -> {
                createWarnEvaluation(suspected = false, otherLesions)
            }

            filterPotentiallyMeningealLesions(tumorDetails.hasSuspectedCnsLesions, otherLesions).isNotEmpty() -> {
                createWarnEvaluation(suspected = true, otherLesions)
            }

            else -> {
                EvaluationFactory.fail("Patient does not have leptomeningeal disease", "No leptomeningeal disease")
            }
        }
    }

    companion object {
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