package com.hartwig.actin.molecular.panel


import com.hartwig.actin.datamodel.clinical.SequencedVariant
import com.hartwig.actin.datamodel.molecular.driver.Variant
import com.hartwig.actin.molecular.paver.PaveQuery
import com.hartwig.actin.molecular.paver.PaveResponse
import com.hartwig.actin.molecular.paver.Paver
import com.hartwig.actin.tools.variant.VariantAnnotator
import com.hartwig.actin.tools.variant.Variant as TransvarVariant

data class AnnotatableVariant(
    val queryId: Int,
    val sequencedVariant: SequencedVariant,
    val queryHgvs: String,
    val localPhaseSet: Int?,
    val transvarVariant: TransvarVariant? = null,
    val paveResponse: PaveResponse? = null,
)

class PanelVariantAnnotator(
    private val variantResolver: VariantAnnotator,
    private val paver: Paver,
    private val decompositions: VariantDecompositionIndex
) {

    fun annotate(sequencedVariants: Set<SequencedVariant>): List<Variant> {
        val expanded = VariantDecompositionExpander.expand(sequencedVariants, decompositions)
        val withTransvar = annotateWithTransvar(expanded)
        val withPave = annotateWithPave(withTransvar)
        val collapsed = collapseDecompositions(withPave)
        return createVariants(collapsed)
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
        val duplicates = responses
            .groupBy { it.id }
            .filter { (_, values) -> values.size > 1 }
            .keys
            .sorted()
        val expectedPhaseById = queries
            .filter { it.localPhaseSet != null }
            .associate { it.id to it.localPhaseSet }
        val mismatchedPhaseIds = expectedPhaseById.mapNotNull { (id, expected) ->
            val actual = responsesById[id]?.localPhaseSet
            if (actual == null || actual != expected) id else null
        }

        when {
            duplicates.isNotEmpty() -> {
                throw IllegalStateException("PAVE returned duplicate responses for ids: ${duplicates.joinToString(", ")}")
            }

            responsesById.keys != expectedIds -> {
                val missing = (expectedIds - responsesById.keys).sorted()
                val extra = (responsesById.keys - expectedIds).sorted()
                throw IllegalStateException(
                    "PAVE returned unexpected set of response ids; missing=${missing.joinToString(", ")}, extra=${extra.joinToString(", ")}"
                )
            }

            mismatchedPhaseIds.isNotEmpty() -> {
                throw IllegalStateException(
                    "Missing or mismatched localPhaseSet for responses: ${mismatchedPhaseIds.sorted().joinToString(", ")}"
                )
            }
        }

        return responsesById
    }

    fun collapseDecompositions(annotated: List<AnnotatableVariant>): List<AnnotatableVariant> {
        val (expanded, direct) = annotated.partition { it.localPhaseSet != null }

        val groupedByPhase: Map<Int?, List<AnnotatableVariant>> =
            expanded.groupBy { it.paveResponse?.localPhaseSet }
        val dedupedExpanded = groupedByPhase.entries.flatMap { entry ->
            val phaseSet = entry.key
            val variants = entry.value
            if (phaseSet == null) {
                variants
            } else {
                listOf(PhasedDecompositionCollapser.collapse(phaseSet, variants, variantResolver))
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
            val adjustedPaveResponse = adjustPaveResponseForOriginalCodingHgvs(annotatedVariant, paveResponse)

            val baseVariant = VariantFactory.createVariant(
                annotatedVariant.sequencedVariant,
                transvar,
                adjustedPaveResponse
            )

            // TODO do we need phase group here for the decomposed variants? this is not a true phased variant from upstream wgs
            adjustedPaveResponse.localPhaseSet?.let { phase -> baseVariant.copy(phaseGroups = setOf(phase)) } ?: baseVariant
        }
    }

    private fun adjustPaveResponseForOriginalCodingHgvs(
        annotatedVariant: AnnotatableVariant,
        paveResponse: PaveResponse
    ): PaveResponse {
        val originalCodingHgvs = annotatedVariant.sequencedVariant.hgvsCodingImpact?.trim()
        return if (annotatedVariant.localPhaseSet != null && !originalCodingHgvs.isNullOrEmpty()) {
            paveResponse.copy(
                impact = paveResponse.impact.copy(hgvsCodingImpact = originalCodingHgvs)
            )
        } else {
            paveResponse
        }
    }
}
