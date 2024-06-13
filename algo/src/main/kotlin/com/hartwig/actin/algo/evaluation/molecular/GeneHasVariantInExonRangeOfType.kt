package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.molecular.datamodel.MolecularHistory
import com.hartwig.actin.molecular.datamodel.MolecularRecord
import com.hartwig.actin.molecular.datamodel.VariantType
import com.hartwig.actin.molecular.datamodel.orange.driver.ExtendedVariant
import com.hartwig.actin.molecular.datamodel.panel.PanelRecord
import com.hartwig.actin.molecular.datamodel.panel.generic.GenericExonDeletionExtraction
import com.hartwig.actin.trial.input.datamodel.VariantTypeInput

class GeneHasVariantInExonRangeOfType(
    private val gene: String, private val minExon: Int, private val maxExon: Int,
    private val requiredVariantType: VariantTypeInput?
) : MolecularEvaluationFunction {

    override fun evaluate(molecularHistory: MolecularHistory): Evaluation {

        val orangeEvaluation = molecularHistory.latestOrangeMolecularRecord()?.let { evaluateOrange(it) }
        val panelEvaluations = molecularHistory.allPanels().map { evaluatePanel(it) }

        return MolecularEvaluation.combine(
            listOfNotNull(orangeEvaluation) + panelEvaluations.filterNotNull(),
            EvaluationFactory.undetermined("Gene $gene not tested in molecular data", "Gene $gene not tested")
        )
    }

    private fun evaluateOrange(molecular: MolecularRecord): MolecularEvaluation {

        val exonRangeMessage = generateExonRangeMessage(minExon, maxExon)
        val variantTypeMessage = generateRequiredVariantTypeMessage(requiredVariantType)
        val baseMessage = "in exon $exonRangeMessage in gene $gene$variantTypeMessage detected"
        val allowedVariantTypes = determineAllowedVariantTypes(requiredVariantType)

        val (canonicalReportableVariantMatches, canonicalUnreportableVariantMatches, reportableOtherVariantMatches) =
            molecular.drivers.variants.filter { it.gene == gene && allowedVariantTypes.contains(it.type) }
                .map { variant ->
                    val (reportableMatches, unreportableMatches) = listOf(variant)
                        .filter { hasEffectInExonRange(variant.canonicalImpact.affectedExon, minExon, maxExon) }
                        .partition(ExtendedVariant::isReportable)

                    val otherImpactMatches = if (!variant.isReportable) emptySet() else {
                        setOfNotNull(variant.otherImpacts.find { hasEffectInExonRange(it.affectedExon, minExon, maxExon) }
                            ?.let { variant.event })
                    }
                    Triple(
                        reportableMatches.map(ExtendedVariant::event).toSet(),
                        unreportableMatches.map(ExtendedVariant::event).toSet(),
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

        val evaluation = if (canonicalReportableVariantMatches.isNotEmpty()) {
            EvaluationFactory.pass(
                "Variant(s) $baseMessage in canonical transcript",
                "Variant(s) $baseMessage",
                inclusionEvents = canonicalReportableVariantMatches
            )
        } else {
            val potentialWarnEvaluation =
                evaluatePotentialWarns(canonicalUnreportableVariantMatches, reportableOtherVariantMatches, baseMessage)
            potentialWarnEvaluation
                ?: EvaluationFactory.fail("No variant $baseMessage in canonical transcript", "No variant $baseMessage")
        }
        return MolecularEvaluation(molecular, evaluation)
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

    private fun evaluatePanel(panelRecord: PanelRecord): MolecularEvaluation? {
        val matches = if (requiredVariantType == null || requiredVariantType == VariantTypeInput.DELETE) {
            panelRecord.genericPanelExtraction?.exonDeletions
                ?.filter { exonDeletion -> exonDeletion.impactsGene(gene) }
                ?.filter { exonDeletion -> hasEffectInExonRange(exonDeletion.affectedExon, minExon, maxExon) }
                ?.map(GenericExonDeletionExtraction::display)
                ?.toSet() ?: emptySet()
        } else {
            emptySet()
        }

        val exonRangeMessage = generateExonRangeMessage(minExon, maxExon)
        val variantTypeMessage = generateRequiredVariantTypeMessage(requiredVariantType)
        val baseMessage = "in exon $exonRangeMessage in gene $gene$variantTypeMessage detected in Panel(s)"

        val evaluation = if (matches.isNotEmpty()) {
            val message = "Variant(s) $baseMessage"
            EvaluationFactory.pass(message, message, inclusionEvents = matches)
        } else {
            val geneIsTestedInPanel = panelRecord.testsGene(gene)

            val anyVariantOnGeneInPanel = panelRecord.drivers.variants.any { it.impactsGene(gene) }

            if (anyVariantOnGeneInPanel) {
                EvaluationFactory.undetermined(
                    "Variant in gene $gene detected in Panel, but unable to determine exon impact",
                    "Unclassified variant detected in gene $gene in Panel"
                )
            } else if (geneIsTestedInPanel) {
                val message = "No variant $baseMessage"
                EvaluationFactory.fail(message, message)
            } else {
                null
            }
        }
        return evaluation?.let { MolecularEvaluation(panelRecord, it) }
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