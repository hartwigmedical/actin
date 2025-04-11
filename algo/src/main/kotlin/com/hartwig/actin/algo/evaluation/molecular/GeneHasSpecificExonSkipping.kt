package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.util.Format.concat
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.molecular.MolecularHistory
import com.hartwig.actin.datamodel.molecular.MolecularTest
import com.hartwig.actin.datamodel.molecular.MolecularTestTarget
import com.hartwig.actin.datamodel.molecular.driver.CodingEffect
import com.hartwig.actin.datamodel.molecular.driver.Fusion
import com.hartwig.actin.datamodel.molecular.driver.Variant
import java.time.LocalDate

class GeneHasSpecificExonSkipping(private val gene: String, private val exonToSkip: Int, maxTestAge: LocalDate? = null) :
    MolecularEvaluationFunction(maxTestAge) {

    override fun genes() = listOf(gene)
    override fun targets() = listOf(MolecularTestTarget.FUSION)

    override fun evaluate(molecularHistory: MolecularHistory): Evaluation {
        val fusionSkippingEvents = molecularHistory.molecularTests.flatMap(::findFusionSkippingEvents).toSet()
        val exonSplicingVariants = molecularHistory.molecularTests.flatMap(::findExonSplicingVariants).toSet()

        return when {
            fusionSkippingEvents.isNotEmpty() && exonSplicingVariants.isEmpty() -> {
                EvaluationFactory.pass(
                    "Exon $exonToSkip skipping in $gene due to ${concat(fusionSkippingEvents)}",
                    inclusionEvents = fusionSkippingEvents
                )
            }

            fusionSkippingEvents.isNotEmpty() -> {
                EvaluationFactory.warn(
                    "Exon $exonToSkip skipping in $gene (${concat(fusionSkippingEvents)}) " +
                            "together with potentially exon skipping variant(s) (${concat(exonSplicingVariants)}",
                    inclusionEvents = fusionSkippingEvents + exonSplicingVariants
                )
            }

            exonSplicingVariants.isNotEmpty() -> {
                EvaluationFactory.warn(
                    "Potential $gene exon $exonToSkip skipping due to splice variant ${concat(exonSplicingVariants)}",
                    inclusionEvents = exonSplicingVariants
                )
            }

            else -> {
                EvaluationFactory.fail("No $gene exon $exonToSkip skipping")
            }
        }
    }

    private fun findExonSplicingVariants(molecular: MolecularTest) = molecular.drivers.variants.filter { variant ->
        val isCanonicalExonAffected = variant.canonicalImpact.affectedExon != null && variant.canonicalImpact.affectedExon == exonToSkip
        variant.isReportable && variant.gene == gene && isCanonicalExonAffected &&
                (variant.canonicalImpact.codingEffect == CodingEffect.SPLICE || variant.canonicalImpact.isSpliceRegion == true)
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