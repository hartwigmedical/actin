package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.util.Format
import com.hartwig.actin.molecular.datamodel.CodingEffect
import com.hartwig.actin.molecular.datamodel.DriverLikelihood
import com.hartwig.actin.molecular.datamodel.GeneRole
import com.hartwig.actin.molecular.datamodel.MolecularHistory
import com.hartwig.actin.molecular.datamodel.MolecularTest
import com.hartwig.actin.molecular.datamodel.ProteinEffect
import com.hartwig.actin.molecular.datamodel.Variant
import com.hartwig.actin.molecular.datamodel.panel.PanelEvent
import com.hartwig.actin.molecular.datamodel.panel.PanelRecord

enum class ActivationWarningType {
    ASSOCIATED_WITH_RESISTANCE,
    NON_ONCOGENE,
    NO_HOTSPOT_AND_NO_GAIN_OF_FUNCTION,
    SUBCLONAL,
    NON_HIGH_DRIVER_GAIN_OF_FUNCTION,
    NON_HIGH_DRIVER_SUBCLONAL,
    NON_HIGH_DRIVER,
    OTHER_MISSENSE_OR_HOTSPOT

}

data class ActivationProfile(
    val event: String,
    val activating: Boolean,
    val warningType: ActivationWarningType? = null
)

private const val CLONAL_CUTOFF = 0.5

class GeneHasActivatingMutation(private val gene: String, private val codonsToIgnore: List<String>?) : MolecularEvaluationFunction {
    override fun evaluate(molecularHistory: MolecularHistory): MolecularEvaluation {

        val orangeMolecular = molecularHistory.latestOrangeMolecularRecord()
        val orangeMolecularEvaluation = if (orangeMolecular != null) {
            findActivatingMutations(orangeMolecular)
        } else null

        val panelEvaluation = if (codonsToIgnore.isNullOrEmpty()) findActivatingMutationsInPanels(molecularHistory) else null

        return MolecularEvaluation(
            molecularRecordEvaluation = orangeMolecularEvaluation,
            panelEvaluations = listOfNotNull(panelEvaluation),
            EvaluationFactory.undetermined(
                "Gene $gene not tested in molecular data",
                "Gene $gene not tested"
            )
        )
    }

    private fun evaluateVariant(
        variant: Variant, hasHighMutationalLoad: Boolean?
    ): ActivationProfile {
        val isNoOncogene = variant.geneRole == GeneRole.TSG
        val isGainOfFunction =
            variant.proteinEffect == ProteinEffect.GAIN_OF_FUNCTION || variant.proteinEffect == ProteinEffect.GAIN_OF_FUNCTION_PREDICTED
        return if (variant.isReportable) {
            if (variant.driverLikelihood == DriverLikelihood.HIGH) {
                return when {
                    isAssociatedWithDrugResistance(variant) -> profile(variant.event, ActivationWarningType.ASSOCIATED_WITH_RESISTANCE)
                    !variant.isHotspot && !isGainOfFunction -> profile(
                        variant.event,
                        ActivationWarningType.NO_HOTSPOT_AND_NO_GAIN_OF_FUNCTION
                    )

                    isNoOncogene -> profile(variant.event, ActivationWarningType.NON_ONCOGENE)
                    isSubclonal(variant) -> profile(variant.event, ActivationWarningType.SUBCLONAL)
                    else -> profile(variant.event, activating = true)
                }
            } else {
                if (isGainOfFunction) {
                    profile(variant.event, ActivationWarningType.NON_HIGH_DRIVER_GAIN_OF_FUNCTION)
                } else if (hasHighMutationalLoad == null || !hasHighMutationalLoad) {
                    return if (isSubclonal(variant)) {
                        profile(variant.event, ActivationWarningType.NON_HIGH_DRIVER_SUBCLONAL)
                    } else {
                        profile(variant.event, ActivationWarningType.NON_HIGH_DRIVER)
                    }
                } else {
                    profile(variant.event)
                }
            }
        } else if (isMissenseOrHotspot(variant)) {
            return profile(variant.event, ActivationWarningType.OTHER_MISSENSE_OR_HOTSPOT)
        } else {
            return profile(variant.event)
        }
    }

    private fun profile(event: String, warningType: ActivationWarningType? = null, activating: Boolean = false) =
        ActivationProfile(event = event, activating = activating, warningType = warningType)

    private fun isSubclonal(variant: Variant) = variant.clonalLikelihood?.let { it < CLONAL_CUTOFF } == true

