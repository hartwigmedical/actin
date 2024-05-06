package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.util.Format
import com.hartwig.actin.molecular.datamodel.MolecularHistory
import com.hartwig.actin.molecular.datamodel.MolecularRecord
import com.hartwig.actin.molecular.datamodel.driver.CodingEffect
import com.hartwig.actin.molecular.datamodel.driver.DriverLikelihood
import com.hartwig.actin.molecular.datamodel.driver.GeneRole
import com.hartwig.actin.molecular.datamodel.driver.ProteinEffect
import com.hartwig.actin.molecular.datamodel.driver.Variant

class GeneHasActivatingMutation internal constructor(private val gene: String, private val codonsToIgnore: List<String>?) :
    EvaluationFunction {
    override fun evaluate(record: PatientRecord): Evaluation {

        if (!record.molecularHistory.hasMolecularData()) {
            return EvaluationFactory.undetermined("No molecular data", "No molecular data")
        }

        val orangeMolecular = record.molecularHistory.latestOrangeMolecularRecord()
        val orangeMolecularEvaluation = if (orangeMolecular != null) {
            findActivatingMutationsInOrangeMolecular(orangeMolecular)
        } else null

        val panelEvaluation = if (codonsToIgnore.isNullOrEmpty()) findActivatingMutationsInPanels(record.molecularHistory) else null

        val groupedEvaluationsByResult = listOfNotNull(orangeMolecularEvaluation, panelEvaluation)
            .groupBy { evaluation -> evaluation.result }
            .mapValues { entry ->
                entry.value.reduce { acc, y -> acc.addMessagesAndEvents(y) }
            }

        return groupedEvaluationsByResult[EvaluationResult.PASS]
            ?: groupedEvaluationsByResult[EvaluationResult.WARN]
            ?: groupedEvaluationsByResult[EvaluationResult.FAIL]
            ?: EvaluationFactory.undetermined("Gene $gene not tested in molecular data", "Gene $gene not tested")
    }

    private fun findActivatingMutationsInOrangeMolecular(molecular: MolecularRecord): Evaluation {
        val activatingVariants: MutableSet<String> = mutableSetOf()
        val activatingVariantsAssociatedWithResistance: MutableSet<String> = mutableSetOf()
        val activatingVariantsNoHotspotAndNoGainOfFunction: MutableSet<String> = mutableSetOf()
        val activatingVariantsInNonOncogene: MutableSet<String> = mutableSetOf()
        val activatingSubclonalVariants: MutableSet<String> = mutableSetOf()
        val nonHighDriverGainOfFunctionVariants: MutableSet<String> = mutableSetOf()
        val nonHighDriverSubclonalVariants: MutableSet<String> = mutableSetOf()
        val nonHighDriverVariants: MutableSet<String> = mutableSetOf()
        val otherMissenseOrHotspotVariants: MutableSet<String> = mutableSetOf()
        val hasHighMutationalLoad = molecular.characteristics.hasHighTumorMutationalLoad
        val evidenceSource = molecular.evidenceSource

        for (variant in molecular.drivers.variants) {
            if (variant.gene == gene && (codonsToIgnore == null || codonsToIgnore.none {
                    isCodonMatch(
                        variant.canonicalImpact.affectedCodon,
                        it
                    )
                })) {
                val isGainOfFunction =
                    (variant.proteinEffect == ProteinEffect.GAIN_OF_FUNCTION ||
                            variant.proteinEffect == ProteinEffect.GAIN_OF_FUNCTION_PREDICTED)
                val isNoOncogene = variant.geneRole == GeneRole.TSG
                if (variant.isReportable) {
                    if (variant.driverLikelihood == DriverLikelihood.HIGH) {
                        if (isAssociatedWithDrugResistance(variant)) {
                            activatingVariantsAssociatedWithResistance.add(variant.event)
                        } else if (!variant.isHotspot && !isGainOfFunction) {
                            activatingVariantsNoHotspotAndNoGainOfFunction.add(variant.event)
                        } else if (isNoOncogene) {
                            activatingVariantsInNonOncogene.add(variant.event)
                        } else if (variant.clonalLikelihood < CLONAL_CUTOFF) {
                            activatingSubclonalVariants.add(variant.event)
                        } else {
                            activatingVariants.add(variant.event)
                        }
                    } else {
                        if (isGainOfFunction) {
                            nonHighDriverGainOfFunctionVariants.add(variant.event)
                        } else if (hasHighMutationalLoad == null || !hasHighMutationalLoad) {
                            if (variant.clonalLikelihood < CLONAL_CUTOFF) {
                                nonHighDriverSubclonalVariants.add(variant.event)
                            } else {
                                nonHighDriverVariants.add(variant.event)
                            }
                        }
                    }
                } else if (isMissenseOrHotspot(variant)) {
                    otherMissenseOrHotspotVariants.add(variant.event)
                }
            }
        }

        if (activatingVariants.isNotEmpty()) {
            return EvaluationFactory.pass(
                "Activating mutation(s) detected in gene + $gene: ${Format.concat(activatingVariants)}",
                "$gene activating mutation(s)",
                inclusionEvents = activatingVariants
            )
        }

        val potentialWarnEvaluation = evaluatePotentialWarns(
            activatingVariantsAssociatedWithResistance,
            activatingVariantsInNonOncogene,
            activatingVariantsNoHotspotAndNoGainOfFunction,
            activatingSubclonalVariants,
            nonHighDriverGainOfFunctionVariants,
            nonHighDriverSubclonalVariants,
            nonHighDriverVariants,
            otherMissenseOrHotspotVariants,
            evidenceSource
        )

        return potentialWarnEvaluation ?: EvaluationFactory.fail(
            "No activating mutation(s) detected in gene $gene", "No $gene activating mutation(s)"
        )
    }

    private fun evaluatePotentialWarns(
        activatingVariantsAssociatedWithResistance: Set<String>,
        activatingVariantsInNonOncogene: Set<String>,
        activatingVariantsNoHotspotAndNoGainOfFunction: Set<String>,
        activatingSubclonalVariants: Set<String>,
        nonHighDriverGainOfFunctionVariants: Set<String>,
        nonHighDriverSubclonalVariants: Set<String>,
        nonHighDriverVariants: Set<String>,
        otherMissenseOrHotspotVariants: Set<String>,
        evidenceSource: String
    ): Evaluation? {
        return MolecularEventUtil.evaluatePotentialWarnsForEventGroups(
            listOf(
                EventsWithMessages(
                    activatingVariantsAssociatedWithResistance,
                    "Gene $gene should have activating mutation(s): ${Format.concat(activatingVariantsAssociatedWithResistance)}, "
                            + "however, these are (also) associated with drug resistance in $evidenceSource",
                    "$gene activating mutation(s) but are associated with drug resistance in $evidenceSource"
                ),
                EventsWithMessages(
                    activatingVariantsInNonOncogene,
                    "Gene $gene has activating mutation(s) ${Format.concat(activatingVariantsInNonOncogene)} but gene known as TSG in $evidenceSource",
                    "$gene activating mutation(s) but gene known as TSG in $evidenceSource"
                ),
                EventsWithMessages(
                    activatingVariantsNoHotspotAndNoGainOfFunction,
                    "Gene $gene has potentially activating mutation(s) " + Format.concat(activatingVariantsNoHotspotAndNoGainOfFunction)
                            + " that have high driver likelihood,"
                            + " but is not a hotspot and not associated with gain-of-function protein effect evidence in $evidenceSource",
                    "$gene potentially activating mutation(s) with high driver likelihood but not a hotspot"
                            + " and not associated with gain-of-function protein effect evidence in $evidenceSource"
                ),
                EventsWithMessages(
                    activatingSubclonalVariants,
                    "Gene $gene potentially activating mutation(s) " + Format.concat(activatingSubclonalVariants) +
                            " but have subclonal likelihood of > " + Format.percentage(1 - CLONAL_CUTOFF),
                    gene + " potentially activating mutation(s) " + Format.concat(activatingSubclonalVariants) +
                            " but subclonal likelihood > " + Format.percentage(1 - CLONAL_CUTOFF)
                ),
                EventsWithMessages(
                    nonHighDriverGainOfFunctionVariants,
                    "Gene " + gene + " has potentially activating mutation(s) " + Format.concat(nonHighDriverGainOfFunctionVariants) +
                            " that do not have high driver likelihood prediction, but annotated with having gain-of-function protein effect evidence in $evidenceSource",
                    "$gene potentially activating mutation(s) having gain-of-function protein effect evidence in $evidenceSource but without high driver prediction"
                ),
                EventsWithMessages(
                    nonHighDriverSubclonalVariants,
                    "Gene $gene has potentially activating mutation(s) " + Format.concat(activatingSubclonalVariants) +
                            " have subclonal likelihood of > ${Format.percentage(1 - CLONAL_CUTOFF)} and no high driver likelihood",
                    "$gene potentially activating mutation(s) without high driver likelihood and subclonal likelihood > " + Format.percentage(
                        1 - CLONAL_CUTOFF
                    )
                ),
                EventsWithMessages(
                    nonHighDriverVariants,
                    "Gene $gene has potentially activating mutation(s) " + Format.concat(nonHighDriverVariants) +
                            " but no high driver likelihood",
                    "$gene potentially activating mutation(s) but no high driver likelihood"
                ),
                EventsWithMessages(
                    otherMissenseOrHotspotVariants,
                    "Gene $gene has potentially activating mutation(s) " + Format.concat(otherMissenseOrHotspotVariants) +
                            " that are missense or have hotspot status, but are not considered reportable",
                    "$gene potentially activating mutation(s) but mutation(s) not reportable"
                )
            )
        )
    }

    private fun findActivatingMutationsInPanels(molecularHistory: MolecularHistory): Evaluation? {

        val activatingVariants: MutableSet<String> = mutableSetOf()

        for (panel in molecularHistory.allArcherPanels()) {
            for (variant in panel.variants) {
                if (gene == variant.gene) {
                    activatingVariants.add(variant.hgvsCodingImpact)
                }
            }
        }

        for (panel in molecularHistory.allGenericPanels()) {
            for (variant in panel.variants) {
                if (gene == variant.gene) {
                    TODO("replace with event() that can describe SNVs, exon deletions, etc")
//                    activatingVariants.add(variant.hgvsCodingImpact)
                }
            }
        }

        if (activatingVariants.isNotEmpty())
            return EvaluationFactory.pass(
                "Activating mutation(s) detected in gene + $gene: ${Format.concat(activatingVariants)} in Panel(s)",
                "$gene activating mutation(s)",
                inclusionEvents = activatingVariants
            )

        return if (molecularHistory.allArcherPanels().any { it.testedGenes().contains(gene) })
            EvaluationFactory.fail("No activating mutation(s) detected in gene $gene", "No $gene activating mutation(s)")
        else null
    }

    companion object {
        private const val CLONAL_CUTOFF = 0.5

        private fun isAssociatedWithDrugResistance(variant: Variant): Boolean {
            val isAssociatedWithDrugResistance = variant.isAssociatedWithDrugResistance
            return isAssociatedWithDrugResistance != null && isAssociatedWithDrugResistance
        }

        private fun isMissenseOrHotspot(variant: Variant): Boolean {
            return variant.canonicalImpact.codingEffect == CodingEffect.MISSENSE || variant.isHotspot
        }

        private fun isCodonMatch(affectedCodon: Int?, codonsToMatch: String): Boolean {
            if (affectedCodon == null) {
                return false
            }
            val codonIndexToMatch = codonsToMatch.substring(1).takeWhile { it.isDigit() }.toInt()
            return codonIndexToMatch == affectedCodon
        }
    }
}