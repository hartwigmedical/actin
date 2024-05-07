package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.util.Format.concat
import com.hartwig.actin.molecular.datamodel.MolecularRecord
import com.hartwig.actin.molecular.datamodel.driver.CodingEffect
import com.hartwig.actin.molecular.datamodel.driver.Fusion
import com.hartwig.actin.molecular.datamodel.driver.Variant

class GeneHasSpecificExonSkipping(private val gene: String, private val exonToSkip: Int) : MolecularEvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {

        val archerExonSkippingEvents = record.molecularHistory.allArcherPanels().flatMap { it.skippedExons }
            .filter { it.impactsGene(gene) && exonToSkip == it.start && exonToSkip == it.end }.map { it.display() }

        val molecular = record.molecularHistory.latestOrangeMolecularRecord()
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
        val isCanonicalSplice =
            variant.canonicalImpact.codingEffect == CodingEffect.SPLICE || variant.canonicalImpact.isSpliceRegion
        val canonicalExonAffected = variant.canonicalImpact.affectedExon
        val isCanonicalExonAffected = canonicalExonAffected != null && canonicalExonAffected == exonToSkip
        variant.isReportable && variant.gene == gene && isCanonicalExonAffected && isCanonicalSplice
    }
        .map(Variant::event)
        .toSet()

    private fun findFusionSkippingEvents(molecular: MolecularRecord) = molecular.drivers.fusions.filter { fusion ->
        fusion.isReportable && fusion.geneStart == gene && fusion.geneEnd == gene && fusion.fusedExonUp == exonToSkip - 1
                && fusion.fusedExonDown == exonToSkip + 1
    }
        .map(Fusion::event)
        .toSet()
}