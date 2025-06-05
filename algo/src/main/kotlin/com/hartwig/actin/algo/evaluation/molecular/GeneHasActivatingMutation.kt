package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.util.Format
import com.hartwig.actin.algo.evaluation.util.Format.concat
import com.hartwig.actin.algo.evaluation.util.Format.concatVariants
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.molecular.MolecularTest
import com.hartwig.actin.datamodel.molecular.MolecularTestTarget
import com.hartwig.actin.datamodel.molecular.driver.CodingEffect
import com.hartwig.actin.datamodel.molecular.driver.DriverLikelihood
import com.hartwig.actin.datamodel.molecular.driver.GeneRole
import com.hartwig.actin.datamodel.molecular.driver.Variant
import java.time.LocalDate

enum class ActivationWarningType(val description: String? = null) {
    ASSOCIATED_WITH_RESISTANCE(
        "Potentially activating mutation(s) that have high driver likelihood " +
                "but are also associated with drug resistance"
    ),
    NON_ONCOGENE,
    NO_CANCER_ASSOCIATED_VARIANT(
        "Potentially activating mutation(s) that have high driver likelihood " +
                "but is not a cancer-associated variant"
    ),
    SUBCLONAL(
        "Potentially activating mutation(s) that have high driver likelihood " +
                "but also have subclonal likelihood of > ${Format.percentage(1 - CLONAL_CUTOFF)}"
    ),
    NON_HIGH_DRIVER_SUBCLONAL,
    NON_HIGH_DRIVER,
    OTHER_MISSENSE_OR_CANCER_ASSOCIATED_VARIANT,
    POTENTIALLY_RELEVANT_MET_EXON_14_SKIPPING_MUTATIONS,
}

data class ActivationProfile(
    val event: String,
    val activating: Boolean,
    val warningType: ActivationWarningType? = null
)

private const val CLONAL_CUTOFF = 0.5

