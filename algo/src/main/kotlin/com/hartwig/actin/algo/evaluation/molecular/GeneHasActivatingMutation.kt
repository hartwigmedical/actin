package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.util.Format
import com.hartwig.actin.molecular.datamodel.CodingEffect
import com.hartwig.actin.molecular.datamodel.DriverLikelihood
import com.hartwig.actin.molecular.datamodel.GeneRole
import com.hartwig.actin.molecular.datamodel.MolecularHistory
import com.hartwig.actin.molecular.datamodel.MolecularTest
import com.hartwig.actin.molecular.datamodel.ProteinEffect
import com.hartwig.actin.molecular.datamodel.Variant

data class ActivatingCharacteristics(
    val event: String,
    val activating: Boolean,
    val associatedWithResistance: Boolean? = null,
    val noHotspotAndNoGainOfFunction: Boolean? = null,
    val nonOncoGene: Boolean? = null,
    val subclonal: Boolean? = null,
    val nonHighDriverGainOfFunction: Boolean? = null,
    val nonHighDriverSubclonal: Boolean? = null,
    val nonHighDriver: Boolean? = null,
    val otherMissenseOrHotspot: Boolean? = null
)

private const val CLONAL_CUTOFF = 0.5

class GeneHasActivatingMutation internal constructor(private val gene: String, private val codonsToIgnore: List<String>?) :
    MolecularEvaluationFunction {
    override fun evaluate(molecularHistory: MolecularHistory): Evaluation {

        val orangeMolecular = molecularHistory.latestOrangeMolecularRecord()
        val orangeMolecularEvaluation = if (orangeMolecular != null) {
            findActivatingMutations(orangeMolecular)
        } else null

        val panelEvaluations =
            if (codonsToIgnore.isNullOrEmpty()) molecularHistory.allPanels().map { findActivatingMutations(it) } else emptyList()

        val groupedEvaluationsByResult = (listOfNotNull(orangeMolecularEvaluation) + panelEvaluations)
            .groupBy { evaluation -> evaluation.result }
            .mapValues { entry ->
                entry.value.reduce { acc, y -> acc.addMessagesAndEvents(y) }
            }

        return groupedEvaluationsByResult[EvaluationResult.PASS]
            ?: groupedEvaluationsByResult[EvaluationResult.WARN]
            ?: groupedEvaluationsByResult[EvaluationResult.FAIL]
            ?: EvaluationFactory.undetermined("Gene $gene not tested in molecular data", "Gene $gene not tested")
    }

    private fun evaluateVariant(
        variant: Variant,
        hasHighMutationalLoad: Boolean?
    ): ActivatingCharacteristics {
        val isNoOncogene = variant.geneRole == GeneRole.TSG
        val isGainOfFunction =
            variant.proteinEffect == ProteinEffect.GAIN_OF_FUNCTION || variant.proteinEffect == ProteinEffect.GAIN_OF_FUNCTION_PREDICTED
        val characteristics = ActivatingCharacteristics(variant.event, false)
        return if (variant.isReportable) {
            if (variant.driverLikelihood == DriverLikelihood.HIGH) {
                if (isAssociatedWithDrugResistance(variant)) {
                    characteristics.copy(associatedWithResistance = true)
                } else if (!variant.isHotspot && !isGainOfFunction) {
                    characteristics.copy(noHotspotAndNoGainOfFunction = true)
                } else if (isNoOncogene) {
                    characteristics.copy(nonOncoGene = true)
                } else if (variant.clonalLikelihood < CLONAL_CUTOFF) {
                    characteristics.copy(subclonal = true)
                } else {
                    characteristics.copy(activating = true)
                }
            } else {
                if (isGainOfFunction) {
                    characteristics.copy(nonHighDriverGainOfFunction = true)
                } else if (hasHighMutationalLoad == null || !hasHighMutationalLoad) {
                    return if (variant.clonalLikelihood < CLONAL_CUTOFF) {
                        characteristics.copy(nonHighDriverSubclonal = true)
                    } else {
                        characteristics.copy(nonHighDriver = true)
                    }
                } else {
                    characteristics
                }
            }
        } else if (isMissenseOrHotspot(variant)) {
            return characteristics.copy(otherMissenseOrHotspot = true)
        } else {
            return characteristics
        }
    }

    private fun findActivatingMutations(molecular: MolecularTest<*>): Evaluation {
        val hasHighMutationalLoad = molecular.characteristics.hasHighTumorMutationalLoad
        val evidenceSource = molecular.evidenceSource
        val variantCharacteristics = molecular.drivers.variants
            .filter { it.gene == gene }
            .filter { ignoredCodon(codonsToIgnore, it) }
            .map { variant ->
                evaluateVariant(variant, hasHighMutationalLoad)
            }

        val activatingVariants =
            variantCharacteristics.filter(ActivatingCharacteristics::activating).map(ActivatingCharacteristics::event).toSet()
        if (activatingVariants.isNotEmpty()) {
            return EvaluationFactory.pass(
                "Activating mutation(s) detected in gene + $gene: ${Format.concat(activatingVariants)}",
                "$gene activating mutation(s)",
                inclusionEvents = activatingVariants
            )
        }

        val potentialWarnEvaluation = evaluatePotentialWarns(
            filteredForWarnings(variantCharacteristics) { it.associatedWithResistance },
            filteredForWarnings(variantCharacteristics) { it.nonOncoGene },
            filteredForWarnings(variantCharacteristics) { it.noHotspotAndNoGainOfFunction },
            filteredForWarnings(variantCharacteristics) { it.subclonal },
            filteredForWarnings(variantCharacteristics) { it.nonHighDriverGainOfFunction },
            filteredForWarnings(variantCharacteristics) { it.nonHighDriverSubclonal },
            filteredForWarnings(variantCharacteristics) { it.nonHighDriver },
            filteredForWarnings(variantCharacteristics) { it.otherMissenseOrHotspot },
            evidenceSource
        )

        return potentialWarnEvaluation ?: EvaluationFactory.fail(
            "No activating mutation(s) detected in gene $gene", "No $gene activating mutation(s)"
        )
    }

    private fun ignoredCodon(
        codonsToIgnore: List<String>?,
        variant: Variant
    ) = codonsToIgnore == null || codonsToIgnore.none {
        isCodonMatch(
            variant.canonicalImpact.affectedCodon,
            it
        )
    }

    private fun filteredForWarnings(
        variantCharacteristics: List<ActivatingCharacteristics>,
        extractor: (ActivatingCharacteristics) -> Boolean?
    ) =
        variantCharacteristics.filter { extractor.invoke(it) == true }.map(ActivatingCharacteristics::event).toSet()

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