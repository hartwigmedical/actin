package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation

class HasMinimumSitesWithLesions(private val minimumSitesWithLesions: Int) : EvaluationFunction {

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

        val otherLesionCount =
            ((tumorDetails.otherLesions ?: emptyList()) + listOfNotNull(tumorDetails.biopsyLocation))
                .filterNot { it.lowercase().contains("lymph") && true == tumorDetails.hasConfirmedOrSuspectedLymphNodeLesions() }
                .count()

        val distinctCategorizedSuspectedLesionLocations = listOf(
            tumorDetails.hasSuspectedBoneLesions,
            tumorDetails.hasSuspectedBrainLesions,
            tumorDetails.hasSuspectedCnsLesions,
            tumorDetails.hasSuspectedLiverLesions,
            tumorDetails.hasSuspectedLungLesions,
            tumorDetails.hasSuspectedLymphNodeLesions
        ).count { it == true }

        val otherSuspectedLesionCount = (tumorDetails.otherSuspectedLesions ?: emptyList())
            .filterNot { it.lowercase().contains("lymph") && true == tumorDetails.hasLymphNodeLesions }
            .count()

        val sitesWithLesionsLowerBound = distinctCategorizedLesionLocations + otherLesionCount.coerceAtMost(1)
        val sitesWithLesionsUpperBound = distinctCategorizedLesionLocations + otherLesionCount + 1

        val totalSitesWithLesionsLowerBound =
            sitesWithLesionsLowerBound + distinctCategorizedSuspectedLesionLocations + otherSuspectedLesionCount.coerceAtMost(1)
        val totalSitesWithLesionsUpperBound =
            sitesWithLesionsUpperBound + distinctCategorizedSuspectedLesionLocations + otherSuspectedLesionCount

        return when {
            sitesWithLesionsLowerBound >= minimumSitesWithLesions -> {
                EvaluationFactory.pass(
                    "Patient has at least $sitesWithLesionsLowerBound lesion sites which meets threshold of $minimumSitesWithLesions",
                    "Sufficient lesion sites"
                )
            }

            totalSitesWithLesionsLowerBound >= minimumSitesWithLesions -> {
                EvaluationFactory.pass(
                    "Patient has at least $totalSitesWithLesionsLowerBound lesion sites (when including $distinctCategorizedSuspectedLesionLocations suspected lesions) " +
                            "which meets threshold of $minimumSitesWithLesions",
                    "Sufficient lesions sites (when including suspected lesions)"
                )
            }

            sitesWithLesionsUpperBound >= minimumSitesWithLesions -> {
                EvaluationFactory.undetermined(
                    "Patient has between $sitesWithLesionsLowerBound and $sitesWithLesionsUpperBound confirmed lesion sites so it is unclear if the threshold of $minimumSitesWithLesions is met.",
                    "Undetermined if sufficient lesion sites (near threshold of $minimumSitesWithLesions)"
                )
            }

            totalSitesWithLesionsUpperBound >= minimumSitesWithLesions -> {
                EvaluationFactory.undetermined(
                    "Patient has between $sitesWithLesionsLowerBound and $totalSitesWithLesionsUpperBound lesion sites (including suspected lesions), " +
                            "so it is unclear if the threshold of $minimumSitesWithLesions is met. We assume suspected lesions contribute.",
                    "Undetermined if sufficient lesion sites (near threshold of $minimumSitesWithLesions and including suspected lesions)"
                )
            }

            else -> {
                EvaluationFactory.fail(
                    String.format(
                        "Patient has at most %d lesion sites, which is less than the threshold of %d",
                        totalSitesWithLesionsUpperBound,
                        minimumSitesWithLesions
                    ),
                    "Insufficient amount of lesion sites"
                )
            }
        }
    }
}