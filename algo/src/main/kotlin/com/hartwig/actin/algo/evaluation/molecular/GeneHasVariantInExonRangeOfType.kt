package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.molecular.datamodel.MolecularHistory
import com.hartwig.actin.molecular.datamodel.MolecularRecord
import com.hartwig.actin.molecular.datamodel.driver.Variant
import com.hartwig.actin.molecular.datamodel.driver.VariantType
import com.hartwig.actin.trial.input.datamodel.VariantTypeInput

class GeneHasVariantInExonRangeOfType(
    private val gene: String, private val minExon: Int, private val maxExon: Int,
    private val requiredVariantType: VariantTypeInput?
) : MolecularEvaluationFunction {

    override fun evaluate(molecularHistory: MolecularHistory): Evaluation {
        val orangeEvaluations = evaluateOrange(molecularHistory.latestOrangeMolecularRecord()!!)
        val panelEvaluations = evaluatePanel(molecularHistory)

        // write a helper to merge the evaluations, and use it here and elsewhere if we are
        // sticking with that pattern
        // also, shouldn't we merge the evals when they are the same type, e.g.
        // if orange gives something and panel another thing (for some reason), should we
        // merge or just return the orange result?
        return orangeEvaluations ?: panelEvaluations ?: EvaluationFactory.fail(
            "No variant in exon $minExon - $maxExon in gene $gene detected",
            "No variant in exon $minExon - $maxExon in gene $gene detected"
        )
    }

    private fun evaluateOrange(molecular: MolecularRecord): Evaluation {

        val exonRangeMessage = generateExonRangeMessage(minExon, maxExon)
        val variantTypeMessage = generateRequiredVariantTypeMessage(requiredVariantType)
        val baseMessage = "in exon $exonRangeMessage in gene $gene$variantTypeMessage detected"
        val allowedVariantTypes = determineAllowedVariantTypes(requiredVariantType)

        val (canonicalReportableVariantMatches, canonicalUnreportableVariantMatches, reportableOtherVariantMatches) =
            molecular.drivers.variants.filter { it.gene == gene && allowedVariantTypes.contains(it.type) }
                .map { variant ->
                    val (reportableMatches, unreportableMatches) = listOf(variant)
                        .filter { hasEffectInExonRange(variant.canonicalImpact.affectedExon, minExon, maxExon) }
                        .partition(Variant::isReportable)

                    val otherImpactMatches = if (!variant.isReportable) emptySet() else {
                        setOfNotNull(variant.otherImpacts.find { hasEffectInExonRange(it.affectedExon, minExon, maxExon) }
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

        if (canonicalReportableVariantMatches.isNotEmpty()) {
            return EvaluationFactory.pass(
                "Variant(s) $baseMessage in canonical transcript",
                "Variant(s) $baseMessage",
                inclusionEvents = canonicalReportableVariantMatches
            )
        }
        val potentialWarnEvaluation =
            evaluatePotentialWarns(canonicalUnreportableVariantMatches, reportableOtherVariantMatches, baseMessage)
        return potentialWarnEvaluation
            ?: EvaluationFactory.fail("No variant $baseMessage in canonical transcript", "No variant $baseMessage")
    }

    private fun evaluatePotentialWarns(
        canonicalUnreportableVariantMatches: Set<String>,
        reportableOtherVariantMatches: Set<String>,
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
                )
            )
        )
    }

    private fun evaluatePanel(molecularHistory: MolecularHistory): Evaluation? {

        val matches = molecularHistory.allGenericPanels().flatMap { panel ->
            panel.exonDeletions
                .filter { variant -> variant.gene == gene }
                .filter { variant ->
                    hasEffectInExonRange(variant.affectedExon, minExon, maxExon) && VariantType.DELETE in determineAllowedVariantTypes(requiredVariantType)
                }
                .map { variant -> variant.event() }
        }.toSet()

        // and what about archer and generic variants?

        if (matches.isNotEmpty()) {
            return EvaluationFactory.pass(
                "Variant in exon $minExon - $maxExon in gene $gene detected",
                "Variant in exon $minExon - $maxExon in gene $gene detected",
                inclusionEvents = matches
            )
        } else {
            // should we return a fail if variant is tested, and undetermined (insufficient data) if not tested?
            return null
        }

    }

    companion object {
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
}