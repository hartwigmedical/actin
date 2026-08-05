package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationLabels
import com.hartwig.actin.algo.evaluation.molecular.MolecularVariantUtil.toProteinImpact
import com.hartwig.actin.algo.evaluation.molecular.MolecularVariantUtil.variantTypesForInput
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
import com.hartwig.actin.datamodel.trial.VariantTypeInput

private enum class ActivationWarningType {
    NON_ONCOGENE,
    NO_CANCER_ASSOCIATED_VARIANT,
    SUBCLONAL,
    NON_HIGH_DRIVER_SUBCLONAL,
    NON_HIGH_DRIVER,
    OTHER_MISSENSE_OR_CANCER_ASSOCIATED_VARIANT,
}

private fun ActivationWarningType.description(subclonalPercentage: String, labels: EvaluationLabels.Molecular): String? = when (this) {
    ActivationWarningType.NO_CANCER_ASSOCIATED_VARIANT -> labels.geneHasActivatingMutationDescriptionNoCav()
    ActivationWarningType.SUBCLONAL -> labels.geneHasActivatingMutationDescriptionSubclonal(subclonalPercentage)
    else -> null
}

private data class ActivationProfile(
    val event: String,
    val activating: Boolean,
    val warningType: ActivationWarningType? = null
)

private const val CLONAL_CUTOFF = 0.5

class GeneHasActivatingMutation(
    override val gene: String,
    private val codonsToIgnore: Set<String>? = null,
    private val inKinaseDomain: Boolean = false,
    private val proteinImpactsToIgnore: Set<String>? = null,
    private val variantTypeToIgnore: VariantTypeInput? = null,
    private val exonToIgnore: Int? = null,
    labels: EvaluationLabels.Molecular
) : MolecularEvaluationFunction(
    targetCoveragePredicate = specific(MolecularTestTarget.MUTATION, messagePrefix = labels.geneHasActivatingMutationMessagePrefix()),
    labels = labels
) {
    override fun evaluate(test: MolecularTest): Evaluation {
        val hasHighMutationalLoad = test.characteristics.tumorMutationalLoad?.isHigh
        val evidenceSource = test.evidenceSource
        val variantCharacteristics =
            test.drivers.variants.filter { it.gene == gene }
                .filter { ignoredCodon(codonsToIgnore, it) }
                .filter { ignoredProteinImpact(proteinImpactsToIgnore, it) }
                .filter { ignoredExonAndType(variantTypeToIgnore, exonToIgnore, it) }
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
        val subclonalPercentage = Format.percentage(1 - CLONAL_CUTOFF)

        return when {
            activatingVariants.isNotEmpty() && potentiallyActivatingWarnings.isEmpty() -> {
                if (!inKinaseDomain)
                    EvaluationFactory.pass(
                        labels.geneHasActivatingMutationPass(gene, variantsString),
                        inclusionEvents = activatingVariants
                    ) else EvaluationFactory.warn(
                    labels.geneHasActivatingMutationWarnKinase(gene, variantsString, inKinaseDomainString),
                    inclusionEvents = activatingVariants
                )
            }

            activatingVariants.isNotEmpty() -> {
                EvaluationFactory.warn(
                    labels.geneHasActivatingMutationWarnWithOther(
                        gene, variantsString,
                        concat(potentiallyActivatingWarnings.map { (event, type) ->
                            "$event (${type.description(subclonalPercentage, labels)})$inKinaseDomainString"
                        })
                    ),
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

                potentialWarnEvaluation ?: EvaluationFactory.fail(labels.geneHasActivatingMutationFail(gene))
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
        codonsToIgnore: Set<String>?, variant: Variant
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
        val percentage = Format.percentage(1 - CLONAL_CUTOFF)

        return MolecularEventUtil.evaluatePotentialWarnsForEventGroups(
            listOf(
                EventsWithMessages(
                    activatingVariantsInNonOncogene,
                    labels.geneHasActivatingMutationWarnNonOncogene(
                        gene,
                        activatingVariantsInNonOncogene?.let { concatVariants(it, gene) }.orEmpty(),
                        evidenceSource,
                        inKinaseDomainString
                    )
                ),
                EventsWithMessages(
                    activatingVariantsNoCavAndNoGainOfFunction,
                    labels.geneHasActivatingMutationWarnNoCav(
                        gene,
                        activatingVariantsNoCavAndNoGainOfFunction?.let { concatVariants(it, gene) }.orEmpty(),
                        inKinaseDomainString
                    )
                ),
                EventsWithMessages(
                    activatingSubclonalVariants,
                    labels.geneHasActivatingMutationWarnSubclonal(
                        gene, activatingSubclonalVariants?.let { concatVariants(it, gene) }.orEmpty(), percentage, inKinaseDomainString
                    )
                ),
                EventsWithMessages(
                    nonHighDriverSubclonalVariants,
                    labels.geneHasActivatingMutationWarnNonHighDriverSubclonal(
                        gene, nonHighDriverSubclonalVariants?.let { concatVariants(it, gene) }.orEmpty(), percentage, inKinaseDomainString
                    )
                ),
                EventsWithMessages(
                    nonHighDriverVariants,
                    labels.geneHasActivatingMutationWarnNonHighDriver(
                        gene, nonHighDriverVariants?.let { concatVariants(it, gene) }.orEmpty(), inKinaseDomainString
                    )
                ),
                EventsWithMessages(
                    otherMissenseOrCancerAssociatedVariants,
                    labels.geneHasActivatingMutationWarnOtherMissense(
                        gene,
                        otherMissenseOrCancerAssociatedVariants?.let { concatVariants(it, gene) }.orEmpty(),
                        inKinaseDomainString
                    )
                )
            )
        )
    }

    private fun isMissenseOrCancerAssociatedVariant(variant: Variant): Boolean {
        return variant.canonicalImpact.codingEffect == CodingEffect.MISSENSE || variant.isCancerAssociatedVariant
    }

    private fun ignoredProteinImpact(impactsToIgnore: Set<String>?, variant: Variant): Boolean {
        return impactsToIgnore == null || toProteinImpact(variant.canonicalImpact.hgvsProteinImpact) !in impactsToIgnore
    }

    private fun ignoredExonAndType(variantTypeToIgnore: VariantTypeInput?, exonToIgnore: Int?, variant: Variant): Boolean {
        return variantTypeToIgnore == null || exonToIgnore == null ||
                !(variant.canonicalImpact.affectedExon == exonToIgnore && variant.type in variantTypesForInput(variantTypeToIgnore))
    }

    private fun isCodonMatch(affectedCodon: Int?, codonsToMatch: String): Boolean {
        if (affectedCodon == null) {
            return false
        }
        val codonIndexToMatch = codonsToMatch.substring(1).takeWhile { it.isDigit() }.toInt()
        return codonIndexToMatch == affectedCodon
    }
}