package com.hartwig.actin.molecular.panel

import org.assertj.core.api.Assertions
import org.junit.Test
import java.io.StringReader

class PaveVariantDecompositionTest {

    @Test
    fun `Should parse protein and coding decompositions trimming whitespace`() {
        val tsv = """
            variant	decomposition
            p.variant1	c.variant1a , c.variant1b
            p.variant2	 c.variant2a,c.variant2b 
        """.trimIndent()

        val parsed = PaveVariantDecomposition.read(StringReader(tsv))

        Assertions.assertThat(parsed).containsExactly(
            VariantDecomposition("p.variant1", listOf("c.variant1a", "c.variant1b")),
            VariantDecomposition("p.variant2", listOf("c.variant2a", "c.variant2b"))
        )
    }

    @Test
    fun `Should throw on duplicate protein hgvs entries`() {
        val tsv = """
            variant	decomposition
            p.variant1	c.variant1a,c.variant1b
            p.variant1	c.variant1c,c.variant1d
        """.trimIndent()

        val decompositions = PaveVariantDecomposition.read(StringReader(tsv))

        Assertions.assertThatThrownBy { VariantDecompositionIndex(decompositions) }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("p.variant1")
    }
}