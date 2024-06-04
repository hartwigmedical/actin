package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.util.Format.concat
import com.hartwig.actin.molecular.datamodel.CodingEffect
import com.hartwig.actin.molecular.datamodel.MolecularHistory
import com.hartwig.actin.molecular.datamodel.MolecularRecord
import com.hartwig.actin.molecular.datamodel.hmf.driver.ExhaustiveFusion
import com.hartwig.actin.molecular.datamodel.hmf.driver.ExhaustiveVariant
import com.hartwig.actin.molecular.datamodel.panel.archer.ArcherSkippedExonsExtraction

class GeneHasSpecificExonSkipping(private val gene: String, private val exonToSkip: Int) : MolecularEvaluationFunction {

    override fun evaluate(molecularHistory: MolecularHistory): Evaluation {

        val archerExonSkippingEvents = molecularHistory.allArcherPanels().flatMap { it.events() }.filterIsInstance<ArcherSkippedExonsExtraction>()
            .filter { it.impactsGene(gene) && exonToSkip == it.start && exonToSkip == it.end }.map { it.display() }

        val molecular = molecularHistory.latestOrangeMolecularRecord()
        val fusionSkippingEvents = molecular?.let(::findFusionSkippingEvents) ?: emptySet()
        val exonSplicingVariants = molecular?.let(::findExonSplicingVariants) ?: emptySet()

        return when {
            fusionSkippingEvents.isNotEmpty() || archerExonSkippingEvents.isNotEmpty() -> {
                EvaluationFactory.pass(
                    "Exon $exonToSkip skipped in gene $gene due to ${concat(fusionSkippingEvents)}",
                    "Exon $exonToSkip skipping in $gene",
                    inclusionEvents = fusionSkippingEvents + archerExonSkippingEvents
                )
            }

            exonSplicingVariants.isNotEmpty() -> {
                EvaluationFactory.warn(
                    "Exon $exonToSkip may be skipped in gene $gene due to ${concat(exonSplicingVariants)}",
                    "Potential $gene exon $exonToSkip skipping due to splice variant",
                    inclusionEvents = exonSplicingVariants
                )
            }

            else -> {
                EvaluationFactory.fail("No $gene exon $exonToSkip skipping", "No $gene exon $exonToSkip skipping")
            }
        }
    }

    private fun findExonSplicingVariants(molecular: MolecularRecord) = molecular.drivers.variants.filter { variant ->
        val isCanonicalExonAffected = variant.canonicalImpact.affectedExon != null && variant.canonicalImpact.affectedExon == exonToSkip
        variant.isReportable && variant.gene == gene && isCanonicalExonAffected && (variant.canonicalImpact.codingEffect == CodingEffect.SPLICE || variant.canonicalImpact.isSpliceRegion)
    }
        .map(ExhaustiveVariant::event)
        .toSet()

    private fun findFusionSkippingEvents(molecular: MolecularRecord) = molecular.drivers.fusions.filter { fusion ->
        fusion.isReportable && fusion.geneStart == gene && fusion.geneEnd == gene && fusion.fusedExonUp == exonToSkip - 1
                && fusion.fusedExonDown == exonToSkip + 1
    }
        .map(ExhaustiveFusion::event)
        .toSet()
}