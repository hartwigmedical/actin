package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation

class HasMinimumSitesWithLesions(private val minimumSitesWithLesions: Int) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        with(record.tumor) {
            val distinctCategorizedLesionLocations = confirmedCategoricalLesionList().count { it == true }

            val otherLesionCount =
                ((otherLesions ?: emptyList()) + listOfNotNull(biopsyLocation))
                    .filterNot { it.lowercase().contains("lymph") && true == hasLymphNodeLesions }
                    .count()

            val distinctCategorizedSuspectedLesionLocations = record.tumor.suspectedCategoricalLesionList().count { it == true }

            val otherSuspectedLesionCount = (otherSuspectedLesions ?: emptyList())
                .filterNot { it.lowercase().contains("lymph") && true == hasLymphNodeLesions }
                .count()

            val sitesWithKnownLesionsLowerBound = distinctCategorizedLesionLocations + otherLesionCount.coerceAtMost(1)
            val sitesWithKnownLesionsUpperBound = distinctCategorizedLesionLocations + otherLesionCount + 1

            val sitesWithKnownAndSuspectedLesionsLowerBound =
                sitesWithKnownLesionsLowerBound + distinctCategorizedSuspectedLesionLocations + otherSuspectedLesionCount.coerceAtMost(1)
            val sitesWithKnownAndSuspectedLesionsUpperBound =
                sitesWithKnownLesionsUpperBound + distinctCategorizedSuspectedLesionLocations + otherSuspectedLesionCount

            return when {
                sitesWithKnownLesionsLowerBound >= minimumSitesWithLesions -> {
                    EvaluationFactory.pass(
                        "Patient has at least $sitesWithKnownLesionsLowerBound lesion sites which meets threshold of $minimumSitesWithLesions",
                        "Sufficient lesion sites"
                    )
                }

                sitesWithKnownAndSuspectedLesionsLowerBound >= minimumSitesWithLesions -> {
                    EvaluationFactory.warn(
                        "Patient has at least $sitesWithKnownAndSuspectedLesionsLowerBound lesion sites (when including " +
                                "$distinctCategorizedSuspectedLesionLocations suspected lesions) which meets threshold of $minimumSitesWithLesions",
                        "Sufficient lesions sites (when including suspected lesions)"
                    )
                }

                sitesWithKnownLesionsUpperBound >= minimumSitesWithLesions -> {
                    EvaluationFactory.undetermined(
                        "Patient has between $sitesWithKnownLesionsLowerBound and $sitesWithKnownLesionsUpperBound confirmed lesion sites so" +
                                " it is unclear if the threshold of $minimumSitesWithLesions is met.",
                        "Undetermined if sufficient lesion sites (near threshold of $minimumSitesWithLesions)"
                    )
                }

                sitesWithKnownAndSuspectedLesionsUpperBound >= minimumSitesWithLesions -> {
                    EvaluationFactory.undetermined(
                        "Patient has between $sitesWithKnownLesionsLowerBound and $sitesWithKnownAndSuspectedLesionsUpperBound lesion sites " +
                                "(including suspected lesions) so it is unclear if the threshold of $minimumSitesWithLesions is met",
                        "Undetermined if sufficient lesion sites (near threshold of $minimumSitesWithLesions and including suspected lesions)"
                    )
                }

                else -> {
                    EvaluationFactory.fail(
                        String.format(
                            "Patient has at most %d lesion sites, which is less than the threshold of %d",
                            sitesWithKnownAndSuspectedLesionsUpperBound,
                            minimumSitesWithLesions
                        ),
                        "Insufficient number of lesion sites"
                    )
                }
            }
        }
    }
}