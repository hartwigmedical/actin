package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.util.Format.concat
import com.hartwig.actin.molecular.datamodel.MolecularTest
import com.hartwig.actin.molecular.datamodel.VariantEffect
import com.hartwig.actin.molecular.datamodel.orange.driver.CodingContext
import com.hartwig.actin.molecular.datamodel.orange.driver.Disruption
import com.hartwig.actin.molecular.datamodel.orange.driver.RegionType

class GeneHasUTR3Loss(private val gene: String) : MolecularEvaluationFunction {

    override fun evaluate(test: MolecularTest): Evaluation {

        val (hotspotsIn3UTR, hotspotsIn3UTRUnreportable, vusIn3UTR) = test.drivers.variants.filter { variant ->
            variant.gene == gene && variant.canonicalImpact.effects.contains(VariantEffect.THREE_PRIME_UTR)
        }
            .fold(Triple(emptySet<String>(), emptySet<String>(), emptySet<String>())) { acc, variant ->
                if (variant.isHotspot && variant.isReportable) {
                    acc.copy(first = acc.first + variant.event)
                } else if (variant.isHotspot) {
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

        if (hotspotsIn3UTR.isNotEmpty()) {
            return EvaluationFactory.pass(
                "3' UTR hotspot mutation(s) in " + gene + " should lead to 3' UTR loss: " + concat(hotspotsIn3UTR),
                "Present 3' UTR loss of $gene",
                inclusionEvents = hotspotsIn3UTR
            )
        }
        val potentialWarnEvaluation = evaluatePotentialWarns(hotspotsIn3UTRUnreportable, vusIn3UTR, disruptionsIn3UTR)
        return potentialWarnEvaluation ?: EvaluationFactory.fail(
            "No variants detected in 3' UTR region of $gene",
            "No 3' UTR loss of $gene"
        )
    }

    private fun evaluatePotentialWarns(
        vusIn3UTR: Set<String>, hotspotsIn3UTRUnreportable: Set<String>, disruptionsIn3UTR: Set<String>
    ): Evaluation? {
        return MolecularEventUtil.evaluatePotentialWarnsForEventGroups(
            listOf(
                EventsWithMessages(
                    hotspotsIn3UTRUnreportable,
                    "Hotspot mutation(s) detected in 3' UTR region of $gene which may lead to 3' UTR loss: "
                            + "${concat(hotspotsIn3UTRUnreportable)} but mutation is not considered reportable",
                    "Hotspot mutation(s) in 3' UTR region of $gene may lead to 3' UTR loss but mutation is not reportable"
                ),
                EventsWithMessages(
                    vusIn3UTR,
                    "VUS mutation(s) detected in 3' UTR region of $gene which may lead to 3' UTR loss: ${concat(vusIn3UTR)}",
                    "VUS mutation(s) in 3' UTR region of $gene may lead to 3' UTR loss"
                ),
                EventsWithMessages(
                    disruptionsIn3UTR,
                    "Disruption(s) detected in 3' UTR region of $gene which may lead to 3' UTR loss: ${concat(disruptionsIn3UTR)}",
                    "Disruption(s) in 3' UTR region of $gene may lead to 3' UTR loss"
                )
            )
        )
    }
}