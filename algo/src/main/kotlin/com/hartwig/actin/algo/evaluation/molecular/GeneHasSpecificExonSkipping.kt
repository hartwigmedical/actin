package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.util.Format.concat
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.molecular.CodingEffect
import com.hartwig.actin.datamodel.molecular.Fusion
import com.hartwig.actin.datamodel.molecular.MolecularHistory
import com.hartwig.actin.datamodel.molecular.MolecularTest
import com.hartwig.actin.datamodel.molecular.Variant
import java.time.LocalDate

class GeneHasSpecificExonSkipping(private val gene: String, private val exonToSkip: Int, maxTestAge: LocalDate? = null) :
    MolecularEvaluationFunction(maxTestAge, false) {

    override fun genes() = listOf(gene)

    override fun evaluate(molecularHistory: MolecularHistory): Evaluation {
        val fusionSkippingEvents = molecularHistory.molecularTests.flatMap(::findFusionSkippingEvents).toSet()
        val exonSplicingVariants = molecularHistory.molecularTests.flatMap(::findExonSplicingVariants).toSet()

        return when {
            fusionSkippingEvents.isNotEmpty() -> {
                EvaluationFactory.pass(
                    "Exon $exonToSkip skipped in gene $gene due to ${concat(fusionSkippingEvents)}",
                    "Exon $exonToSkip skipping in $gene",
                    inclusionEvents = fusionSkippingEvents
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

    private fun findExonSplicingVariants(molecular: MolecularTest) = molecular.drivers.variants.filter { variant ->
        val isCanonicalExonAffected = variant.canonicalImpact.affectedExon != null && variant.canonicalImpact.affectedExon == exonToSkip
        variant.isReportable && variant.gene == gene && isCanonicalExonAffected && (variant.canonicalImpact.codingEffect == CodingEffect.SPLICE || variant.canonicalImpact.isSpliceRegion == true)
    }
        .map(Variant::event)
        .toSet()

    private fun findFusionSkippingEvents(molecular: MolecularTest) = molecular.drivers.fusions.filter { fusion ->
        fusion.isReportable && fusion.geneStart == gene && fusion.geneEnd == gene && fusion.fusedExonUp == exonToSkip - 1
                && fusion.fusedExonDown == exonToSkip + 1
    }
        .map(Fusion::event)
        .toSet()
}