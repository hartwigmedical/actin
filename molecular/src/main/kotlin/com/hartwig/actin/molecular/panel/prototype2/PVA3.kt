package com.hartwig.actin.molecular.panel.prototype2

import com.hartwig.actin.datamodel.clinical.SequencedVariant
import com.hartwig.actin.datamodel.molecular.driver.Variant
import com.hartwig.actin.molecular.panel.PanelAnnotatorVariantFactory
import com.hartwig.actin.molecular.panel.VariantDecompositionIndex
import com.hartwig.actin.molecular.paver.PaveQuery
import com.hartwig.actin.molecular.paver.PaveResponse
import com.hartwig.actin.molecular.paver.Paver
import com.hartwig.actin.tools.pave.PaveLite
import com.hartwig.actin.tools.variant.VariantAnnotator
import com.hartwig.actin.tools.variant.Variant as TransvarVariant

data class VariantIndex(val id: Int, val sequencedVariantId: Int)

data class AnnotatableVariant(
    val variantIndex: VariantIndex,
    val sequencedVariant: SequencedVariant,
    val queryHgvs: String,
    val phaseGroupKey: Int?, // grouping key for decomposed variants; null for direct
    val transvarVariant: TransvarVariant? = null,
    val paveResponse: PaveResponse? = null,
)

class PVA3(
    private val variantResolver: VariantAnnotator,
    private val paver: Paver,
    private val paveLite: PaveLite,
    private val decompositions: VariantDecompositionIndex
) {

    private val variantFactory = PanelAnnotatorVariantFactory(paveLite)

    fun annotate(sequencedVariants: Set<SequencedVariant>): List<Variant> {
        val expanded = applyDecompositions(sequencedVariants.toList())
        val withTransvar = annotateWithTransvar(expanded)
        val withPave = annotateWithPave(withTransvar)
        val dedupedAnnotated = deduplicateAnnotated(withPave)

        return createVariants(dedupedAnnotated)
    }

    fun applyDecompositions(sequencedVariants: List<SequencedVariant>): List<AnnotatableVariant> {
        val expanded = sequencedVariants.withIndex().flatMap { (sequencedVariantId, variant) ->
            val decomposedHgvsList =
                variant.hgvsCodingImpact?.let { decompositions.lookup(it) }?.decomposedCodingHgvs
                    ?: variant.hgvsProteinImpact?.let { decompositions.lookup(it) }?.decomposedCodingHgvs

            if (decomposedHgvsList != null) {
                decomposedHgvsList.map { decomposedHgvs ->
                    AnnotatableVariant(
                        variantIndex = VariantIndex(-1, sequencedVariantId), // placeholder, set below
                        sequencedVariant = variant,
                        queryHgvs = decomposedHgvs,
                        phaseGroupKey = sequencedVariantId
                    )
                }
            } else {
                listOf(
                    AnnotatableVariant(
                        variantIndex = VariantIndex(-1, sequencedVariantId), // placeholder, set below
                        sequencedVariant = variant,
                        queryHgvs = variant.hgvsCodingOrProteinImpact(),
                        phaseGroupKey = null
                    )
                )
            }
        }

        return expanded.mapIndexed { idx, variant ->
            variant.copy(variantIndex = VariantIndex(idx, variant.variantIndex.sequencedVariantId))
        }
    }

    fun annotateWithTransvar(annotatable: List<AnnotatableVariant>): List<AnnotatableVariant> {
        return annotatable.map { variant ->
            val transvar = variantResolver.resolve(variant.sequencedVariant.gene, variant.sequencedVariant.transcript, variant.queryHgvs)
                ?: throw IllegalStateException("Unable to resolve variant '${variant.queryHgvs}' for gene '${variant.sequencedVariant.gene}'")

            variant.copy(transvarVariant = transvar)
        }
    }

    fun annotateWithPave(
        transvarResponses: List<AnnotatableVariant>
    ): List<AnnotatableVariant> {
        if (transvarResponses.isEmpty()) {
            return emptyList()
        }

        val queries = transvarResponses.map { response ->
            val transvarVariant = response.transvarVariant
                ?: throw IllegalStateException("Missing Transvar annotation for id ${response.variantIndex.id}")

            PaveQuery(
                id = response.variantIndex.id.toString(),
                chromosome = transvarVariant.chromosome(),
                position = transvarVariant.position(),
                ref = transvarVariant.ref(),
                alt = transvarVariant.alt(),
                localPhaseSet = response.phaseGroupKey
            )
        }
        val responses = paver.run(queries)
        val responsesById = responses.associateBy { it.id }
        require(responsesById.keys == queries.map { it.id }.toSet()) { "PAVE did not return responses for all queries" }

        return transvarResponses.map { variant ->
            val paveResponse =
                responsesById[variant.variantIndex.id.toString()]
                    ?: throw IllegalStateException("Missing PAVE response for query ${variant.variantIndex.id}")

            variant.copy(paveResponse = paveResponse)
        }
    }

    fun deduplicateAnnotated(annotated: List<AnnotatableVariant>): List<AnnotatableVariant> {
        val (expanded, direct) = annotated.partition { it.phaseGroupKey != null }

        val groupedByPhase: Map<Int?, List<AnnotatableVariant>> =
            expanded.groupBy { it.paveResponse?.localPhaseSet }
        val dedupedExpanded = groupedByPhase.entries.flatMap { entry ->
            val phaseSet = entry.key
            val variants = entry.value
            if (phaseSet == null) {
                variants
            } else {
                val proteinImpacts = variants.mapNotNull { it.paveResponse?.impact?.hgvsProteinImpact }.toSet()
                require(proteinImpacts.size <= 1) {
                    "Mismatched protein impacts within phase set $phaseSet: $proteinImpacts"
                }
                variants.take(1)
            }
        }

        return direct + dedupedExpanded
    }

    private fun createVariants(
        dedupedAnnotated: List<AnnotatableVariant>
    ): List<Variant> {
        return dedupedAnnotated.map { annotatedVariant ->
            val transvar = annotatedVariant.transvarVariant
                ?: throw IllegalStateException("Missing Transvar annotation for id ${annotatedVariant.variantIndex.id}")
            val paveResponse = annotatedVariant.paveResponse
                ?: throw IllegalStateException("Missing PAVE response for id ${annotatedVariant.variantIndex.id}")

            val baseVariant = variantFactory.createVariant(
                annotatedVariant.sequencedVariant,
                transvar,
                paveResponse
            )

            paveResponse.localPhaseSet?.let { phase -> baseVariant.copy(phaseGroups = setOf(phase)) } ?: baseVariant
        }
    }
}
