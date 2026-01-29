package com.hartwig.actin.molecular.panel

import org.assertj.core.api.Assertions
import org.junit.Test
import java.io.StringReader

class PaveVariantDecompositionTest {

    @Test
    fun `Should parse coding decompositions trimming whitespace`() {
        val tsv = """
            gene	transcript	variant	decomposition
            gene1	ENST1	c.variant1	c.variant1a , c.variant1b
            gene2		c.variant2	 c.variant2a,c.variant2b 
        """.trimIndent()

        val parsed = PaveVariantDecomposition.read(StringReader(tsv))

        Assertions.assertThat(parsed).containsExactly(
            VariantDecomposition("gene1", "ENST1", "c.variant1", listOf("c.variant1a", "c.variant1b")),
            VariantDecomposition("gene2", null, "c.variant2", listOf("c.variant2a", "c.variant2b"))
        )
    }

    @Test
    fun `Should throw on duplicate coding hgvs entries`() {
        val tsv = """
            gene	transcript	variant	decomposition
            GENE1	ENST1	c.variant1	c.variant1a,c.variant1b
            GENE1	ENST1	c.variant1	c.variant1c,c.variant1d
        """.trimIndent()

        val decompositions = PaveVariantDecomposition.read(StringReader(tsv))

        Assertions.assertThatThrownBy { VariantDecompositionTable(decompositions) }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("GENE1:ENST1:c.variant1")
    }
}