class GeneHasActivatingMutation(
    override val gene: String,
    private val codonsToIgnore: List<String>?,
    maxTestAge: LocalDate? = null,
    private val inKinaseDomain: Boolean = false,
) : MolecularEvaluationFunction(
    targetCoveragePredicate = specific(MolecularTestTarget.MUTATION, messagePrefix = "Activating mutation in"),
    maxTestAge = maxTestAge
) {
    override fun evaluate(test: MolecularTest): Evaluation {
        val hasHighMutationalLoad = test.characteristics.tumorMutationalLoad?.isHigh
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
            ActivationWarningType.NO_CANCER_ASSOCIATED_VARIANT,
            ActivationWarningType.SUBCLONAL,
        ).flatMap { warningType -> eventsByWarningType[warningType]?.map { event -> event to warningType } ?: emptyList() }

        val variantsString = concatVariants(activatingVariants, gene)
        val inKinaseDomainString = if (inKinaseDomain) " but undetermined if in kinase domain" else ""

        return when {
            activatingVariants.isNotEmpty() && potentiallyActivatingWarnings.isEmpty() -> {
                if (!inKinaseDomain)
                    EvaluationFactory.pass(
                        "$gene activating mutation(s): $variantsString",
                        inclusionEvents = activatingVariants
                    ) else EvaluationFactory.warn(
                    "$gene activating mutation(s): $variantsString$inKinaseDomainString",
                    inclusionEvents = activatingVariants
                )
            }

            activatingVariants.isNotEmpty() -> {
                EvaluationFactory.warn(
                    "$gene activating mutation(s): $variantsString " +
                            "together with potentially activating mutation(s) " +
                            concat(potentiallyActivatingWarnings.map { (event, type) -> "$event (${type.description})$inKinaseDomainString" }),
                    inclusionEvents = activatingVariants + potentiallyActivatingWarnings.map { (event, _) -> event }
                )
            }

            else -> {
                val potentialWarnEvaluation = evaluatePotentialWarns(
                    eventsByWarningType[ActivationWarningType.ASSOCIATED_WITH_RESISTANCE],
                    eventsByWarningType[ActivationWarningType.NON_ONCOGENE],
                    eventsByWarningType[ActivationWarningType.NO_CANCER_ASSOCIATED_VARIANT],
                    eventsByWarningType[ActivationWarningType.SUBCLONAL],
                    eventsByWarningType[ActivationWarningType.NON_HIGH_DRIVER_SUBCLONAL],
                    eventsByWarningType[ActivationWarningType.NON_HIGH_DRIVER],
                    eventsByWarningType[ActivationWarningType.OTHER_MISSENSE_OR_CANCER_ASSOCIATED_VARIANT],
                    eventsByWarningType[ActivationWarningType.POTENTIALLY_RELEVANT_MET_EXON_14_SKIPPING_MUTATIONS],
                    evidenceSource
                )

                potentialWarnEvaluation ?: EvaluationFactory.fail("No $gene activating mutation(s)")
            }
        }
    }

    private fun evaluateVariant(variant: Variant, hasHighMutationalLoad: Boolean?): ActivationProfile {
        val isNoOncogene = variant.geneRole == GeneRole.TSG
        val isNonCodingSpliceRegionVariantInMetExon14 =
            gene == "MET" && variant.gene == "MET" && variant.canonicalImpact.affectedExon == 14 && variant.canonicalImpact.codingEffect == CodingEffect.NONE && variant.canonicalImpact.isSpliceRegion == true

        return if (variant.isReportable) {
            if (variant.driverLikelihood == DriverLikelihood.HIGH) {
                return when {
                    isAssociatedWithDrugResistance(variant) -> profile(variant.event, ActivationWarningType.ASSOCIATED_WITH_RESISTANCE)
                    !variant.isCancerAssociatedVariant -> profile(
                        variant.event,
                        ActivationWarningType.NO_CANCER_ASSOCIATED_VARIANT
                    )

                    isNoOncogene -> profile(variant.event, ActivationWarningType.NON_ONCOGENE)
                    isSubclonal(variant) -> profile(variant.event, ActivationWarningType.SUBCLONAL)
                    else -> profile(variant.event, activating = true)
                }
            } else {
                if (hasHighMutationalLoad == null || !hasHighMutationalLoad) {
                    return if (isSubclonal(variant)) {
                        profile(variant.event, ActivationWarningType.NON_HIGH_DRIVER_SUBCLONAL)
                    } else if (isNonCodingSpliceRegionVariantInMetExon14) {
                        profile(variant.event, ActivationWarningType.POTENTIALLY_RELEVANT_MET_EXON_14_SKIPPING_MUTATIONS)
                    } else {
                        profile(variant.event, ActivationWarningType.NON_HIGH_DRIVER)
                    }
                } else {
                    profile(variant.event)
                }
            }
        } else if (isMissenseOrCancerAssociatedVariant(variant)) {
            return profile(variant.event, ActivationWarningType.OTHER_MISSENSE_OR_CANCER_ASSOCIATED_VARIANT)
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
        activatingVariantsNoCavAndNoGainOfFunction: Set<String>?,
        activatingSubclonalVariants: Set<String>?,
        nonHighDriverSubclonalVariants: Set<String>?,
        nonHighDriverVariants: Set<String>?,
        otherMissenseOrCancerAssociatedVariants: Set<String>?,
        spliceRegionPotentialMetExon14SkippingMutations: Set<String>?,
        evidenceSource: String
    ): Evaluation? {
        val inKinaseDomainString = if (inKinaseDomain) " and undetermined if in kinase domain" else ""

        return MolecularEventUtil.evaluatePotentialWarnsForEventGroups(
            listOf(
                EventsWithMessages(
                    activatingVariantsAssociatedWithResistance,
                    "$gene activating mutation(s) ${activatingVariantsAssociatedWithResistance?.let { concatVariants(it, gene) }} " +
                            "(also associated with drug resistance in $evidenceSource)$inKinaseDomainString"
                ),
                EventsWithMessages(
                    activatingVariantsInNonOncogene,
                    "$gene activating mutation(s) ${activatingVariantsInNonOncogene?.let { concatVariants(it, gene) }} " +
                            "- however gene known as TSG in $evidenceSource$inKinaseDomainString"
                ),
                EventsWithMessages(
                    activatingVariantsNoCavAndNoGainOfFunction,
                    "$gene potentially activating mutation(s) ${
                        activatingVariantsNoCavAndNoGainOfFunction?.let {
                            concatVariants(it, gene)
                        }
                    } with high driver likelihood - however not a cancer-associated variant$inKinaseDomainString"
                ),
                EventsWithMessages(
                    activatingSubclonalVariants,
                    gene + " potentially activating mutation(s) " + activatingSubclonalVariants?.let { concatVariants(it, gene) } +
                            " but subclonal likelihood > $Format.percentage(1 - CLONAL_CUTOFF)$inKinaseDomainString"
                ),
                EventsWithMessages(
                    nonHighDriverSubclonalVariants,
                    "$gene potentially activating mutation(s) " + activatingSubclonalVariants?.let { concatVariants(it, gene) } +
                            " have subclonal likelihood of > ${Format.percentage(1 - CLONAL_CUTOFF)} and no high driver likelihood$inKinaseDomainString"
                ),
                EventsWithMessages(
                    nonHighDriverVariants,
                    "$gene potentially activating mutation(s) " + nonHighDriverVariants?.let { concatVariants(it, gene) } +
                            " but no high driver likelihood$inKinaseDomainString"
                ),
                EventsWithMessages(
                    otherMissenseOrCancerAssociatedVariants,
                    "$gene potentially activating mutation(s) " + otherMissenseOrCancerAssociatedVariants?.let {
                        concatVariants(
                            it,
                            gene
                        )
                    } + " that are missense or have cancer-associated variant status but are not considered reportable$inKinaseDomainString"
                ),
                EventsWithMessages(
                    spliceRegionPotentialMetExon14SkippingMutations,
                    "$gene has non-coding splice region mutation(s) in exon 14" + spliceRegionPotentialMetExon14SkippingMutations?.let {
                        concatVariants(
                            it,
                            gene
                        )
                    } + " undetermined if this could still potentially be an activating mutation$inKinaseDomainString"
                )
            )
        )
    }

    private fun isAssociatedWithDrugResistance(variant: Variant): Boolean {
        val isAssociatedWithDrugResistance = variant.isAssociatedWithDrugResistance
        return isAssociatedWithDrugResistance != null && isAssociatedWithDrugResistance
    }

    private fun isMissenseOrCancerAssociatedVariant(variant: Variant): Boolean {
        return variant.canonicalImpact.codingEffect == CodingEffect.MISSENSE || variant.isCancerAssociatedVariant
    }

    private fun isCodonMatch(affectedCodon: Int?, codonsToMatch: String): Boolean {
        if (affectedCodon == null) {
            return false
        }
        val codonIndexToMatch = codonsToMatch.substring(1).takeWhile { it.isDigit() }.toInt()
        return codonIndexToMatch == affectedCodon
    }
}