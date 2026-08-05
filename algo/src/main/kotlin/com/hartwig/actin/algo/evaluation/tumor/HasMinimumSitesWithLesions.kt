package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.EvaluationLabels
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation

class HasMinimumSitesWithLesions(private val minimumSitesWithLesions: Int, private val labels: EvaluationLabels.Tumor) :
    EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        with(record.tumor) {
            val distinctCategorizedLesionLocations = confirmedCategoricalLesionList().count { it == true }

            val otherLesionCount = (otherLesions ?: emptyList())
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
                    EvaluationFactory.pass(labels.hasMinimumSitesWithLesionsPass(minimumSitesWithLesions))
                }

                sitesWithKnownAndSuspectedLesionsLowerBound >= minimumSitesWithLesions -> {
                    EvaluationFactory.warn(labels.hasMinimumSitesWithLesionsWarn(minimumSitesWithLesions))
                }

                sitesWithKnownLesionsUpperBound >= minimumSitesWithLesions -> {
                    EvaluationFactory.undetermined(labels.hasMinimumSitesWithLesionsUndetermined(minimumSitesWithLesions))
                }

                sitesWithKnownAndSuspectedLesionsUpperBound >= minimumSitesWithLesions -> {
                    EvaluationFactory.undetermined(labels.hasMinimumSitesWithLesionsUndeterminedSuspected(minimumSitesWithLesions))
                }

                else -> {
                    EvaluationFactory.fail(labels.hasMinimumSitesWithLesionsFail(minimumSitesWithLesions))
                }
            }
        }
    }
}