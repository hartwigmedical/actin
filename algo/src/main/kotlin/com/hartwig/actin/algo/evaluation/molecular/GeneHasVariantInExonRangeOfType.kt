package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.util.Format.concat
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.molecular.MolecularTest
import com.hartwig.actin.datamodel.molecular.MolecularTestTarget
import com.hartwig.actin.datamodel.molecular.driver.DriverLikelihood
import com.hartwig.actin.datamodel.molecular.driver.Fusion
import com.hartwig.actin.datamodel.molecular.driver.Variant
import com.hartwig.actin.datamodel.molecular.driver.VariantType
import com.hartwig.actin.trial.input.datamodel.VariantTypeInput
import java.time.LocalDate

class GeneHasVariantInExonRangeOfType(
    private val gene: String, private val minExon: Int, private val maxExon: Int,
    private val requiredVariantType: VariantTypeInput?,
    maxTestAge: LocalDate? = null
) : MolecularEvaluationFunction(maxTestAge) {

    private enum class VariantClassification {
        CANONICAL_HIGH_DRIVER,
        CANONICAL_REPORTABLE_NON_HIGH_DRIVER,
        CANONICAL_UNREPORTABLE,
        REPORTABLE_OTHER,
        NONE
    }

    override fun gene() = gene
    override fun targetCoveragePredicate() = atLeast(MolecularTestTarget.MUTATION)

    override fun evaluate(test: MolecularTest): Evaluation {
        val exonRangeMessage = generateExonRangeMessage(minExon, maxExon)
        val variantTypeMessage = generateRequiredVariantTypeMessage(requiredVariantType)
        val baseMessage = "in exon $exonRangeMessage in $gene$variantTypeMessage"
        val allowedVariantTypes = determineAllowedVariantTypes(requiredVariantType)

        val variantClassifications =
            test.drivers.variants.filter { it.gene == gene && allowedVariantTypes.contains(it.type) }
                .groupBy { variant ->
                    val hasCanonicalEffectInExonRange = hasEffectInExonRange(variant.canonicalImpact.affectedExon, minExon, maxExon)
                    when {
                        hasCanonicalEffectInExonRange && variant.isReportable && variant.driverLikelihood == DriverLikelihood.HIGH -> {
                            VariantClassification.CANONICAL_HIGH_DRIVER
                        }

                        hasCanonicalEffectInExonRange && variant.isReportable -> {
                            VariantClassification.CANONICAL_REPORTABLE_NON_HIGH_DRIVER
                        }

                        hasCanonicalEffectInExonRange -> {
                            VariantClassification.CANONICAL_UNREPORTABLE
                        }

                        variant.isReportable && variant.otherImpacts.any { hasEffectInExonRange(it.affectedExon, minExon, maxExon) } -> {
                            VariantClassification.REPORTABLE_OTHER
                        }

                        else -> VariantClassification.NONE
                    }
                }.mapValues { (_, variants) -> variants.map(Variant::event).toSet() }
        val highDriverEvents = variantClassifications[VariantClassification.CANONICAL_HIGH_DRIVER]
        val reportableOtherVariantMatches = variantClassifications[VariantClassification.REPORTABLE_OTHER]

        val (reportableExonSkips, unreportableExonSkips) =
            if (requiredVariantType == VariantTypeInput.DELETE || requiredVariantType == null)
                test.drivers.fusions.filter {
                    it.geneStart == gene && it.geneEnd == gene && exonsWithinRange(it)
                }.partition { it.isReportable }
            else emptyList<Fusion>() to emptyList()

        val (highDriverExonSkips, nonHighDriverExonSkips) = reportableExonSkips.partition { it.driverLikelihood == DriverLikelihood.HIGH }
        val highDriverExonSkipEvents = highDriverExonSkips.map { it.event }.toSet()

        return when {
            !highDriverEvents.isNullOrEmpty() && reportableOtherVariantMatches.isNullOrEmpty() -> {
                EvaluationFactory.pass(
                    "Variant(s) $baseMessage in canonical transcript",
                    inclusionEvents = highDriverEvents
                )
            }

            highDriverExonSkipEvents.isNotEmpty() && reportableOtherVariantMatches.isNullOrEmpty() -> {
                EvaluationFactory.pass("Exon(s) skipped $baseMessage", inclusionEvents = highDriverExonSkipEvents)
            }

            !highDriverEvents.isNullOrEmpty() -> {
                EvaluationFactory.warn(
                    "Variant(s) ${concat(highDriverEvents)} $baseMessage in canonical transcript together with " +
                            "variant(s) in non-canonical transcript: ${concat(reportableOtherVariantMatches!!)}",
                    inclusionEvents = highDriverEvents + reportableOtherVariantMatches
                )
            }

            highDriverExonSkipEvents.isNotEmpty() -> {
                EvaluationFactory.warn(
                    "Exon(s) skipped $baseMessage due to ${concat(highDriverExonSkipEvents)} together with variant(s) in " +
                            "non-canonical transcript: ${concat(reportableOtherVariantMatches!!)}",
                    inclusionEvents = highDriverExonSkipEvents + reportableOtherVariantMatches
                )
            }

            else -> {
                evaluatePotentialWarns(
                    variantClassifications[VariantClassification.CANONICAL_UNREPORTABLE],
                    reportableOtherVariantMatches,
                    unreportableExonSkips.map { it.event }.toSet(),
                    variantClassifications[VariantClassification.CANONICAL_REPORTABLE_NON_HIGH_DRIVER],
                    nonHighDriverExonSkips.map { it.event }.toSet(),
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
                EventsWithMessages(nonHighDriverExonSkips, "Exon skip(s) $baseMessage but not high driver")
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

    private fun generateRequiredVariantTypeMessage(requiredVariantType: VariantTypeInput?): String {
        return if (requiredVariantType == null) {
            ""
        } else when (requiredVariantType) {
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

    private fun determineAllowedVariantTypes(requiredVariantType: VariantTypeInput?): Set<VariantType> {
        return if (requiredVariantType == null) {
            VariantType.entries.toSet()
        } else when (requiredVariantType) {
            VariantTypeInput.SNV -> {
                setOf(VariantType.SNV)
            }

            VariantTypeInput.MNV -> {
                setOf(VariantType.MNV)
            }

            VariantTypeInput.INSERT -> {
                setOf(VariantType.INSERT)
            }

            VariantTypeInput.DELETE -> {
                setOf(VariantType.DELETE)
            }

            VariantTypeInput.INDEL -> {
                setOf(VariantType.INSERT, VariantType.DELETE)
            }

            else -> {
                throw IllegalStateException("Could not map required variant type: $requiredVariantType")
            }
        }
    }
}