    private fun findActivatingMutations(molecular: MolecularTest<*>): Evaluation {
        val hasHighMutationalLoad = molecular.characteristics.hasHighTumorMutationalLoad
        val evidenceSource = molecular.evidenceSource
        val variantCharacteristics =
            molecular.drivers.variants.filter { it.gene == gene }
                .filter { ignoredCodon(codonsToIgnore, it) }
                .map { variant ->
                    evaluateVariant(variant, hasHighMutationalLoad)
                }

        val activatingVariants = variantCharacteristics.filter(ActivationProfile::activating).map(ActivationProfile::event).toSet()
        if (activatingVariants.isNotEmpty()) {
            return EvaluationFactory.pass(
                "Activating mutation(s) detected in gene + $gene: ${Format.concat(activatingVariants)}",
                "$gene activating mutation(s)",
                inclusionEvents = activatingVariants
            )
        }
        val warningsByType =
            variantCharacteristics.groupBy { it.warningType }.mapValues { entry -> entry.value.map(ActivationProfile::event).toSet() }
        val potentialWarnEvaluation = evaluatePotentialWarns(
            warningsByType[ActivationWarningType.ASSOCIATED_WITH_RESISTANCE] ?: emptySet(),
            warningsByType[ActivationWarningType.NON_ONCOGENE] ?: emptySet(),
            warningsByType[ActivationWarningType.NO_HOTSPOT_AND_NO_GAIN_OF_FUNCTION] ?: emptySet(),
            warningsByType[ActivationWarningType.SUBCLONAL] ?: emptySet(),
            warningsByType[ActivationWarningType.NON_HIGH_DRIVER_GAIN_OF_FUNCTION] ?: emptySet(),
            warningsByType[ActivationWarningType.NON_HIGH_DRIVER_SUBCLONAL] ?: emptySet(),
            warningsByType[ActivationWarningType.NON_HIGH_DRIVER] ?: emptySet(),
            warningsByType[ActivationWarningType.OTHER_MISSENSE_OR_HOTSPOT] ?: emptySet(),
            evidenceSource
        )

        return potentialWarnEvaluation ?: EvaluationFactory.fail(
            "No activating mutation(s) detected in gene $gene", "No $gene activating mutation(s)"
        )
    }

    private fun ignoredCodon(
        codonsToIgnore: List<String>?, variant: Variant
    ) = codonsToIgnore == null || codonsToIgnore.none {
        isCodonMatch(
            variant.canonicalImpact.affectedCodon, it
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
                    "Gene $gene should have activating mutation(s): ${Format.concat(activatingVariantsAssociatedWithResistance)}, " + "however, these are (also) associated with drug resistance in $evidenceSource",
                    "$gene activating mutation(s) but are associated with drug resistance in $evidenceSource"
                ),
                EventsWithMessages(
                    activatingVariantsInNonOncogene,
                    "Gene $gene has activating mutation(s) ${Format.concat(activatingVariantsInNonOncogene)} but gene known as TSG in $evidenceSource",
                    "$gene activating mutation(s) but gene known as TSG in $evidenceSource"
                ),
                EventsWithMessages(
                    activatingVariantsNoHotspotAndNoGainOfFunction,
                    "Gene $gene has potentially activating mutation(s) " + Format.concat(activatingVariantsNoHotspotAndNoGainOfFunction) + " that have high driver likelihood," + " but is not a hotspot and not associated with gain-of-function protein effect evidence in $evidenceSource",
                    "$gene potentially activating mutation(s) with high driver likelihood but not a hotspot" + " and not associated with gain-of-function protein effect evidence in $evidenceSource"
                ),
                EventsWithMessages(
                    activatingSubclonalVariants,
                    "Gene $gene potentially activating mutation(s) " + Format.concat(activatingSubclonalVariants) + " but have subclonal likelihood of > " + Format.percentage(
                        1 - CLONAL_CUTOFF
                    ),
                    gene + " potentially activating mutation(s) " + Format.concat(activatingSubclonalVariants) + " but subclonal likelihood > " + Format.percentage(
                        1 - CLONAL_CUTOFF
                    )
                ),
                EventsWithMessages(
                    nonHighDriverGainOfFunctionVariants,
                    "Gene " + gene + " has potentially activating mutation(s) " + Format.concat(nonHighDriverGainOfFunctionVariants) + " that do not have high driver likelihood prediction, but annotated with having gain-of-function protein effect evidence in $evidenceSource",
                    "$gene potentially activating mutation(s) having gain-of-function protein effect evidence in $evidenceSource but without high driver prediction"
                ),
                EventsWithMessages(
                    nonHighDriverSubclonalVariants,
                    "Gene $gene has potentially activating mutation(s) " + Format.concat(activatingSubclonalVariants) + " have subclonal likelihood of > ${
                        Format.percentage(1 - CLONAL_CUTOFF)
                    } and no high driver likelihood",
                    "$gene potentially activating mutation(s) without high driver likelihood and subclonal likelihood > " + Format.percentage(
                        1 - CLONAL_CUTOFF
                    )
                ),
                EventsWithMessages(
                    nonHighDriverVariants,
                    "Gene $gene has potentially activating mutation(s) " + Format.concat(nonHighDriverVariants) + " but no high driver likelihood",
                    "$gene potentially activating mutation(s) but no high driver likelihood"
                ),
                EventsWithMessages(
                    otherMissenseOrHotspotVariants,
                    "Gene $gene has potentially activating mutation(s) " + Format.concat(otherMissenseOrHotspotVariants) + " that are missense or have hotspot status, but are not considered reportable",
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

    private fun findActivatingMutationsInPanels(molecularHistory: MolecularHistory): Evaluation? {

        val activatingVariants =
            activatingVariants(molecularHistory.allPanels().flatMap(PanelRecord::events))

        if (activatingVariants.isNotEmpty()) return EvaluationFactory.pass(
            "Activating mutation(s) detected in gene + $gene: ${Format.concat(activatingVariants)} in Panel(s)",
            "$gene activating mutation(s)",
            inclusionEvents = activatingVariants
        )

        return if (molecularHistory.allPanels()
                .any { it.testedGenes().contains(gene) }
        ) EvaluationFactory.fail("No activating mutation(s) detected in gene $gene", "No $gene activating mutation(s)")
        else null
    }

    private fun activatingVariants(events: List<PanelEvent>) = events.filter { it.impactsGene(gene) }.map(PanelEvent::display).toSet()
}