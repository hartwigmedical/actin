package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.util.Format
import com.hartwig.actin.algo.evaluation.util.Format.concat
import com.hartwig.actin.algo.evaluation.util.Format.concatVariants
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.molecular.MolecularTest
import com.hartwig.actin.datamodel.molecular.driver.CodingEffect
import com.hartwig.actin.datamodel.molecular.driver.DriverLikelihood
import com.hartwig.actin.datamodel.molecular.driver.GeneRole
import com.hartwig.actin.datamodel.molecular.driver.ProteinEffect
import com.hartwig.actin.datamodel.molecular.driver.Variant
import java.time.LocalDate

enum class ActivationWarningType(val description: String? = null) {
    ASSOCIATED_WITH_RESISTANCE(
        "Potentially activating mutation(s) that have high driver likelihood " +
                "but are also associated with drug resistance"
    ),
    NON_ONCOGENE,
    NO_HOTSPOT_AND_NO_GAIN_OF_FUNCTION(
        "Potentially activating mutation(s) that have high driver likelihood " +
                "but is not a hotspot and not associated with gain-of-function"
    ),
    SUBCLONAL(
        "Potentially activating mutation(s) that have high driver likelihood " +
                "but also have subclonal likelihood of > ${Format.percentage(1 - CLONAL_CUTOFF)}"
    ),
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

class GeneHasActivatingMutation(
    private val gene: String,
    private val codonsToIgnore: List<String>?,
    maxTestAge: LocalDate? = null
) : MolecularEvaluationFunction(maxTestAge) {

    override fun genes() = listOf(gene)

    override fun evaluate(test: MolecularTest): Evaluation {
        val hasHighMutationalLoad = test.characteristics.hasHighTumorMutationalLoad
        val evidenceSource = test.evidenceSource
        val variantCharacteristics =
            test.drivers.variants.filter { it.gene == gene }
                .filter { ignoredCodon(codonsToIgnore, it) }
                .map { variant ->
                    evaluateVariant(variant, hasHighMutationalLoad)
                }

        val activatingVariants = variantCharacteristics.filter(ActivationProfile::activating).map(ActivationProfile::event).toSet()
        val eventsByWarningType =
            variantCharacteristics.groupBy { it.warningType }.mapValues { entry -> entry.value.map(ActivationProfile::event).toSet() }

        val potentiallyActivatingWarnings = listOf(
            ActivationWarningType.ASSOCIATED_WITH_RESISTANCE,
            ActivationWarningType.NO_HOTSPOT_AND_NO_GAIN_OF_FUNCTION,
            ActivationWarningType.SUBCLONAL,
        ).flatMap { warningType -> eventsByWarningType[warningType]?.map { event -> event to warningType } ?: emptyList() }

        val variantsString = concatVariants(activatingVariants, gene)
        return when {
            activatingVariants.isNotEmpty() && potentiallyActivatingWarnings.isEmpty() -> {
                EvaluationFactory.pass(
                    "$gene activating mutation(s): $variantsString",
                    inclusionEvents = activatingVariants
                )
            }

            activatingVariants.isNotEmpty() -> {
                EvaluationFactory.warn(
                    "$gene activating mutation(s): $variantsString " +
                            "together with potentially activating mutation(s) " +
                            concat(potentiallyActivatingWarnings.map { (event, type) -> "$event (${type.description})" }),
                    inclusionEvents = activatingVariants + potentiallyActivatingWarnings.map { (event, _) -> event }
                )
            }

            else -> {
                val potentialWarnEvaluation = evaluatePotentialWarns(
                    eventsByWarningType[ActivationWarningType.ASSOCIATED_WITH_RESISTANCE],
                    eventsByWarningType[ActivationWarningType.NON_ONCOGENE],
                    eventsByWarningType[ActivationWarningType.NO_HOTSPOT_AND_NO_GAIN_OF_FUNCTION],
                    eventsByWarningType[ActivationWarningType.SUBCLONAL],
                    eventsByWarningType[ActivationWarningType.NON_HIGH_DRIVER_GAIN_OF_FUNCTION],
                    eventsByWarningType[ActivationWarningType.NON_HIGH_DRIVER_SUBCLONAL],
                    eventsByWarningType[ActivationWarningType.NON_HIGH_DRIVER],
                    eventsByWarningType[ActivationWarningType.OTHER_MISSENSE_OR_HOTSPOT],
                    evidenceSource
                )

                potentialWarnEvaluation ?: EvaluationFactory.fail("No $gene activating mutation(s)")
            }
        }
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

    private fun isSubclonal(variant: Variant) = variant.extendedVariantDetails?.clonalLikelihood?.let { it < CLONAL_CUTOFF } == true

    private fun ignoredCodon(
        codonsToIgnore: List<String>?, variant: Variant
    ) = codonsToIgnore == null || codonsToIgnore.none {
        isCodonMatch(
            variant.canonicalImpact.affectedCodon, it
        )
    }

    private fun evaluatePotentialWarns(
        activatingVariantsAssociatedWithResistance: Set<String>?,
        activatingVariantsInNonOncogene: Set<String>?,
        activatingVariantsNoHotspotAndNoGainOfFunction: Set<String>?,
        activatingSubclonalVariants: Set<String>?,
        nonHighDriverGainOfFunctionVariants: Set<String>?,
        nonHighDriverSubclonalVariants: Set<String>?,
        nonHighDriverVariants: Set<String>?,
        otherMissenseOrHotspotVariants: Set<String>?,
        evidenceSource: String
    ): Evaluation? {
        return MolecularEventUtil.evaluatePotentialWarnsForEventGroups(
            listOf(
                EventsWithMessages(
                    activatingVariantsAssociatedWithResistance,
                    "$gene activating mutation(s) ${activatingVariantsAssociatedWithResistance?.let { concatVariants(it, gene) }} " +
                            "(also associated with drug resistance in $evidenceSource)"
                ),
                EventsWithMessages(
                    activatingVariantsInNonOncogene,
                    "$gene activating mutation(s) ${activatingVariantsInNonOncogene?.let { concatVariants(it, gene) }} " +
                            "- however gene known as TSG in $evidenceSource"
                ),
                EventsWithMessages(
                    activatingVariantsNoHotspotAndNoGainOfFunction,
                    "$gene potentially activating mutation(s) ${activatingVariantsNoHotspotAndNoGainOfFunction?.let {
                            concatVariants(it, gene)
                        }
                    } with high driver likelihood - however not a hotspot and not associated with gain-of-function protein effect evidence in $evidenceSource"
                ),
                EventsWithMessages(
                    activatingSubclonalVariants,
                    gene + " potentially activating mutation(s) " + activatingSubclonalVariants?.let { concatVariants(it, gene) } +
                            " but subclonal likelihood > " + Format.percentage(1 - CLONAL_CUTOFF)
                ),
                EventsWithMessages(
                    nonHighDriverGainOfFunctionVariants,
                    "$gene potentially activating mutation(s) " + nonHighDriverGainOfFunctionVariants?.let { concatVariants(it, gene) } +
                            " without high driver prediction but having gain-of-function protein effect evidence in $evidenceSource"
                ),
                EventsWithMessages(
                    nonHighDriverSubclonalVariants,
                    "$gene potentially activating mutation(s) " + activatingSubclonalVariants?.let { concatVariants(it, gene) } +
                            " have subclonal likelihood of > ${Format.percentage(1 - CLONAL_CUTOFF)} and no high driver likelihood"
                ),
                EventsWithMessages(
                    nonHighDriverVariants,
                    "$gene potentially activating mutation(s) " + nonHighDriverVariants?.let { concatVariants(it, gene) } +
                            " but no high driver likelihood"
                ),
                EventsWithMessages(
                    otherMissenseOrHotspotVariants,
                    "$gene potentially activating mutation(s) " + otherMissenseOrHotspotVariants?.let { concatVariants(it, gene) } +
                            " that are missense or have hotspot status but are not considered reportable"
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