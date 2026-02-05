package com.hartwig.actin.molecular.panel

import com.hartwig.actin.datamodel.clinical.SequencedVariant

class VariantDecompositionExpander(
    private val decompositions: VariantDecompositionTable
) {

    private data class PendingVariant(
        val sequencedVariant: SequencedVariant,
        val queryHgvs: String,
        val localPhaseSet: Int?
    )

    fun expand(
        sequencedVariants: Set<SequencedVariant>
    ): List<AnnotatableVariant> {
        val sortedSequencedVariants = sortSequencedVariantsForDeterminism(sequencedVariants)
        return expand(sortedSequencedVariants)
    }

    private fun sortSequencedVariantsForDeterminism(variants: Set<SequencedVariant>): List<SequencedVariant> {
        val comparator =
            compareBy<SequencedVariant> { it.gene }
                .thenBy { it.transcript.orEmpty() }
                .thenBy { it.hgvsCodingImpact.orEmpty() }
                .thenBy { it.hgvsProteinImpact.orEmpty() }

        return variants.sortedWith(comparator)
    }

    private fun expand(
        sequencedVariants: List<SequencedVariant>
    ): List<AnnotatableVariant> {
        val expanded = sequencedVariants.withIndex().flatMap { (sequencedVariantId, variant) ->
            val decomposedHgvsList = variant.hgvsCodingImpact
                ?.let { decompositions.lookup(variant.gene, variant.transcript, it) }
                ?.decomposedCodingHgvs

            if (decomposedHgvsList != null) {
                decomposedHgvsList.map { decomposedHgvs ->
                    PendingVariant(
                        sequencedVariant = variant,
                        queryHgvs = decomposedHgvs,
                        localPhaseSet = sequencedVariantId
                    )
                }
            } else {
                listOf(
                    PendingVariant(
                        sequencedVariant = variant,
                        queryHgvs = variant.hgvsCodingOrProteinImpact(),
                        localPhaseSet = null
                    )
                )
            }
        }

        return expanded.mapIndexed { idx, pending ->
            AnnotatableVariant(
                queryId = idx,
                sequencedVariant = pending.sequencedVariant,
                queryHgvs = pending.queryHgvs,
                localPhaseSet = pending.localPhaseSet
            )
        }
    }
}
