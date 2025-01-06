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
                    EvaluationFactory.pass("Has at least $minimumSitesWithLesions lesion sites")
                }

                sitesWithKnownAndSuspectedLesionsLowerBound >= minimumSitesWithLesions -> {
                    EvaluationFactory.warn("Has at least $minimumSitesWithLesions lesion sites (when including suspected lesions)")
                }

                sitesWithKnownLesionsUpperBound >= minimumSitesWithLesions -> {
                    EvaluationFactory.undetermined("Undetermined if sufficient lesion sites (near threshold of $minimumSitesWithLesions)")
                }

                sitesWithKnownAndSuspectedLesionsUpperBound >= minimumSitesWithLesions -> {
                    EvaluationFactory.undetermined("Undetermined if sufficient lesion sites (near threshold of $minimumSitesWithLesions and including suspected lesions)")
                }

                else -> {
                    EvaluationFactory.fail("Insufficient number of lesion sites (less than $minimumSitesWithLesions)")
                }
            }
        }
    }
}