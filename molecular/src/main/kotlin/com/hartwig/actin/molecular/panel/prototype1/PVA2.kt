package com.hartwig.actin.molecular.panel.prototype1

import com.hartwig.actin.datamodel.clinical.SequencedVariant
import com.hartwig.actin.datamodel.molecular.driver.Variant
import com.hartwig.actin.molecular.paver.PaveQuery
import com.hartwig.actin.molecular.paver.PaveResponse
import com.hartwig.actin.molecular.paver.Paver
import com.hartwig.actin.tools.pave.PaveLite
import com.hartwig.actin.tools.variant.VariantAnnotator
import com.hartwig.actin.tools.variant.Variant as TransvarVariant

// Trying out revised dataflow for PanelVariantAnnotator

data class VariantId(val group: Int, val part: Int) {
    constructor(id: String) : this(group = id.substringBefore("-").toInt(), part = id.substringAfter("-").toInt())

    constructor(group: Int) : this(group = group, part = 0)

    override fun toString(): String {
        return "${group}-${part}"
    }
}

data class IdentifiedSequencedVariant(val variantId: VariantId, val sequencedVariant: SequencedVariant)

sealed class ExpandedVariant {
    data class Direct(val identifiedSequencedVariant: IdentifiedSequencedVariant) : ExpandedVariant()
    data class Decomposed(val identifiedSequencedVariant: IdentifiedSequencedVariant, val alternateHgvs: String) : ExpandedVariant()
}

data class AnotherExpandedVariant(val variantId: VariantId, val gene: String, val transcript: String?, val hgvs: String)

data class IdentifiedTransvarVariant(val variantId: VariantId, val transvarVariant: TransvarVariant)

data class IdentifiedPaveResponse(val variantId: VariantId, val paveResponses: PaveResponse)

class PVA2(private val variantResolver: VariantAnnotator, private val paver: Paver, private val paveLite: PaveLite, private val decompositions: com.hartwig.actin.molecular.panel.VariantDecompositionIndex) {
    fun annotate(sequencedVariants: Set<SequencedVariant>): List<Variant> {

        val identifiedSequencedVariants = identifySequencedVariants(sequencedVariants)
        val anotherDecomposed = anotherDecompose(identifiedSequencedVariants)
        val annotatedWithTransvar = annotateWithTransvar(anotherDecomposed)

        val paveAnnotations = annotateWithPave(annotatedWithTransvar)

//        val decomposed = decompose(identifiedSequencedVariants)
//        val identifiedTransvarVariants = annotateWithTransvar(decomposed)
//        val something = annotateWithPave(identifiedTransvarVariants)


        TODO()

    }

    fun decompose(identifiedSequencedVariants: List<IdentifiedSequencedVariant>): List<ExpandedVariant> {

        return identifiedSequencedVariants.flatMap { identifiedSequencedVariant ->
            val codingDecomposition = identifiedSequencedVariant.sequencedVariant.hgvsCodingImpact?.let { decompositions.lookup(it) }
            val proteinDecomposition = identifiedSequencedVariant.sequencedVariant.hgvsProteinImpact?.let { decompositions.lookup(it) }

            when {
                codingDecomposition != null -> {
                    codingDecomposition.decomposedCodingHgvs.map { decomposedHgvs ->
                        ExpandedVariant.Decomposed(identifiedSequencedVariant, decomposedHgvs)
                    }
                }

                proteinDecomposition != null -> {
                    proteinDecomposition.decomposedCodingHgvs.map { decomposedHgvs ->
                        ExpandedVariant.Decomposed(identifiedSequencedVariant, decomposedHgvs)
                    }
                }

                else -> listOf(ExpandedVariant.Direct(identifiedSequencedVariant))
            }
        }
    }

