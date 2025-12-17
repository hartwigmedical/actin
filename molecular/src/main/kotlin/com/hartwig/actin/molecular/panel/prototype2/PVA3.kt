package com.hartwig.actin.molecular.panel.prototype2

import com.hartwig.actin.datamodel.clinical.SequencedVariant
import com.hartwig.actin.datamodel.molecular.driver.Variant
import com.hartwig.actin.molecular.panel.VariantDecompositionIndex
import com.hartwig.actin.molecular.panel.VariantFactory
import com.hartwig.actin.molecular.paver.PaveQuery
import com.hartwig.actin.molecular.paver.PaveResponse
import com.hartwig.actin.molecular.paver.Paver
import com.hartwig.actin.tools.variant.VariantAnnotator
import com.hartwig.actin.tools.variant.Variant as TransvarVariant

private const val UNASSIGNED_QUERY_ID = -1

data class AnnotatableVariant(
    val queryId: Int,
    val sequencedVariant: SequencedVariant,
    val queryHgvs: String,
    val localPhaseSet: Int?, // grouping key for decomposed variants; null for direct
    val transvarVariant: TransvarVariant? = null,
    val paveResponse: PaveResponse? = null,
)

class PVA3(
    private val variantResolver: VariantAnnotator,
    private val paver: Paver,
    private val decompositions: VariantDecompositionIndex
) {

    fun annotate(sequencedVariants: Set<SequencedVariant>): List<Variant> {
        val expanded = applyDecompositions(sortSequencedVariantsForDeterminism(sequencedVariants))
        val withTransvar = annotateWithTransvar(expanded)
        val withPave = annotateWithPave(withTransvar)
        val dedupedAnnotated = deduplicateAnnotated(withPave)

        return createVariants(dedupedAnnotated)
    }

    private fun sortSequencedVariantsForDeterminism(variants: Set<SequencedVariant>): List<SequencedVariant> {
        val comparator =
            compareBy<SequencedVariant> { it.gene }
                .thenBy { it.transcript.orEmpty() }
                .thenBy { it.hgvsCodingImpact.orEmpty() }
                .thenBy { it.hgvsProteinImpact.orEmpty() }

        return variants.sortedWith(comparator)
    }

    fun applyDecompositions(sequencedVariants: List<SequencedVariant>): List<AnnotatableVariant> {
        val expanded = sequencedVariants.withIndex().flatMap { (sequencedVariantId, variant) ->
            val decomposedHgvsList =
                variant.hgvsCodingImpact?.let { decompositions.lookup(it) }?.decomposedCodingHgvs
                    ?: variant.hgvsProteinImpact?.let { decompositions.lookup(it) }?.decomposedCodingHgvs

            if (decomposedHgvsList != null) {
                decomposedHgvsList.map { decomposedHgvs ->
                    AnnotatableVariant(
                        queryId = UNASSIGNED_QUERY_ID,
                        sequencedVariant = variant,
                        queryHgvs = decomposedHgvs,
                        localPhaseSet = sequencedVariantId
                    )
                }
            } else {
                listOf(
                    AnnotatableVariant(
                        queryId = UNASSIGNED_QUERY_ID,
                        sequencedVariant = variant,
                        queryHgvs = variant.hgvsCodingOrProteinImpact(),
                        localPhaseSet = null
                    )
                )
            }
        }

        return expanded.mapIndexed { idx, variant ->
            variant.copy(queryId = idx)
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
                ?: throw IllegalStateException("Missing Transvar annotation for id ${response.queryId}")

            PaveQuery(
                id = response.queryId.toString(),
                chromosome = transvarVariant.chromosome(),
                position = transvarVariant.position(),
                ref = transvarVariant.ref(),
                alt = transvarVariant.alt(),
                localPhaseSet = response.localPhaseSet
            )
        }
        val responsesById = validatePaveResponses(queries, paver.run(queries))

        return transvarResponses.map { variant ->
            val paveResponse =
                responsesById[variant.queryId.toString()]
                    ?: throw IllegalStateException("Missing PAVE response for query ${variant.queryId}")

            variant.copy(paveResponse = paveResponse)
        }
    }

    private fun validatePaveResponses(
        queries: List<PaveQuery>,
        responses: List<PaveResponse>,
    ): Map<String, PaveResponse> {
        val responsesById = responses.associateBy { it.id }
        val expectedIds = queries.map { it.id }.toSet()

        if (responsesById.size != responses.size) {
            val duplicates = responses
                .groupBy { it.id }
                .filter { (_, values) -> values.size > 1 }
                .keys
                .sorted()
            throw IllegalStateException("PAVE returned duplicate responses for ids: ${duplicates.joinToString(", ")}")
        }

        if (responsesById.keys != expectedIds) {
            val missing = (expectedIds - responsesById.keys).sorted()
            val extra = (responsesById.keys - expectedIds).sorted()
            throw IllegalStateException(
                "PAVE returned unexpected set of response ids; missing=${missing.joinToString(", ")}, extra=${extra.joinToString(", ")}"
            )
        }

        return responsesById
    }

    fun deduplicateAnnotated(annotated: List<AnnotatableVariant>): List<AnnotatableVariant> {
        val (expanded, direct) = annotated.partition { it.localPhaseSet != null }

        val groupedByPhase: Map<Int?, List<AnnotatableVariant>> =
            expanded.groupBy { it.paveResponse?.localPhaseSet }
        val dedupedExpanded = groupedByPhase.entries.flatMap { entry ->
            val phaseSet = entry.key
            val variants = entry.value
            if (phaseSet == null) {
                variants
            } else {
                val proteinImpactsRaw = variants.map { it.paveResponse?.impact?.hgvsProteinImpact }
                if (proteinImpactsRaw.any { it.isNullOrBlank() }) {
                    throw IllegalStateException("Missing protein impact within phase set $phaseSet")
                }

                val proteinImpacts = proteinImpactsRaw.filterNotNull().toSet()
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
                ?: throw IllegalStateException("Missing Transvar annotation for id ${annotatedVariant.queryId}")
            val paveResponse = annotatedVariant.paveResponse
                ?: throw IllegalStateException("Missing PAVE response for id ${annotatedVariant.queryId}")

            val baseVariant = VariantFactory.createVariant(
                annotatedVariant.sequencedVariant,
                transvar,
                paveResponse
            )

            paveResponse.localPhaseSet?.let { phase -> baseVariant.copy(phaseGroups = setOf(phase)) } ?: baseVariant
        }
    }
}
