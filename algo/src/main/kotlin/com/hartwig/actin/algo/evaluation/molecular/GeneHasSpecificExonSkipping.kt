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

class GeneHasSpecificExonSkipping(override val gene: String, private val exonToSkip: Int, maxTestAge: LocalDate? = null) :
    MolecularEvaluationFunction(
        targetCoveragePredicate = or(
            MolecularTestTarget.MUTATION,
            MolecularTestTarget.FUSION,
            messagePrefix = "Skipped exon $exonToSkip in"
        ),
        maxTestAge = maxTestAge
    ) {

    override fun evaluate(molecularHistory: MolecularHistory): Evaluation {
        val exonSkippingFusions = molecularHistory.molecularTests.flatMap(::findExonSkippingFusions).toSet()
        val exonSplicingVariants = molecularHistory.molecularTests.flatMap { findExonSplicingVariants(it, true) }.toSet()
        val potentialExonSplicingVariants = molecularHistory.molecularTests.flatMap { findExonSplicingVariants(it, false) }.toSet()

        return when {
            exonSkippingFusions.isNotEmpty() && exonSplicingVariants.isEmpty() -> {
                EvaluationFactory.pass(
                    "$gene exon $exonToSkip skipping detected: ${concat(exonSkippingFusions)}",
                    inclusionEvents = exonSkippingFusions
                )
            }

            exonSkippingFusions.isNotEmpty() -> {
                EvaluationFactory.warn(
                    "$gene exon $exonToSkip skipping detected: ${concat(exonSkippingFusions)} " +
                            "together with additional potentially exon $exonToSkip skipping variant(s) (${concat(exonSplicingVariants)}",
                    inclusionEvents = exonSkippingFusions + exonSplicingVariants
                )
            }

            exonSplicingVariants.isNotEmpty() -> {
                EvaluationFactory.warn(
                    "Potential $gene exon $exonToSkip skipping detected: ${concat(exonSplicingVariants)}",
                    inclusionEvents = exonSplicingVariants
                )
            }

            potentialExonSplicingVariants.isNotEmpty() -> {
                EvaluationFactory.warn(
                    "Potential $gene exon $exonToSkip skipping: variant(s) ${concat(potentialExonSplicingVariants)} detected in " +
                            "splice region of exon $exonToSkip although unknown relevance (not annotated with splice coding effect)",
                    inclusionEvents = potentialExonSplicingVariants
                )
            }

            else -> {
                EvaluationFactory.fail("No $gene exon $exonToSkip skipping")
            }
        }
    }

    private fun findExonSplicingVariants(molecular: MolecularTest, certainSplice: Boolean) =
        molecular.drivers.variants.filter { variant ->
            variant.isReportable && variant.gene == gene && variant.canonicalImpact.affectedExon != null
                    && variant.canonicalImpact.affectedExon == exonToSkip
                    && if (certainSplice) {
                variant.canonicalImpact.codingEffect == CodingEffect.SPLICE
            } else {
                variant.canonicalImpact.isSpliceRegion == true
            }
        }
            .map(Variant::event)
            .toSet()

    private fun findExonSkippingFusions(molecular: MolecularTest) = molecular.drivers.fusions.filter { fusion ->
        fusion.isReportable && fusion.geneStart == gene && fusion.geneEnd == gene && fusion.fusedExonUp == exonToSkip - 1
                && fusion.fusedExonDown == exonToSkip + 1
    }
        .map(Fusion::event)
        .toSet()
}