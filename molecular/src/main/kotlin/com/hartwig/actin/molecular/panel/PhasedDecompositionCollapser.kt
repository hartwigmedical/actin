package com.hartwig.actin.molecular.panel

import com.hartwig.actin.molecular.paver.PaveVariantEffect
import com.hartwig.actin.tools.variant.VariantAnnotator
import org.apache.logging.log4j.LogManager
import com.hartwig.actin.tools.variant.Variant as TransvarVariant

object PhasedDecompositionCollapser {

    private val LOGGER = LogManager.getLogger(PhasedDecompositionCollapser::class.java)

    fun collapse(
        phaseSet: Int,
        variants: List<AnnotatableVariant>,
        variantResolver: VariantAnnotator
    ): AnnotatableVariant {
        validateProteinImpacts(phaseSet, variants)

        val representative = selectRepresentative(variants)
        val withResolvedExonCodon = applyPhaseSetExonCodon(phaseSet, variants, representative)
        val withOriginalCoordinates = applyOriginalHgvsCoordinates(phaseSet, variants, variantResolver, withResolvedExonCodon)
        return normalizePhasedEffects(withOriginalCoordinates)
    }

    private fun validateProteinImpacts(phaseSet: Int, variants: List<AnnotatableVariant>) {
        val proteinImpactsRaw = variants.map { it.paveResponse?.impact?.hgvsProteinImpact }
        if (proteinImpactsRaw.any { it.isNullOrBlank() }) {
            throw IllegalStateException("Missing protein impact within phase set $phaseSet")
        }

        val proteinImpacts = proteinImpactsRaw.filterNotNull().toSet()
        require(proteinImpacts.size <= 1) {
            "Mismatched protein impacts within phase set $phaseSet: $proteinImpacts"
        }
    }

    private fun selectRepresentative(variants: List<AnnotatableVariant>): AnnotatableVariant {
        return variants.first()
    }

    private fun applyPhaseSetExonCodon(
        phaseSet: Int,
        variants: List<AnnotatableVariant>,
        representative: AnnotatableVariant
    ): AnnotatableVariant {
        val responses = variants.map { it.paveResponse ?: throw IllegalStateException("Missing PAVE response for phase set $phaseSet") }
        val resolvedExonCodon = PhaseSetExonCodonResolver.resolve(phaseSet, responses) ?: return representative
        val paveResponse = representative.paveResponse ?: return representative
        return representative.copy(
            paveResponse = PhaseSetExonCodonResolver.applyToResponse(paveResponse, resolvedExonCodon)
        )
    }

    private fun applyOriginalHgvsCoordinates(
        phaseSet: Int,
        variants: List<AnnotatableVariant>,
        variantResolver: VariantAnnotator,
        representative: AnnotatableVariant
    ): AnnotatableVariant {
        val originalTransvar = resolveOriginalHgvsTransvar(phaseSet, variants, variantResolver)
        return if (originalTransvar == null) {
            representative
        } else {
            representative.copy(transvarVariant = originalTransvar)
        }
    }

    private fun normalizePhasedEffects(variant: AnnotatableVariant): AnnotatableVariant {
        val paveResponse = variant.paveResponse ?: return variant
        val normalizedImpact = paveResponse.impact.copy(
            canonicalEffects = paveResponse.impact.canonicalEffects.map(::toNonPhasedEffect)
        )
        val normalizedTranscriptImpacts = paveResponse.transcriptImpacts.map { transcriptImpact ->
            transcriptImpact.copy(
                effects = transcriptImpact.effects.map(::toNonPhasedEffect)
            )
        }
        return variant.copy(
            paveResponse = paveResponse.copy(
                impact = normalizedImpact,
                transcriptImpacts = normalizedTranscriptImpacts,
            )
        )
    }

    private fun toNonPhasedEffect(effect: PaveVariantEffect): PaveVariantEffect {
        return when (effect) {
            PaveVariantEffect.PHASED_MISSENSE -> PaveVariantEffect.MISSENSE
            PaveVariantEffect.PHASED_INFRAME_INSERTION -> PaveVariantEffect.INFRAME_INSERTION
            PaveVariantEffect.PHASED_INFRAME_DELETION -> PaveVariantEffect.INFRAME_DELETION
            PaveVariantEffect.PHASED_SYNONYMOUS -> PaveVariantEffect.SYNONYMOUS
            else -> effect
        }
    }

    private fun resolveOriginalHgvsTransvar(
        phaseSet: Int,
        variants: List<AnnotatableVariant>,
        variantResolver: VariantAnnotator
    ): TransvarVariant? {
        val variant = variants.first().sequencedVariant
        return variant.hgvsCodingImpact
            ?.trim()
            ?.takeIf { it.isNotEmpty() }
            ?.let { originalHgvs ->
                variantResolver.resolve(variant.gene, variant.transcript, originalHgvs).also { resolved ->
                    if (resolved == null) {
                        LOGGER.warn(
                            "Unable to resolve original coding HGVS '{}' for gene '{}' in phase set {}",
                            originalHgvs,
                            variant.gene,
                            phaseSet
                        )
                    }
                }
            }
    }
}
