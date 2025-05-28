package com.hartwig.actin.datamodel.clinical.treatment

import com.hartwig.actin.datamodel.clinical.SequencedFusion
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class SequencingTestTest {

    private val fusion = SequencedFusion(geneUp = "geneUp", exonUp = 5, geneDown = "geneDown", exonDown = 10)

    @Test
    fun `Should display fusions correctly with exon if exons known`() {
        assertThat(fusion.display()).isEqualTo("geneUp exon 5 :: geneDown exon 10 fusion")
    }

    @Test
    fun `Should display fusions correctly without exon if exons not known`() {
        val fusion = fusion.copy(exonUp = null, exonDown = null)
        assertThat(fusion.display()).isEqualTo("geneUp :: geneDown fusion")
    }

    @Test
    fun `Should display fusions correctly if only geneUp and exonUp known`() {
        val fusion = fusion.copy(geneDown = null, exonDown = null)
        assertThat(fusion.display()).isEqualTo("geneUp exon 5 fusion")
    }

    @Test
    fun `Should display fusions correctly if only geneUp known`() {
        val fusion = fusion.copy(exonUp = null, geneDown = null, exonDown = null)
        assertThat(fusion.display()).isEqualTo("geneUp fusion")
    }

    @Test
    fun `Should display fusions correctly if only geneDown and exonDown known`() {
        val fusion = fusion.copy(geneUp = null, exonUp = null)
        assertThat(fusion.display()).isEqualTo("geneDown exon 10 fusion")
    }

    @Test
    fun `Should display fusions correctly if only geneDown known`() {
        val fusion = fusion.copy(geneUp = null, exonUp = null, exonDown = null)
        assertThat(fusion.display()).isEqualTo("geneDown fusion")
    }

    @Test(expected = IllegalStateException::class)
    fun `Should throw exception if geneUp and geneDown are null`() {
        val fusion = fusion.copy(geneUp = null, exonUp = null,  geneDown = null, exonDown = null)
        fusion.display()
    }
}