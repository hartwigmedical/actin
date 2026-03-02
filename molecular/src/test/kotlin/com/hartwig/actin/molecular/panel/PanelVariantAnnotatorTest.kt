package com.hartwig.actin.molecular.panel

import com.hartwig.actin.datamodel.clinical.SequencedVariant
import com.hartwig.actin.datamodel.molecular.driver.VariantEffect
import com.hartwig.actin.molecular.paver.PaveCodingEffect
import com.hartwig.actin.molecular.paver.PaveImpact
import com.hartwig.actin.molecular.paver.PaveQuery
import com.hartwig.actin.molecular.paver.PaveResponse
import com.hartwig.actin.molecular.paver.PaveTranscriptImpact
import com.hartwig.actin.molecular.paver.PaveVariantEffect
import com.hartwig.actin.molecular.paver.Paver
import com.hartwig.actin.tools.variant.ImmutableVariant
import com.hartwig.actin.tools.variant.VariantAnnotator
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test

class PanelVariantAnnotatorTest {

    private val variantResolver = mockk<VariantAnnotator>()
    private val paver = mockk<Paver>()

    @Test
    fun `Should not run PAVE when no variants`() {
        val annotator = PanelVariantAnnotator(variantResolver, paver, VariantDecompositionTable(emptyList()))

        val result = annotator.annotate(emptySet())

        assertThat(result).isEmpty()
        verify(exactly = 0) { variantResolver.resolve(any(), any(), any()) }
        verify(exactly = 0) { paver.run(any<List<PaveQuery>>()) }
    }

    @Test
    fun `Should throw exception on null output from transcript annotator`() {
        val variant = SequencedVariant(gene = "EGFR", transcript = null, hgvsCodingImpact = "c.1A>T")
        every { variantResolver.resolve("EGFR", null, "c.1A>T") } returns null

        val annotator = PanelVariantAnnotator(variantResolver, paver, VariantDecompositionTable(emptyList()))

        assertThatThrownBy { annotator.annotate(setOf(variant)) }
            .isInstanceOf(IllegalStateException::class.java)

        verify(exactly = 0) { paver.run(any<List<PaveQuery>>()) }
    }

    @Test
    fun `Should add local phase set for decomposed variants only`() {
        val directVariant = SequencedVariant(gene = "A", transcript = null, hgvsCodingImpact = "c.1A>T")
        val decomposedVariant = SequencedVariant(gene = "B", transcript = null, hgvsCodingImpact = "c.2A>T")

        val decompositions = VariantDecompositionTable(
            listOf(
                VariantDecomposition(
                    gene = "B",
                    transcript = null,
                    originalCodingHgvs = "c.2A>T",
                    decomposedCodingHgvs = listOf("c.2A>G", "c.3_4delinsA"),
                )
            )
        )

        every { variantResolver.resolve(any(), any(), any()) } answers {
            val hgvs = thirdArg<String>()
            when (hgvs) {
                "c.1A>T" -> transvarVariant(chromosome = "7", position = 1, ref = "A", alt = "T")
                "c.2A>G" -> transvarVariant(chromosome = "7", position = 2, ref = "A", alt = "G")
                "c.3_4delinsA" -> transvarVariant(chromosome = "7", position = 3, ref = "AT", alt = "A")
                else -> null
            }
        }

        val capturedQueries = mutableListOf<List<PaveQuery>>()
        every { paver.run(any<List<PaveQuery>>()) } answers {
            val queries = firstArg<List<PaveQuery>>()
            capturedQueries.add(queries)

            queries.map { query ->
                paveResponse(
                    id = query.id,
                    gene = "GENE",
                    canonicalTranscript = "TX",
                    hgvsCodingImpact = "c.mock",
                    hgvsProteinImpact = "p.M1L",
                    localPhaseSet = query.localPhaseSet
                )
            }
        }

        val annotator = PanelVariantAnnotator(variantResolver, paver, decompositions)

        val result = annotator.annotate(setOf(decomposedVariant, directVariant))

        assertThat(result).hasSize(2)
        verify(exactly = 1) { paver.run(any<List<PaveQuery>>()) }

        val queries = capturedQueries.single()
        assertThat(queries).hasSize(3)

        val directQuery = queries.single { it.ref == "A" && it.alt == "T" }
        assertThat(directQuery.localPhaseSet).isNull()

        val decomposedQueries = queries.filter { it.id != directQuery.id }
        assertThat(decomposedQueries.map { it.localPhaseSet }.toSet()).containsExactly(1)

        assertThat(result.all { it.phaseGroups == null }).isTrue()
        assertThat(result.single { it.gene == "B" }.canonicalImpact.hgvsCodingImpact).isEqualTo("c.2A>T")
    }

