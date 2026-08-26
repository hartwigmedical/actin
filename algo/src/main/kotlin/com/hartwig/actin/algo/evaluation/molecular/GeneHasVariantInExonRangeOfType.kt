package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.molecular.MolecularVariantUtil.variantTypesForInput
import com.hartwig.actin.algo.evaluation.util.Format.concat
import com.hartwig.actin.algo.evaluation.util.Format.percentage
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.molecular.MolecularTest
import com.hartwig.actin.datamodel.molecular.MolecularTestTarget
import com.hartwig.actin.datamodel.molecular.driver.DriverLikelihood
import com.hartwig.actin.datamodel.molecular.driver.Fusion
import com.hartwig.actin.datamodel.molecular.driver.Variant
import com.hartwig.actin.datamodel.molecular.driver.VariantType
import com.hartwig.actin.datamodel.trial.VariantTypeInput

class GeneHasVariantInExonRangeOfType(
    override val gene: String, private val minExon: Int, private val maxExon: Int, private val requiredVariantType: VariantTypeInput?
) : MolecularEvaluationFunction(
    targetCoveragePredicate = atLeast(
        MolecularTestTarget.MUTATION,
        messagePrefix = "Mutation in ${rangeText(minExon, maxExon)}${generateRequiredVariantTypeMessage(requiredVariantType)} in"
    )
) {

    private enum class VariantClassification {
        CANONICAL_REPORTABLE_HIGH_DRIVER,
        CANONICAL_REPORTABLE_SUBCLONAL,
        CANONICAL_REPORTABLE_NON_HIGH_DRIVER,
        CANONICAL_UNREPORTABLE,
        NON_CANONICAL_REPORTABLE,
        NONE
    }

    override fun evaluate(test: MolecularTest): Evaluation {
        val exonRangeMessage = generateExonRangeMessage(minExon, maxExon)
        val variantTypeMessage = generateRequiredVariantTypeMessage(requiredVariantType)
        val baseMessage = "in exon $exonRangeMessage in $gene$variantTypeMessage"
        val allowedVariantTypes =
            if (requiredVariantType == null) VariantType.entries.toSet() else variantTypesForInput(requiredVariantType)

        val variantClassifications =
            test.drivers.variants.filter { it.gene == gene && allowedVariantTypes.contains(it.type) }
                .groupBy { variant ->
                    val hasCanonicalEffectInExonRange = hasEffectInExonRange(variant.canonicalImpact.affectedExon, minExon, maxExon)
                    when {
                        hasCanonicalEffectInExonRange && variant.isReportable && variant.clonalLikelihood?.let { it < CLONAL_CUTOFF } == true -> {
                            VariantClassification.CANONICAL_REPORTABLE_SUBCLONAL
                        }

                        hasCanonicalEffectInExonRange && variant.isReportable && variant.driverLikelihood == DriverLikelihood.HIGH -> {
                            VariantClassification.CANONICAL_REPORTABLE_HIGH_DRIVER
                        }

                        hasCanonicalEffectInExonRange && variant.isReportable -> {
                            VariantClassification.CANONICAL_REPORTABLE_NON_HIGH_DRIVER
                        }

                        hasCanonicalEffectInExonRange -> {
                            VariantClassification.CANONICAL_UNREPORTABLE
                        }

                        variant.isReportable && variant.otherImpacts.any { hasEffectInExonRange(it.affectedExon, minExon, maxExon) } -> {
                            VariantClassification.NON_CANONICAL_REPORTABLE
                        }

                        else -> VariantClassification.NONE
                    }
                }.mapValues { (_, variants) -> variants.map(Variant::event).toSet() }
        val highDriverEvents = variantClassifications[VariantClassification.CANONICAL_REPORTABLE_HIGH_DRIVER]
        val reportableOtherVariantMatches = variantClassifications[VariantClassification.NON_CANONICAL_REPORTABLE]
        val subclonalVariantMatches = variantClassifications[VariantClassification.CANONICAL_REPORTABLE_SUBCLONAL]

        val (reportableExonSkips, unreportableExonSkips) =
            if (requiredVariantType == VariantTypeInput.DELETE || requiredVariantType == null)
                test.drivers.fusions.filter {
                    it.geneStart == gene && it.geneEnd == gene && exonsWithinRange(it)
                }.partition { it.isReportable }
            else emptyList<Fusion>() to emptyList()

        val (highDriverExonSkips, nonHighDriverExonSkips) = reportableExonSkips.partition { it.driverLikelihood == DriverLikelihood.HIGH }
        val highDriverExonSkipEvents = highDriverExonSkips.map { it.event }.toSet()

        return when {
            !highDriverEvents.isNullOrEmpty() && reportableOtherVariantMatches.isNullOrEmpty() && subclonalVariantMatches.isNullOrEmpty() -> {
                EvaluationFactory.pass(
                    "Variant(s) $baseMessage in canonical transcript",
                    inclusionEvents = highDriverEvents
                )
            }

            highDriverExonSkipEvents.isNotEmpty() && reportableOtherVariantMatches.isNullOrEmpty() && subclonalVariantMatches.isNullOrEmpty() -> {
                EvaluationFactory.pass("Exon(s) skipped $baseMessage", inclusionEvents = highDriverExonSkipEvents)
            }

            !highDriverEvents.isNullOrEmpty() -> {
                val extensions = buildWarnExtensions(reportableOtherVariantMatches, subclonalVariantMatches)
                EvaluationFactory.warn(
                    "Variant(s) ${concat(highDriverEvents)} $baseMessage in canonical transcript together with ${concat(extensions)}",
                    inclusionEvents = highDriverEvents + reportableOtherVariantMatches.orEmpty() + subclonalVariantMatches.orEmpty()
                )
            }

            highDriverExonSkipEvents.isNotEmpty() -> {
                val extensions = buildWarnExtensions(reportableOtherVariantMatches, subclonalVariantMatches)
                EvaluationFactory.warn(
                    "Exon(s) skipped $baseMessage due to ${concat(highDriverExonSkipEvents)} together with ${concat(extensions)}",
                    inclusionEvents = highDriverExonSkipEvents + reportableOtherVariantMatches.orEmpty() + subclonalVariantMatches.orEmpty()
                )
            }

            else -> {
                evaluatePotentialWarns(
                    variantClassifications[VariantClassification.CANONICAL_UNREPORTABLE],
                    reportableOtherVariantMatches,
                    unreportableExonSkips.map { it.event }.toSet(),
                    variantClassifications[VariantClassification.CANONICAL_REPORTABLE_NON_HIGH_DRIVER],
                    nonHighDriverExonSkips.map { it.event }.toSet(),
                    subclonalVariantMatches,
                    baseMessage
                )
                    ?: EvaluationFactory.fail("No variant $baseMessage in canonical transcript")
            }
        }
    }

    private fun exonsWithinRange(fusion: Fusion): Boolean {
        val range = IntRange(minExon, maxExon)
        return range.contains(fusion.fusedExonUp) && range.contains(fusion.fusedExonDown)
    }

    private fun evaluatePotentialWarns(
        canonicalUnreportableVariantMatches: Set<String>?,
        reportableOtherVariantMatches: Set<String>?,
        unreportableFusions: Set<String>?,
        nonHighDriverVariants: Set<String>?,
        nonHighDriverExonSkips: Set<String>?,
        subclonalVariantMatches: Set<String>?,
        baseMessage: String
    ): Evaluation? {
        return MolecularEventUtil.evaluatePotentialWarnsForEventGroups(
            listOf(
                EventsWithMessages(
                    canonicalUnreportableVariantMatches,
                    "Variant(s) $baseMessage in canonical transcript but considered not reportable"
                ),
                EventsWithMessages(reportableOtherVariantMatches, "Variant(s) $baseMessage but in non-canonical transcript"),
                EventsWithMessages(unreportableFusions, "Exon skip(s) $baseMessage but not reportable"),
                EventsWithMessages(nonHighDriverVariants, "Variant(s) $baseMessage in canonical transcript but not high driver"),
                EventsWithMessages(nonHighDriverExonSkips, "Exon skip(s) $baseMessage but not high driver"),
                EventsWithMessages(
                    subclonalVariantMatches,
                    "Variant(s) $baseMessage in canonical transcript but subclonal likelihood of > ${percentage(1 - CLONAL_CUTOFF)}"
                )
            )
        )
    }

    private fun hasEffectInExonRange(affectedExon: Int?, minExon: Int, maxExon: Int): Boolean {
        return affectedExon != null && affectedExon >= minExon && affectedExon <= maxExon
    }

    private fun generateExonRangeMessage(minExon: Int, maxExon: Int): String {
        return if (minExon == maxExon) {
            minExon.toString()
        } else {
            "$minExon-$maxExon"
        }
    }

    private fun buildWarnExtensions(
        nonCanonicalMatches: Set<String>?,
        subclonalMatches: Set<String>?
    ): List<String> = listOfNotNull(
        nonCanonicalMatches?.ifEmpty { null }?.let { "variant(s) in non-canonical transcript: ${concat(it)}" },
        subclonalMatches?.ifEmpty { null }?.let { "variant(s) in canonical transcript but subclonal likelihood of > ${percentage(1 - CLONAL_CUTOFF)}: ${concat(it)}" }
    )

    companion object {
        private const val CLONAL_CUTOFF = 0.5
    }
}

private fun rangeText(minExon: Int, maxExon: Int) = if (minExon != maxExon) "exon range $minExon to $maxExon" else "exon $minExon"

private fun generateRequiredVariantTypeMessage(requiredVariantType: VariantTypeInput?): String {
    return when (requiredVariantType) {
        null -> ""
        VariantTypeInput.SNV, VariantTypeInput.MNV, VariantTypeInput.INDEL -> {
            " of type $requiredVariantType"
        }

        VariantTypeInput.INSERT -> {
            " of type insertion"
        }

        VariantTypeInput.DELETE -> {
            " of type deletion"
        }
    }
}