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

data class IdentifiedSequencedVariant(val sequencedVariantId: Int, val sequencedVariant: SequencedVariant)

data class TransvarQuery(
    val variantIndex: VariantIndex,
    val isExpanded: Boolean,
    val gene: String,
    val transcript: String?,
    val hgvs: String
)

data class TransvarResponse(
    val variantIndex: VariantIndex,
    val isExpanded: Boolean,
    val transvarVariant: TransvarVariant,
    val gene: String,
    val transcript: String?,
    val hgvs: String
)

data class AnnotatedVariant(
    val variantIndex: VariantIndex,
    val isExpanded: Boolean,
    val transvarVariant: TransvarVariant,
    val paveResponse: PaveResponse
)

class PVA3(
    private val variantResolver: VariantAnnotator,
    private val paver: Paver,
    private val paveLite: PaveLite,
    private val decompositions: VariantDecompositionIndex
) {

    private val variantFactory = PanelAnnotatorVariantFactory(paveLite)

    fun annotate(sequencedVariants: Set<SequencedVariant>): List<Variant> {
        val identifiedSequencedVariants = sequencedVariants.mapIndexed { index, sequencedVariant ->
            IdentifiedSequencedVariant(sequencedVariantId = index, sequencedVariant = sequencedVariant)
        }

        val transvarQueries = applyDecompositions(identifiedSequencedVariants)
        val transvarResponses = annotateWithTransvar(transvarQueries)
        val paveResponses = annotateWithPave(transvarResponses)
        val annotated = transvarResponses.zip(paveResponses) { tr, pr ->
            AnnotatedVariant(
                variantIndex = tr.variantIndex,
                isExpanded = tr.isExpanded,
                transvarVariant = tr.transvarVariant,
                paveResponse = pr
            )
        }
        val dedupedAnnotated = deduplicateAnnotated(annotated)

        return createVariants(dedupedAnnotated, identifiedSequencedVariants)
    }

    fun applyDecompositions(identifiedSequencedVariants: List<IdentifiedSequencedVariant>): List<TransvarQuery> {
        val expanded = identifiedSequencedVariants.flatMap { identified ->
            val variant = identified.sequencedVariant
            val decomposedHgvsList =
                variant.hgvsCodingImpact?.let { decompositions.lookup(it) }?.decomposedCodingHgvs
                    ?: variant.hgvsProteinImpact?.let { decompositions.lookup(it) }?.decomposedCodingHgvs

            if (decomposedHgvsList != null) {
                decomposedHgvsList.map { decomposedHgvs ->
                    TransvarQuery(
                        variantIndex = VariantIndex(-1, identified.sequencedVariantId), // placeholder, set below
                        isExpanded = true,
                        gene = variant.gene,
                        transcript = variant.transcript,
                        hgvs = decomposedHgvs
                    )
                }
            } else {
                listOf(
                    TransvarQuery(
                        variantIndex = VariantIndex(-1, identified.sequencedVariantId), // placeholder, set below
                        isExpanded = false,
                        gene = variant.gene,
                        transcript = variant.transcript,
                        hgvs = variant.hgvsCodingOrProteinImpact()
                    )
                )
            }
        }

        return expanded.mapIndexed { idx, query ->
            query.copy(variantIndex = VariantIndex(idx, query.variantIndex.sequencedVariantId))
        }
    }

    fun annotateWithTransvar(transvarQueries: List<TransvarQuery>): List<TransvarResponse> {
        return transvarQueries.map { transvarQuery ->
            val transvar = variantResolver.resolve(transvarQuery.gene, transvarQuery.transcript, transvarQuery.hgvs)
                ?: throw IllegalStateException("Unable to resolve variant '${transvarQuery.hgvs}' for gene '${transvarQuery.gene}'")

            TransvarResponse(
                variantIndex = transvarQuery.variantIndex,
                isExpanded = transvarQuery.isExpanded,
                transvarVariant = transvar,
                gene = transvarQuery.gene,
                transcript = transvarQuery.transcript,
                hgvs = transvarQuery.hgvs
            )
        }
    }

    fun annotateWithPave(
        transvarResponses: List<TransvarResponse>
    ): List<PaveResponse> {
        if (transvarResponses.isEmpty()) {
            return emptyList()
        }

        val localPhaseSets =
            transvarResponses.filter { it.isExpanded }.map { it.variantIndex.sequencedVariantId }.distinct()
                .withIndex()
                .associate { (idx, sequencedId) -> sequencedId to idx + 1 }

        val queries = transvarResponses.map { response ->
            PaveQuery(
                id = response.variantIndex.id.toString(),
                chromosome = response.transvarVariant.chromosome(),
                position = response.transvarVariant.position(),
                ref = response.transvarVariant.ref(),
                alt = response.transvarVariant.alt(),
                localPhaseSet = if (response.isExpanded) localPhaseSets[response.variantIndex.sequencedVariantId] else null
            )
        }
        val responses = paver.run(queries)
        val responsesById = responses.associateBy { it.id }
        require(responsesById.keys == queries.map { it.id }.toSet()) { "PAVE did not return responses for all queries" }

        return queries.map { query ->
            responsesById[query.id] ?: throw IllegalStateException("Missing PAVE response for query ${query.id}")
        }
    }

    fun deduplicateAnnotated(annotated: List<AnnotatedVariant>): List<AnnotatedVariant> {
        val (expanded, direct) = annotated.partition { it.isExpanded }

        val groupedByPhase: Map<Int?, List<AnnotatedVariant>> = expanded.groupBy { it.paveResponse.localPhaseSet }
        val dedupedExpanded = groupedByPhase.entries.flatMap { entry ->
            val phaseSet = entry.key
            val variants = entry.value
            if (phaseSet == null) {
                variants
            } else {
                val proteinImpacts = variants.map { it.paveResponse.impact.hgvsProteinImpact }.toSet()
                require(proteinImpacts.size <= 1) {
                    "Mismatched protein impacts within phase set $phaseSet: $proteinImpacts"
                }
                variants.take(1)
            }
        }

        return direct + dedupedExpanded
    }

    private fun createVariants(
        dedupedAnnotated: List<AnnotatedVariant>,
        identifiedSequencedVariants: List<IdentifiedSequencedVariant>
    ): List<Variant> {
        val sequencedById = identifiedSequencedVariants.associateBy { it.sequencedVariantId }

        return dedupedAnnotated.map { annotatedVariant ->
            val sequencedVariant = sequencedById[annotatedVariant.variantIndex.sequencedVariantId]
                ?: throw IllegalStateException("Missing sequenced variant for id ${annotatedVariant.variantIndex.sequencedVariantId}")

            val baseVariant = variantFactory.createVariant(
                sequencedVariant.sequencedVariant,
                annotatedVariant.transvarVariant,
                annotatedVariant.paveResponse
            )

            annotatedVariant.paveResponse.localPhaseSet?.let { phase -> baseVariant.copy(phaseGroups = setOf(phase)) }
                ?: baseVariant
        }
    }
}