    @Test
    fun `Should use original transvar coordinates for decomposed variants`() {
        val decomposedVariant = SequencedVariant(gene = "B", transcript = null, hgvsCodingImpact = "c.2A>T")
        val decompositions = VariantDecompositionTable(
            listOf(
                VariantDecomposition(
                    gene = "B",
                    transcript = null,
                    originalCodingHgvs = "c.2A>T",
                    decomposedCodingHgvs = listOf("c.2A>G", "c.3_4delinsA"),
                )
            )
        )

        every { variantResolver.resolve(any(), any(), any()) } answers {
            val hgvs = thirdArg<String>()
            when (hgvs) {
                "c.2A>G" -> transvarVariant(chromosome = "7", position = 2, ref = "A", alt = "G")
                "c.3_4delinsA" -> transvarVariant(chromosome = "7", position = 3, ref = "AT", alt = "A")
                "c.2A>T" -> transvarVariant(chromosome = "7", position = 99, ref = "C", alt = "T")
                else -> null
            }
        }

        every { paver.run(any<List<PaveQuery>>()) } answers {
            val queries = firstArg<List<PaveQuery>>()
            queries.map { query ->
                paveResponse(
                    id = query.id,
                    gene = "GENE",
                    canonicalTranscript = "TX",
                    hgvsCodingImpact = "c.mock",
                    hgvsProteinImpact = "p.M1L",
                    localPhaseSet = query.localPhaseSet
                )
            }
        }

        val annotator = PanelVariantAnnotator(variantResolver, paver, decompositions)

        val result = annotator.annotate(setOf(decomposedVariant))

        assertThat(result).hasSize(1)
        val variant = result.single()
        assertThat(variant.position).isEqualTo(99)
        assertThat(variant.ref).isEqualTo("C")
        assertThat(variant.alt).isEqualTo("T")
    }

    @Test
    fun `Should not decompose variants when hgvsCodingImpact is null`() {
        val decomposedVariant = SequencedVariant(gene = "B", transcript = null, hgvsCodingImpact = null, hgvsProteinImpact = "p.V34E")
        val decompositions = VariantDecompositionTable(
            listOf(
                VariantDecomposition(
                    gene = "B",
                    transcript = null,
                    originalCodingHgvs = "c.2A>T",
                    decomposedCodingHgvs = listOf("c.2A>G", "c.3_4delinsA"),
                )
            )
        )

        every { variantResolver.resolve(any(), any(), any()) } answers {
            val hgvs = thirdArg<String>()
            when (hgvs) {
                "p.V34E" -> transvarVariant(chromosome = "7", position = 2, ref = "A", alt = "G")
                else -> null
            }
        }

        val capturedQueries = mutableListOf<List<PaveQuery>>()
        every { paver.run(any<List<PaveQuery>>()) } answers {
            val queries = firstArg<List<PaveQuery>>()
            capturedQueries.add(queries)
            queries.map { query ->
                paveResponse(
                    id = query.id,
                    gene = "GENE",
                    canonicalTranscript = "TX",
                    hgvsCodingImpact = "c.mock",
                    hgvsProteinImpact = "p.M1L",
                    localPhaseSet = query.localPhaseSet
                )
            }
        }

        val annotator = PanelVariantAnnotator(variantResolver, paver, decompositions)
        val result = annotator.annotate(setOf(decomposedVariant))

        assertThat(result).hasSize(1)
        val queries = capturedQueries.single()
        assertThat(queries).hasSize(1)
        assertThat(queries.single().localPhaseSet).isNull()
    }

