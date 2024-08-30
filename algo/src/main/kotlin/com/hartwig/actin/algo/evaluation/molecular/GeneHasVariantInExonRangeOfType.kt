package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.molecular.Fusion
import com.hartwig.actin.datamodel.molecular.MolecularTest
import com.hartwig.actin.datamodel.molecular.Variant
import com.hartwig.actin.datamodel.molecular.VariantType
import com.hartwig.actin.trial.input.datamodel.VariantTypeInput

class GeneHasVariantInExonRangeOfType(
    private val gene: String, private val minExon: Int, private val maxExon: Int,
    private val requiredVariantType: VariantTypeInput?
) : MolecularEvaluationFunction {

    override fun genes() = listOf(gene)

    override fun evaluate(test: MolecularTest): Evaluation {

        val exonRangeMessage = generateExonRangeMessage(minExon, maxExon)
        val variantTypeMessage = generateRequiredVariantTypeMessage(requiredVariantType)
        val baseMessage = "in exon $exonRangeMessage in gene $gene$variantTypeMessage detected"
        val allowedVariantTypes = determineAllowedVariantTypes(requiredVariantType)

        val (canonicalReportableVariantMatches, canonicalUnreportableVariantMatches, reportableOtherVariantMatches) =
            test.drivers.variants.filter { it.gene == gene && allowedVariantTypes.contains(it.type) }
                .map { variant ->
                    val (reportableMatches, unreportableMatches) = listOf(variant)
                        .filter { hasEffectInExonRange(variant.canonicalImpact.affectedExon, minExon, maxExon) }
                        .partition(Variant::isReportable)

                    val otherImpactMatches = if (!variant.isReportable) emptySet() else {
                        setOfNotNull(variant.otherImpacts.find {
                            hasEffectInExonRange(
                                it.affectedExon,
                                minExon,
                                maxExon
                            )
                        }
                            ?.let { variant.event })
                    }
                    Triple(
                        reportableMatches.map(Variant::event).toSet(),
                        unreportableMatches.map(Variant::event).toSet(),
                        otherImpactMatches
                    )
                }.fold(
                    Triple(
                        emptySet<String>(),
                        emptySet<String>(),
                        emptySet<String>()
                    )
                ) { (allReportable, allUnreportable, allOther), (reportable, unreportable, other) ->
                    Triple(allReportable + reportable, allUnreportable + unreportable, allOther + other)
                }

        val (reportableExonSkips, unreportableExonSkips) =
            if (requiredVariantType == VariantTypeInput.DELETE || requiredVariantType == null)
                test.drivers.fusions
                    .filter { it.geneStart == gene && it.geneEnd == gene }
                    .filter {
                        exonsWithinRange(it)
                    }.partition { it.isReportable }
            else emptyList<Fusion>() to emptyList()

        return if (canonicalReportableVariantMatches.isNotEmpty()) {
            EvaluationFactory.pass(
                "Variant(s) $baseMessage in canonical transcript",
                "Variant(s) $baseMessage",
                inclusionEvents = canonicalReportableVariantMatches
            )
        } else if (reportableExonSkips.isNotEmpty()) {
            EvaluationFactory.pass(
                "Exon(s) skipped $baseMessage",
                "Exons skipped $baseMessage",
                inclusionEvents = reportableExonSkips.map { it.event }.toSet()
            )
        } else {
            val potentialWarnEvaluation =
                evaluatePotentialWarns(
                    canonicalUnreportableVariantMatches,
                    reportableOtherVariantMatches,
                    unreportableExonSkips.map { it.event }.toSet(),
                    baseMessage
                )
            potentialWarnEvaluation
                ?: EvaluationFactory.fail("No variant $baseMessage in canonical transcript", "No variant $baseMessage")
        }
    }

    private fun exonsWithinRange(it: Fusion) = it.extendedFusionDetails?.let { e ->
        val range = IntRange(minExon, maxExon)
        return range.contains(e.fusedExonUp) && range.contains(e.fusedExonDown)
    } ?: false

    private fun evaluatePotentialWarns(
        canonicalUnreportableVariantMatches: Set<String>,
        reportableOtherVariantMatches: Set<String>,
        unreportableFusions: Set<String>,
        baseMessage: String
    ): Evaluation? {
        return MolecularEventUtil.evaluatePotentialWarnsForEventGroups(
            listOf(
                EventsWithMessages(
                    canonicalUnreportableVariantMatches,
                    "Variant(s) $baseMessage in canonical transcript but considered not reportable",
                    "Variant(s) $baseMessage but not reportable"
                ),
                EventsWithMessages(
                    reportableOtherVariantMatches,
                    "Variant(s) $baseMessage but in non-canonical transcript",
                    "Variant(s) $baseMessage but in non-canonical transcript"
                ),
                EventsWithMessages(
                    unreportableFusions,
                    "Exon skip(s) $baseMessage but not reportable",
                    "Exon skip(s) $baseMessage but not reportable"
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
            "$minExon - $maxExon"
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
            VariantType.values().toSet()
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