    fun anotherDecompose(identifiedSequencedVariants: List<IdentifiedSequencedVariant>): List<AnotherExpandedVariant> {
        return identifiedSequencedVariants.flatMap { identifiedSequencedVariant ->
            val codingDecomposition = identifiedSequencedVariant.sequencedVariant.hgvsCodingImpact?.let { decompositions.lookup(it) }
            val proteinDecomposition = identifiedSequencedVariant.sequencedVariant.hgvsProteinImpact?.let { decompositions.lookup(it) }

            when {
                codingDecomposition != null -> {
                    codingDecomposition.decomposedCodingHgvs.mapIndexed { index, decomposedHgvs ->
                        AnotherExpandedVariant(variantId = VariantId(identifiedSequencedVariant.variantId.group, index), gene = identifiedSequencedVariant.sequencedVariant.gene, transcript = identifiedSequencedVariant.sequencedVariant.transcript, hgvs = decomposedHgvs)
                    }
                }

                proteinDecomposition != null -> {
                    proteinDecomposition.decomposedCodingHgvs.mapIndexed { index, decomposedHgvs ->
                        AnotherExpandedVariant(variantId = VariantId(identifiedSequencedVariant.variantId.group, index), gene = identifiedSequencedVariant.sequencedVariant.gene, transcript = identifiedSequencedVariant.sequencedVariant.transcript, hgvs = decomposedHgvs)
                    }
                }

                else -> listOf(AnotherExpandedVariant(variantId = identifiedSequencedVariant.variantId, gene = identifiedSequencedVariant.sequencedVariant.gene, transcript = identifiedSequencedVariant.sequencedVariant.transcript, hgvs = identifiedSequencedVariant.sequencedVariant.hgvsCodingOrProteinImpact()))
            }
        }
    }

//    fun annotateWithTransvar(decomposed: List<ExpandedVariant>): List<IdentifiedTransvarVariant> {
//        return decomposed.map { expandedVariant ->
//            when (expandedVariant) {
//                is ExpandedVariant.Direct -> {
//                    val transvarVariant = resolveWithTransvar(
//                        expandedVariant.sequencedVariant.gene,
//                        expandedVariant.sequencedVariant.transcript,
//                        expandedVariant.sequencedVariant.hgvsCodingOrProteinImpact()
//                    )
//                    IdentifiedTransvarVariant(expandedVariant.sequencedVariant.variantId, transvarVariant)
//                }
//
//                is ExpandedVariant.Decomposed -> {
//                    val modifiedSequencedVariant = expandedVariant.sequencedVariant.copy(
//                        hgvsCodingImpact = expandedVariant.alternateHgvs
//                    )
//                    val transvarAnnotation = transvarAnnotation(modifiedSequencedVariant)
//                    IdentifiedTransvarVariant(
//                        VariantId(expandedVariant.sequencedVariant.hgvsCodingOrProteinImpact()),
//                        transvarAnnotation
//                    )
//                }
//            }
//        }
//    }

    fun annotateWithTransvar(x: List<AnotherExpandedVariant>): List<IdentifiedTransvarVariant> {
        return x.map { anotherExpandedVariant ->
            val transvarVariant = resolveWithTransvar(
                anotherExpandedVariant.gene,
                anotherExpandedVariant.transcript,
                anotherExpandedVariant.hgvs
            )
            IdentifiedTransvarVariant(anotherExpandedVariant.variantId, transvarVariant)
        }
    }

    private fun identifySequencedVariants(sequencedVariants: Set<SequencedVariant>): List<IdentifiedSequencedVariant> {
        return sequencedVariants.mapIndexed { index, variant ->
            IdentifiedSequencedVariant(VariantId(index), variant)
        }
    }

    private fun resolveWithTransvar(gene: String, transcript: String?, impact: String): TransvarVariant {
        val externalVariantAnnotation = variantResolver.resolve(gene, transcript, impact)

        return externalVariantAnnotation
            ?: throw IllegalStateException("Unable to resolve variant '$gene:$transcript:$impact' in variant annotator.")
    }

    fun annotateWithPave(identified: List<IdentifiedTransvarVariant>): List<PaveResponse> {
        if (identified.isEmpty()) {
            return emptyList()
        }

        val paveQueries = identified.map { identifiedTransvarVariant ->
            PaveQuery(
                id = identifiedTransvarVariant.variantId.toString(),
                chromosome = identifiedTransvarVariant.transvarVariant.chromosome(),
                position = identifiedTransvarVariant.transvarVariant.position(),
                ref = identifiedTransvarVariant.transvarVariant.ref(),
                alt = identifiedTransvarVariant.transvarVariant.alt(),
                localPhaseSet = identifiedTransvarVariant.variantId.group.takeIf { it > 0 }
            )
        }

        val paveResponses = paver.run(paveQueries)
        if (paveQueries.size != paveResponses.size) {
            throw IllegalStateException("Paver did not return a response for all queries")
        }

        return paveResponses
    }


}