    @Test
    fun `Should decompose variants using hgvsCodingImpact when hgvsProteinImpact is null`() {
        val decomposedVariant = SequencedVariant(gene = "B", transcript = null, hgvsCodingImpact = "c.2A>T", hgvsProteinImpact = null)
        val decompositions = VariantDecompositionTable(
            listOf(
                VariantDecomposition(
                    gene = "B",
                    transcript = null,
                    originalCodingHgvs = "c.2A>T",
                    decomposedCodingHgvs = listOf("c.2A>G", "c.3_4delinsA"),
                )
            )
        )

        every { variantResolver.resolve(any(), any(), any()) } answers {
            val hgvs = thirdArg<String>()
            when (hgvs) {
                "c.2A>G" -> transvarVariant(chromosome = "7", position = 2, ref = "A", alt = "G")
                "c.3_4delinsA" -> transvarVariant(chromosome = "7", position = 3, ref = "AT", alt = "A")
                else -> null
            }
        }

        val capturedQueries = mutableListOf<List<PaveQuery>>()
        every { paver.run(any<List<PaveQuery>>()) } answers {
            val queries = firstArg<List<PaveQuery>>()
            capturedQueries.add(queries)
            queries.map { query ->
                paveResponse(
                    id = query.id,
                    gene = "GENE",
                    canonicalTranscript = "TX",
                    hgvsCodingImpact = "c.mock",
                    hgvsProteinImpact = "p.M1L",
                    localPhaseSet = query.localPhaseSet
                )
            }
        }

        val annotator = PanelVariantAnnotator(variantResolver, paver, decompositions)
        val result = annotator.annotate(setOf(decomposedVariant))

        assertThat(result).hasSize(1)
        val queries = capturedQueries.single()
        assertThat(queries).hasSize(2)
        assertThat(queries.map { it.localPhaseSet }.toSet()).containsExactly(0)
        assertThat(queries.map { it.position }.toSet()).containsExactlyInAnyOrder(2, 3)
    }

    @Test
    fun `Should normalize phased effects after selecting representative phased response`() {
        val decomposedVariant = SequencedVariant(gene = "B", transcript = null, hgvsCodingImpact = "c.2A>T")
        val decompositions = VariantDecompositionTable(
            listOf(
                VariantDecomposition(
                    gene = "B",
                    transcript = null,
                    originalCodingHgvs = "c.2A>T",
                    decomposedCodingHgvs = listOf("c.2A>G", "c.3_4delinsA"),
                )
            )
        )

        every { variantResolver.resolve(any(), any(), any()) } answers {
            val hgvs = thirdArg<String>()
            when (hgvs) {
                "c.2A>G" -> transvarVariant(chromosome = "7", position = 2, ref = "A", alt = "G")
                "c.3_4delinsA" -> transvarVariant(chromosome = "7", position = 3, ref = "AT", alt = "A")
                else -> null
            }
        }

        every { paver.run(any<List<PaveQuery>>()) } answers {
            val queries = firstArg<List<PaveQuery>>()
            queries.map { query ->
                val impact = PaveImpact(
                    gene = "GENE",
                    canonicalTranscript = "TX",
                    canonicalEffects = listOf(PaveVariantEffect.PHASED_INFRAME_DELETION),
                    canonicalCodingEffect = PaveCodingEffect.MISSENSE,
                    spliceRegion = false,
                    hgvsCodingImpact = "c.mock",
                    hgvsProteinImpact = "p.M1L",
                    otherReportableEffects = null,
                    worstCodingEffect = PaveCodingEffect.MISSENSE,
                    genesAffected = 1
                )
                val transcriptImpact = PaveTranscriptImpact(
                    geneId = "gene_id",
                    gene = "GENE",
                    transcript = "TX",
                    effects = listOf(PaveVariantEffect.PHASED_INFRAME_DELETION),
                    spliceRegion = false,
                    hgvsCodingImpact = "c.mock",
                    hgvsProteinImpact = "p.M1L",
                    refSeqId = "refseq",
                    exon = 1,
                    codon = 1,
                )
                PaveResponse(
                    id = query.id,
                    impact = impact,
                    transcriptImpacts = listOf(transcriptImpact),
                    localPhaseSet = query.localPhaseSet
                )
            }
        }

        val annotator = PanelVariantAnnotator(variantResolver, paver, decompositions)

        val result = annotator.annotate(setOf(decomposedVariant))

        assertThat(result).hasSize(1)
        assertThat(result.single().canonicalImpact.effects).contains(VariantEffect.INFRAME_DELETION)
        assertThat(result.single().canonicalImpact.effects).doesNotContain(VariantEffect.PHASED_INFRAME_DELETION)
    }

