package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.evaluation.EvaluationFactory.fail
import com.hartwig.actin.algo.evaluation.EvaluationFactory.pass
import com.hartwig.actin.algo.evaluation.EvaluationFactory.undetermined
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation

class HasMinimumSitesWithLesions (private val minimumSitesWithLesions: Int) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val tumorDetails = record.tumor
        val distinctCategorizedLesionLocations = listOf(
            tumorDetails.hasBoneLesions,
            tumorDetails.hasBrainLesions,
            tumorDetails.hasCnsLesions,
            tumorDetails.hasLiverLesions,
            tumorDetails.hasLungLesions,
            tumorDetails.hasLymphNodeLesions
        ).count { it == true }

        val otherLesionCount = ((tumorDetails.otherLesions ?: emptyList()) + listOfNotNull(tumorDetails.biopsyLocation))
            .filterNot { it.lowercase().contains("lymph") && true == tumorDetails.hasLymphNodeLesions }
            .count()

        val sitesWithLesionsLowerBound = distinctCategorizedLesionLocations + otherLesionCount.coerceAtMost(1)
        val sitesWithLesionsUpperBound = distinctCategorizedLesionLocations + otherLesionCount + 1
        return if (sitesWithLesionsLowerBound >= minimumSitesWithLesions) {
            pass(
                "Patient has at least $sitesWithLesionsLowerBound lesion sites which meets threshold of $minimumSitesWithLesions",
                EVALUATION_GENERAL_MESSAGE
            )
        } else if (sitesWithLesionsUpperBound >= minimumSitesWithLesions) {
            undetermined(
                "Patient has between $sitesWithLesionsLowerBound and $sitesWithLesionsUpperBound lesion sites, " +
                        "so it is unclear if threshold of $minimumSitesWithLesions is met", EVALUATION_GENERAL_MESSAGE
            )
        } else {
            fail(
                String.format(
                    "Patient has at most %d lesion sites, which is less than threshold %d",
                    sitesWithLesionsUpperBound,
                    minimumSitesWithLesions
                ), EVALUATION_GENERAL_MESSAGE
            )
        }
    }

    companion object {
        private const val EVALUATION_GENERAL_MESSAGE: String = "Minimum sites with lesions"
    }
}