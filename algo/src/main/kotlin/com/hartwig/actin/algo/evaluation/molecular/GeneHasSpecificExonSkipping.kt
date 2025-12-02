package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.util.Format.concat
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.molecular.MolecularTest
import com.hartwig.actin.datamodel.molecular.MolecularTestTarget
import com.hartwig.actin.datamodel.molecular.driver.CodingEffect
import com.hartwig.actin.datamodel.molecular.driver.Fusion
import com.hartwig.actin.datamodel.molecular.driver.Variant
import java.time.LocalDate

class GeneHasSpecificExonSkipping(override val gene: String, private val exonToSkip: Int, maxTestAge: LocalDate? = null) :
    MolecularEvaluationFunction(
        targetCoveragePredicate = or(
            MolecularTestTarget.MUTATION,
            MolecularTestTarget.FUSION,
            messagePrefix = "Skipped exon $exonToSkip in"
        ),
        maxTestAge = maxTestAge
    ) {

    override fun evaluate(test: MolecularTest): Evaluation {
        val exonSkippingFusionEvents = findExonSkippingFusions(test)
            .map(Fusion::event).toSet()
        val exonSplicingVariants = findExonSplicingVariants(test, true)
        val potentialExonSplicingVariants = findExonSplicingVariants(test, false)
        val exonSplicingVariantEvents = exonSplicingVariants.map(Variant::event).toSet()
        val potentialExonSplicingVariantEvents = potentialExonSplicingVariants.map(Variant::event).toSet()
        val confirmedExonSkippingEvents =
            (exonSplicingVariants + potentialExonSplicingVariants).filter { it.exonSkippingIsConfirmed == true }
                .map(Variant::event)
                .toSet()

        return when {
            exonSkippingFusionEvents.isNotEmpty() && exonSplicingVariants.isEmpty() && potentialExonSplicingVariantEvents.isEmpty() -> {
                EvaluationFactory.pass(
                    "$gene exon $exonToSkip skipping detected: ${concat(exonSkippingFusionEvents)}",
                    inclusionEvents = exonSkippingFusionEvents
                )
            }

            exonSkippingFusionEvents.isNotEmpty() -> {
                if (confirmedExonSkippingEvents.isNotEmpty()) {
                    EvaluationFactory.pass(
                        "$gene exon $exonToSkip skipping detected: ${concat(exonSkippingFusionEvents)} " +
                                "together with confirmed additional exon $exonToSkip skipping variant(s) (${concat(confirmedExonSkippingEvents)})",
                        inclusionEvents = exonSkippingFusionEvents + confirmedExonSkippingEvents
                    )
                } else {
                    EvaluationFactory.warn(
                        "$gene exon $exonToSkip skipping detected: ${concat(exonSkippingFusionEvents)} " +
                                "together with potential additional exon $exonToSkip skipping variant(s) (${concat(exonSplicingVariantEvents)})",
                        inclusionEvents = exonSkippingFusionEvents + exonSplicingVariantEvents
                    )
                }
            }

            confirmedExonSkippingEvents.isNotEmpty() -> {
                EvaluationFactory.pass(
                    "Confirmed $gene exon $exonToSkip skipping detected: ${concat(confirmedExonSkippingEvents)}",
                    inclusionEvents = confirmedExonSkippingEvents
                )
            }

            exonSplicingVariants.isNotEmpty() -> {
                EvaluationFactory.warn(
                    "Potential $gene exon $exonToSkip skipping detected: ${concat(exonSplicingVariantEvents)}",
                    inclusionEvents = exonSplicingVariantEvents
                )
            }

            potentialExonSplicingVariants.isNotEmpty() -> {
                EvaluationFactory.warn(
                    "Potential $gene exon $exonToSkip skipping: variant(s) ${concat(potentialExonSplicingVariantEvents)} detected in " +
                            "splice region of exon $exonToSkip although unknown relevance (not annotated with splice coding effect)",
                    inclusionEvents = potentialExonSplicingVariantEvents
                )
            }

            else -> {
                EvaluationFactory.fail("No $gene exon $exonToSkip skipping")
            }
        }
    }

    private fun findExonSkippingFusions(molecular: MolecularTest) = molecular.drivers.fusions.filter { fusion ->
        fusion.isReportable && fusion.geneStart == gene && fusion.geneEnd == gene && fusion.fusedExonUp == exonToSkip - 1
                && fusion.fusedExonDown == exonToSkip + 1
    }.toSet()

    private fun findExonSplicingVariants(molecular: MolecularTest, requireCertainty: Boolean) =
        molecular.drivers.variants.filter { variant ->
            variant.gene == gene && variant.canonicalImpact.affectedExon != null
                    && variant.canonicalImpact.affectedExon == exonToSkip
                    && isSplice(
                requireCertainty,
                variant.isReportable,
                variant.canonicalImpact.codingEffect,
                variant.canonicalImpact.inSpliceRegion
            )
        }.toSet()

    private fun isSplice(requireCertainty: Boolean, isReportable: Boolean, codingEffect: CodingEffect?, inSpliceRegion: Boolean?): Boolean {
        return inSpliceRegion == true && (!requireCertainty) || (isReportable && codingEffect == CodingEffect.SPLICE)
    }
}