    @Test
    fun `Should throw on mismatched protein impacts within a local phase set`() {
        val decomposedVariant = SequencedVariant(gene = "B", transcript = null, hgvsCodingImpact = "c.2A>T")
        val decompositions = VariantDecompositionTable(
            listOf(
                VariantDecomposition(
                    gene = "B",
                    transcript = null,
                    originalCodingHgvs = "c.2A>T",
                    decomposedCodingHgvs = listOf("c.2A>G", "c.3_4delinsA"),
                )
            )
        )

        every { variantResolver.resolve(any(), any(), any()) } answers {
            val hgvs = thirdArg<String>()
            when (hgvs) {
                "c.2A>G" -> transvarVariant(chromosome = "7", position = 2, ref = "A", alt = "G")
                "c.3_4delinsA" -> transvarVariant(chromosome = "7", position = 3, ref = "AT", alt = "A")
                else -> null
            }
        }

        every { paver.run(any<List<PaveQuery>>()) } answers {
            val queries = firstArg<List<PaveQuery>>()

            queries.map { query ->
                val proteinImpact = if (query.ref == "A") "p.M1L" else "p.V2E"
                paveResponse(
                    id = query.id,
                    gene = "GENE",
                    canonicalTranscript = "TX",
                    hgvsCodingImpact = "c.mock",
                    hgvsProteinImpact = proteinImpact,
                    localPhaseSet = query.localPhaseSet
                )
            }
        }

        val annotator = PanelVariantAnnotator(variantResolver, paver, decompositions)

        assertThatThrownBy { annotator.annotate(setOf(decomposedVariant)) }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("Mismatched protein impacts within phase set")
    }

    @Test
    fun `Should throw when protein impact is missing within a local phase set`() {
        val decomposedVariant = SequencedVariant(gene = "B", transcript = null, hgvsCodingImpact = "c.2A>T")
        val decompositions = VariantDecompositionTable(
            listOf(
                VariantDecomposition(
                    gene = "B",
                    transcript = null,
                    originalCodingHgvs = "c.2A>T",
                    decomposedCodingHgvs = listOf("c.2A>G", "c.3_4delinsA"),
                )
            )
        )

        every { variantResolver.resolve(any(), any(), any()) } answers {
            val hgvs = thirdArg<String>()
            when (hgvs) {
                "c.2A>G" -> transvarVariant(chromosome = "7", position = 2, ref = "A", alt = "G")
                "c.3_4delinsA" -> transvarVariant(chromosome = "7", position = 3, ref = "AT", alt = "A")
                else -> null
            }
        }

        every { paver.run(any<List<PaveQuery>>()) } answers {
            val queries = firstArg<List<PaveQuery>>()
            queries.map { query ->
                val proteinImpact = if (query.ref == "A") "" else "p.V2E"
                paveResponse(
                    id = query.id,
                    gene = "GENE",
                    canonicalTranscript = "TX",
                    hgvsCodingImpact = "c.mock",
                    hgvsProteinImpact = proteinImpact,
                    localPhaseSet = query.localPhaseSet
                )
            }
        }

        val annotator = PanelVariantAnnotator(variantResolver, paver, decompositions)

        assertThatThrownBy { annotator.annotate(setOf(decomposedVariant)) }
            .isInstanceOf(IllegalStateException::class.java)
            .hasMessageContaining("Missing protein impact within phase set")
    }

