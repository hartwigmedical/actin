package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.util.Format.concatVariants
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.molecular.MolecularTest
import com.hartwig.actin.datamodel.molecular.MolecularTestTarget
import com.hartwig.actin.datamodel.molecular.driver.CodingContext
import com.hartwig.actin.datamodel.molecular.driver.Disruption
import com.hartwig.actin.datamodel.molecular.driver.RegionType
import com.hartwig.actin.datamodel.molecular.driver.VariantEffect

class GeneHasUTR3Loss(override val gene: String): MolecularEvaluationFunction(
    targetCoveragePredicate = specific(MolecularTestTarget.MUTATION, "3' UTR loss in")
) {

    override fun evaluate(test: MolecularTest): Evaluation {
        val (cavsIn3UTR, cavsIn3UTRUnreportable, vusIn3UTR) = test.drivers.variants.filter { variant ->
            variant.gene == gene && variant.canonicalImpact.effects.contains(VariantEffect.THREE_PRIME_UTR)
        }
            .fold(Triple(emptySet<String>(), emptySet<String>(), emptySet<String>())) { acc, variant ->
                if (variant.isCancerAssociatedVariant && variant.isReportable) {
                    acc.copy(first = acc.first + variant.event)
                } else if (variant.isCancerAssociatedVariant) {
                    acc.copy(second = acc.second + variant.event)
                } else {
                    acc.copy(third = acc.third + variant.event)
                }
            }

        val disruptionsIn3UTR = test.drivers.disruptions.filter { disruption ->
            disruption.gene == gene && disruption.codingContext == CodingContext.UTR_3P && disruption.regionType == RegionType.EXONIC
        }
            .map(Disruption::event)
            .toSet()

        if (cavsIn3UTR.isNotEmpty()) {
            return EvaluationFactory.pass(
                "3' UTR cancer-associated variant(s) ${concatVariants(cavsIn3UTR, gene)} in " + gene + " should lead to 3' UTR loss",
                inclusionEvents = cavsIn3UTR
            )
        }
        val potentialWarnEvaluation = evaluatePotentialWarns(cavsIn3UTRUnreportable, vusIn3UTR, disruptionsIn3UTR)
        return potentialWarnEvaluation ?: EvaluationFactory.fail("No 3' UTR loss of $gene")
    }

    private fun evaluatePotentialWarns(
        vusIn3UTR: Set<String>, cavsIn3UTRUnreportable: Set<String>, disruptionsIn3UTR: Set<String>
    ): Evaluation? {
        return MolecularEventUtil.evaluatePotentialWarnsForEventGroups(
            listOf(
                EventsWithMessages(
                    cavsIn3UTRUnreportable,
                    "Cancer-associated variant(s) ${concatVariants(cavsIn3UTRUnreportable, gene)} in 3' UTR region of $gene which may " +
                            "lead to 3' UTR loss but mutation is not considered reportable"
                ),
                EventsWithMessages(
                    vusIn3UTR,
                    "VUS mutation(s) ${concatVariants(vusIn3UTR, gene)} in 3' UTR region of $gene which may lead to 3' UTR loss"
                ),
                EventsWithMessages(
                    disruptionsIn3UTR,
                    "Disruption(s) ${concatVariants(disruptionsIn3UTR, gene)} in 3' UTR region of $gene which may lead to 3' UTR loss"
                )
            )
        )
    }
}