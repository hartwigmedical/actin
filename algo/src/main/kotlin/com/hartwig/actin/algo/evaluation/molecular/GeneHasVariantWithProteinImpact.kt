package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.util.Format.concat
import com.hartwig.actin.algo.evaluation.util.Format.percentage
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.molecular.MolecularTest
import com.hartwig.actin.datamodel.molecular.MolecularTestTarget
import com.hartwig.actin.datamodel.molecular.driver.TranscriptVariantImpact
import com.hartwig.actin.datamodel.molecular.driver.Variant
import com.hartwig.actin.molecular.interpretation.MolecularInputChecker
import org.apache.logging.log4j.LogManager
import java.time.LocalDate

private const val CLONAL_CUTOFF = 0.5

private enum class VariantClassification {
    CANONICAL_REPORTABLE,
    CANONICAL_REPORTABLE_SUBCLONAL,
    CANONICAL_UNREPORTABLE,
}

private data class VariantAndProteinImpact(val variant: Variant, val proteinImpact: String)

class GeneHasVariantWithProteinImpact(
    private val gene: String,
    private val allowedProteinImpacts: Set<String>,
    maxTestAge: LocalDate? = null
) : MolecularEvaluationFunction(maxTestAge) {

    private val logger = LogManager.getLogger(GeneHasVariantWithProteinImpact::class.java)

    override fun gene() = gene
    override fun targetCoveragePredicate() = atLeast(MolecularTestTarget.MUTATION)

    override fun evaluate(test: MolecularTest): Evaluation {

        val variantsForGene = test.drivers.variants.filter { it.gene == gene }

        val canonicalImpactClassifications = variantsForGene
            .map { VariantAndProteinImpact(it, toProteinImpact(it.canonicalImpact.hgvsProteinImpact)) }
            .filter { it.proteinImpact in allowedProteinImpacts }
            .groupBy { variantAndImpact ->
                if (variantAndImpact.variant.isReportable) {
                    if (variantAndImpact.variant.extendedVariantDetails?.clonalLikelihood?.let { it < CLONAL_CUTOFF } == true) {
                        VariantClassification.CANONICAL_REPORTABLE_SUBCLONAL
                    } else {
                        VariantClassification.CANONICAL_REPORTABLE
                    }
                } else {
                    VariantClassification.CANONICAL_UNREPORTABLE
                }
            }

        val reportableOtherProteinImpactMatches = variantsForGene.filter(Variant::isReportable).flatMap { variant ->
            toProteinImpacts(variant.otherImpacts).filter(allowedProteinImpacts::contains).map { VariantAndProteinImpact(variant, it) }
        }

        return canonicalImpactClassifications[VariantClassification.CANONICAL_REPORTABLE]
            ?.let { canonicalReportableImpactMatches ->
                val impactString = concat(canonicalReportableImpactMatches.map { it.proteinImpact })
                EvaluationFactory.pass(
                    "$impactString in $gene in canonical transcript",
                    inclusionEvents = canonicalReportableImpactMatches.map { it.variant.event }.toSet()
                )
            }
            ?: evaluatePotentialWarns(
                canonicalImpactClassifications[VariantClassification.CANONICAL_REPORTABLE_SUBCLONAL],
                canonicalImpactClassifications[VariantClassification.CANONICAL_UNREPORTABLE],
                reportableOtherProteinImpactMatches
            )
            ?: EvaluationFactory.fail("${concat(allowedProteinImpacts)} not detected in $gene")
    }

    private fun evaluatePotentialWarns(
        canonicalReportableSubclonalMatches: Collection<VariantAndProteinImpact>?,
        canonicalUnreportableMatches: Collection<VariantAndProteinImpact>?,
        reportableOtherProteinMatches: Collection<VariantAndProteinImpact>?
    ): Evaluation? {
        val subclonalWarning = eventsWithMessagesForVariantsAndImpacts(
            canonicalReportableSubclonalMatches,
            { "Variant(s) $it in $gene but subclonal likelihood of > " + percentage(1 - CLONAL_CUTOFF) })
        val unreportableWarning =
            eventsWithMessagesForVariantsAndImpacts(canonicalUnreportableMatches, { "$it detected in $gene but not reportable" })
        val reportableOtherWarning = eventsWithMessagesForVariantsAndImpacts(
            reportableOtherProteinMatches,
            { "$it detected in non-canonical transcript of $gene" })
        return MolecularEventUtil.evaluatePotentialWarnsForEventGroups(
            listOfNotNull(subclonalWarning, unreportableWarning, reportableOtherWarning)
        )
    }

    private fun eventsWithMessagesForVariantsAndImpacts(
        variantsAndImpacts: Collection<VariantAndProteinImpact>?,
        makeMessage: (String) -> String
    ): EventsWithMessages? = variantsAndImpacts?.takeIf { it.isNotEmpty() }?.let { matches ->
        val listAsString = concat(matches.map { it.proteinImpact })
        EventsWithMessages(matches.map { it.variant.event }, makeMessage(listAsString))
    }

    private fun toProteinImpacts(impacts: Set<TranscriptVariantImpact>): Set<String> {
        return impacts.map { toProteinImpact(it.hgvsProteinImpact) }.toSet()
    }

    private fun toProteinImpact(hgvsProteinImpact: String): String {
        val impact = if (hgvsProteinImpact.startsWith("p.")) hgvsProteinImpact.substring(2) else hgvsProteinImpact
        if (impact.isEmpty()) {
            return impact
        }
        if (!MolecularInputChecker.isProteinImpact(impact)) {
            logger.warn("Cannot convert hgvs protein impact to a usable protein impact: {}", hgvsProteinImpact)
        }
        return impact
    }
}