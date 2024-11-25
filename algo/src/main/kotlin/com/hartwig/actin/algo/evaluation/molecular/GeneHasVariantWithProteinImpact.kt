package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.util.Format.concat
import com.hartwig.actin.algo.evaluation.util.Format.percentage
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.molecular.MolecularTest
import com.hartwig.actin.datamodel.molecular.TranscriptImpact
import com.hartwig.actin.datamodel.molecular.Variant
import com.hartwig.actin.molecular.interpretation.MolecularInputChecker
import java.time.LocalDate
import org.apache.logging.log4j.LogManager

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

    override fun genes() = listOf(gene)

    override fun evaluate(test: MolecularTest): Evaluation {

        val variantsForGene = test.drivers.variants.filter { it.gene == gene }

        val canonicalImpactClassifications = variantsForGene
            .map { VariantAndProteinImpact(it, toProteinImpact(it.canonicalImpact.hgvsProteinImpact)) }
            .filter { it.proteinImpact in allowedProteinImpacts }
            .groupBy { (variant, _) ->
                if (variant.isReportable) {
                    if (variant.extendedVariantDetails?.clonalLikelihood?.let { it < CLONAL_CUTOFF } == true) {
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
                    "Variant(s) $impactString in gene $gene detected in canonical transcript",
                    "$impactString detected in $gene",
                    inclusionEvents = canonicalReportableImpactMatches.map { it.variant.event }.toSet()
                )
            }
            ?: evaluatePotentialWarns(
                canonicalImpactClassifications[VariantClassification.CANONICAL_REPORTABLE_SUBCLONAL],
                canonicalImpactClassifications[VariantClassification.CANONICAL_UNREPORTABLE],
                reportableOtherProteinImpactMatches
            )
            ?: EvaluationFactory.fail(
                "None of ${concat(allowedProteinImpacts)} detected in gene $gene", "${concat(allowedProteinImpacts)} not detected in $gene"
            )
    }

    private fun evaluatePotentialWarns(
        canonicalReportableSubclonalMatches: Collection<VariantAndProteinImpact>?,
        canonicalUnreportableMatches: Collection<VariantAndProteinImpact>?,
        reportableOtherProteinMatches: Collection<VariantAndProteinImpact>?
    ): Evaluation? {
        val subclonalWarning = eventsWithMessagesForVariantsAndImpacts(
            canonicalReportableSubclonalMatches,
            { "Variant(s) $it in $gene detected in canonical transcript but subclonal likelihood of > " + percentage(1 - CLONAL_CUTOFF) },
            { "Variant(s) $it in $gene but subclonal likelihood of > " + percentage(1 - CLONAL_CUTOFF) },
            useEventsInMessage = true
        )
        val unreportableWarning = eventsWithMessagesForVariantsAndImpacts(
            canonicalUnreportableMatches,
            { "Variant(s) $it in $gene detected in canonical transcript but are not reportable" },
            { "$it found in $gene but not reportable" },
            useEventsInMessage = false
        )
        val reportableOtherWarning = eventsWithMessagesForVariantsAndImpacts(
            reportableOtherProteinMatches,
            { "Variant(s) $it in $gene detected but in non-canonical transcript" },
            { "$it found in non-canonical transcript of gene $gene" },
            useEventsInMessage = false
        )
        return MolecularEventUtil.evaluatePotentialWarnsForEventGroups(
            listOfNotNull(subclonalWarning, unreportableWarning, reportableOtherWarning)
        )
    }

    private fun eventsWithMessagesForVariantsAndImpacts(
        variantsAndImpacts: Collection<VariantAndProteinImpact>?,
        makeSpecificMessage: (String) -> String,
        makeGeneralMessage: (String) -> String,
        useEventsInMessage: Boolean
    ): EventsWithMessages? = variantsAndImpacts?.let { matches ->
        val events = matches.map { it.variant.event }
        val listAsString = concat(if (useEventsInMessage) events else matches.map { it.proteinImpact })
        EventsWithMessages(events, makeSpecificMessage(listAsString), makeGeneralMessage(listAsString))
    }

    private fun toProteinImpacts(impacts: Set<TranscriptImpact>): Set<String> {
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