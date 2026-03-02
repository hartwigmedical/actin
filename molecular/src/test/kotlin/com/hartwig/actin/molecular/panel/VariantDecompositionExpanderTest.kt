package com.hartwig.actin.molecular.panel

import com.hartwig.actin.datamodel.clinical.SequencedVariant
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class VariantDecompositionExpanderTest {

    @Test
    fun `Should expand decomposed variants and assign phase sets deterministically`() {
        val directVariant = SequencedVariant(gene = "A", transcript = null, hgvsCodingImpact = "c.1A>T")
        val decomposedVariant = SequencedVariant(gene = "B", transcript = null, hgvsCodingImpact = "c.2A>T")
        val otherVariant = SequencedVariant(gene = "C", transcript = null, hgvsCodingImpact = "c.3A>T")

        val decompositions = VariantDecompositionTable(
            listOf(
                VariantDecomposition(
                    gene = "B",
                    transcript = null,
                    originalCodingHgvs = "c.2A>T",
                    decomposedCodingHgvs = listOf("c.2A>G", "c.3_4delinsA")
                )
            )
        )
        val expander = VariantDecompositionExpander(decompositions)

        val expanded = expander.expand(setOf(decomposedVariant, directVariant, otherVariant))

        val direct = expanded.single { it.queryHgvs == "c.1A>T" }
        assertThat(direct.localPhaseSet).isNull()

        val decomposed = expanded.filter { it.queryHgvs in setOf("c.2A>G", "c.3_4delinsA") }
        assertThat(decomposed).hasSize(2)
        assertThat(decomposed.map { it.localPhaseSet }.toSet()).containsExactly(1)

        val other = expanded.single { it.queryHgvs == "c.3A>T" }
        assertThat(other.localPhaseSet).isNull()
    }

    @Test
    fun `Should not decompose when coding hgvs is missing`() {
        val variant = SequencedVariant(gene = "B", transcript = null, hgvsCodingImpact = null, hgvsProteinImpact = "p.V34E")
        val decompositions = VariantDecompositionTable(
            listOf(
                VariantDecomposition(
                    gene = "B",
                    transcript = null,
                    originalCodingHgvs = "c.2A>T",
                    decomposedCodingHgvs = listOf("c.2A>G", "c.3_4delinsA")
                )
            )
        )
        val expander = VariantDecompositionExpander(decompositions)

        val expanded = expander.expand(setOf(variant))

        assertThat(expanded).hasSize(1)
        assertThat(expanded.single().queryHgvs).isEqualTo("p.V34E")
        assertThat(expanded.single().localPhaseSet).isNull()
    }

    @Test
    fun `Should not decompose when transcript does not match`() {
        val variant = SequencedVariant(gene = "B", transcript = "TX1", hgvsCodingImpact = "c.2A>T")
        val decompositions = VariantDecompositionTable(
            listOf(
                VariantDecomposition(
                    gene = "B",
                    transcript = null,
                    originalCodingHgvs = "c.2A>T",
                    decomposedCodingHgvs = listOf("c.2A>G", "c.3_4delinsA")
                )
            )
        )
        val expander = VariantDecompositionExpander(decompositions)

        val expanded = expander.expand(setOf(variant))

        assertThat(expanded).hasSize(1)
        assertThat(expanded.single().queryHgvs).isEqualTo("c.2A>T")
        assertThat(expanded.single().localPhaseSet).isNull()
    }

    @Test
    fun `Should not decompose when transcript is missing but decomposition is transcript-specific`() {
        val variant = SequencedVariant(gene = "B", transcript = null, hgvsCodingImpact = "c.2A>T")
        val decompositions = VariantDecompositionTable(
            listOf(
                VariantDecomposition(
                    gene = "B",
                    transcript = "TX1",
                    originalCodingHgvs = "c.2A>T",
                    decomposedCodingHgvs = listOf("c.2A>G", "c.3_4delinsA")
                )
            )
        )
        val expander = VariantDecompositionExpander(decompositions)

        val expanded = expander.expand(setOf(variant))

        assertThat(expanded).hasSize(1)
        assertThat(expanded.single().queryHgvs).isEqualTo("c.2A>T")
        assertThat(expanded.single().localPhaseSet).isNull()
    }
}