    @Test
    fun `Should throw when PAVE does not return responses for all queries`() {
        val variant = SequencedVariant(gene = "EGFR", transcript = null, hgvsCodingImpact = "c.1A>T")
        every { variantResolver.resolve("EGFR", null, "c.1A>T") } returns transvarVariant(chromosome = "7", position = 1, ref = "A", alt = "T")
        every { paver.run(any<List<PaveQuery>>()) } returns emptyList()

        val annotator = PanelVariantAnnotator(variantResolver, paver, VariantDecompositionTable(emptyList()))

        assertThatThrownBy { annotator.annotate(setOf(variant)) }
            .isInstanceOf(IllegalStateException::class.java)
            .hasMessageContaining("PAVE returned unexpected set of response ids")
    }

    @Test
    fun `Should throw when PAVE returns duplicate response ids`() {
        val variant = SequencedVariant(gene = "EGFR", transcript = null, hgvsCodingImpact = "c.1A>T")
        every { variantResolver.resolve("EGFR", null, "c.1A>T") } returns transvarVariant(chromosome = "7", position = 1, ref = "A", alt = "T")
        every { paver.run(any<List<PaveQuery>>()) } returns listOf(
            paveResponse(
                id = "0",
                gene = "GENE",
                canonicalTranscript = "TX",
                hgvsCodingImpact = "c.mock",
                hgvsProteinImpact = "p.M1L",
                localPhaseSet = null
            ),
            paveResponse(
                id = "0",
                gene = "GENE",
                canonicalTranscript = "TX",
                hgvsCodingImpact = "c.mock",
                hgvsProteinImpact = "p.M1L",
                localPhaseSet = null
            ),
        )

        val annotator = PanelVariantAnnotator(variantResolver, paver, VariantDecompositionTable(emptyList()))

        assertThatThrownBy { annotator.annotate(setOf(variant)) }
            .isInstanceOf(IllegalStateException::class.java)
            .hasMessageContaining("PAVE returned duplicate responses for ids")
    }

    private fun transvarVariant(chromosome: String, position: Int, ref: String, alt: String) =
        ImmutableVariant.builder()
            .chromosome(chromosome)
            .position(position)
            .ref(ref)
            .alt(alt)
            .build()

    private fun paveResponse(
        id: String,
        gene: String,
        canonicalTranscript: String,
        hgvsCodingImpact: String,
        hgvsProteinImpact: String,
        localPhaseSet: Int?
    ): PaveResponse {
        val canonicalTranscriptImpact = PaveTranscriptImpact(
            geneId = "gene_id",
            gene = gene,
            transcript = canonicalTranscript,
            effects = listOf(PaveVariantEffect.MISSENSE),
            spliceRegion = false,
            hgvsCodingImpact = hgvsCodingImpact,
            hgvsProteinImpact = hgvsProteinImpact,
            refSeqId = "refseq",
            exon = 1,
            codon = 1,
        )

        val impact = PaveImpact(
            gene = gene,
            canonicalTranscript = canonicalTranscript,
            canonicalEffects = listOf(PaveVariantEffect.MISSENSE),
            canonicalCodingEffect = PaveCodingEffect.MISSENSE,
            spliceRegion = false,
            hgvsCodingImpact = hgvsCodingImpact,
            hgvsProteinImpact = hgvsProteinImpact,
            otherReportableEffects = null,
            worstCodingEffect = PaveCodingEffect.MISSENSE,
            genesAffected = 1
        )

        return PaveResponse(
            id = id,
            impact = impact,
            transcriptImpacts = listOf(canonicalTranscriptImpact),
            localPhaseSet = localPhaseSet
        )
    }
}
