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

private enum class ActivationWarningType(val description: String? = null) {
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
}

private data class ActivationProfile(
    val event: String,
    val activating: Boolean,
    val warningType: ActivationWarningType? = null
)

private const val CLONAL_CUTOFF = 0.5

class GeneHasActivatingMutation(
    override val gene: String,
    private val codonsToIgnore: List<String>?,
    private val inKinaseDomain: Boolean = false,
) : MolecularEvaluationFunction(
    targetCoveragePredicate = specific(MolecularTestTarget.MUTATION, messagePrefix = "Activating mutation in")
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
                    eventsByWarningType[ActivationWarningType.NON_ONCOGENE],
                    eventsByWarningType[ActivationWarningType.NO_CANCER_ASSOCIATED_VARIANT],
                    eventsByWarningType[ActivationWarningType.SUBCLONAL],
                    eventsByWarningType[ActivationWarningType.NON_HIGH_DRIVER_SUBCLONAL],
                    eventsByWarningType[ActivationWarningType.NON_HIGH_DRIVER],
                    eventsByWarningType[ActivationWarningType.OTHER_MISSENSE_OR_CANCER_ASSOCIATED_VARIANT],
                    evidenceSource
                )

                potentialWarnEvaluation ?: EvaluationFactory.fail("No $gene activating mutation(s)")
            }
        }
    }

    private fun evaluateVariant(variant: Variant, hasHighMutationalLoad: Boolean?): ActivationProfile {
        val isNoOncogene = variant.geneRole == GeneRole.TSG

        return if (variant.isReportable) {
            if (variant.driverLikelihood == DriverLikelihood.HIGH) {
                when {
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
                    if (isSubclonal(variant)) {
                        profile(variant.event, ActivationWarningType.NON_HIGH_DRIVER_SUBCLONAL)
                    } else {
                        profile(variant.event, ActivationWarningType.NON_HIGH_DRIVER)
                    }
                } else {
                    profile(variant.event)
                }
            }
        } else if (isMissenseOrCancerAssociatedVariant(variant)) {
            profile(variant.event, ActivationWarningType.OTHER_MISSENSE_OR_CANCER_ASSOCIATED_VARIANT)
        } else {
            profile(variant.event)
        }
    }

    private fun profile(event: String, warningType: ActivationWarningType? = null, activating: Boolean = false) =
        ActivationProfile(event = event, activating = activating, warningType = warningType)

    private fun isSubclonal(variant: Variant) = variant.clonalLikelihood?.let { it < CLONAL_CUTOFF } == true

    private fun ignoredCodon(
        codonsToIgnore: List<String>?, variant: Variant
    ) = codonsToIgnore == null || codonsToIgnore.none {
        isCodonMatch(
            variant.canonicalImpact.affectedCodon, it
        )
    }

    private fun evaluatePotentialWarns(
        activatingVariantsInNonOncogene: Set<String>?,
        activatingVariantsNoCavAndNoGainOfFunction: Set<String>?,
        activatingSubclonalVariants: Set<String>?,
        nonHighDriverSubclonalVariants: Set<String>?,
        nonHighDriverVariants: Set<String>?,
        otherMissenseOrCancerAssociatedVariants: Set<String>?,
        evidenceSource: String
    ): Evaluation? {
        val inKinaseDomainString = if (inKinaseDomain) " and undetermined if in kinase domain" else ""

        return MolecularEventUtil.evaluatePotentialWarnsForEventGroups(
            listOf(
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
                            " but subclonal likelihood > ${Format.percentage(1 - CLONAL_CUTOFF)}$inKinaseDomainString"
                ),
                EventsWithMessages(
                    nonHighDriverSubclonalVariants,
                    "$gene potentially activating mutation(s) " + nonHighDriverSubclonalVariants?.let { concatVariants(it, gene) } +
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
                )
            )
        )
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