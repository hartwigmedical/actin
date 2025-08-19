package com.hartwig.actin.molecular.util

import com.hartwig.actin.molecular.util.FormatFunctions.formatVariantImpact
import com.hartwig.actin.molecular.util.FormatFunctions.formatFusionEvent
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class FormatFunctionsTest {

    private val geneUp = "geneUp"
    private val exonUp = 5
    private val geneDown = "geneDown"
    private val exonDown = 10

    @Test
    fun `Should format variant impact with protein impact`() {
        val result = formatVariantImpact("p.Met1Leu", "", false, false, "")
        assertThat(result).isEqualTo("Met1Leu")
    }

    @Test
    fun `Should format variant impact with coding impact and splice`() {
        val result = formatVariantImpact("", "c.123A>T", true, false, "")
        assertThat(result).isEqualTo("c.123A>T splice")
    }

    @Test
    fun `Should format variant impact with coding impact`() {
        val result = formatVariantImpact("", "c.123A>T", false, false, "")
        assertThat(result).isEqualTo("c.123A>T")
    }

    @Test
    fun `Should format variant impact with upstream`() {
        val result = formatVariantImpact("", "", false, true, "")
        assertThat(result).isEqualTo("upstream")
    }

    @Test
    fun `Should format variant impact with effects`() {
        val result = formatVariantImpact("", "", false, false, "some_effect")
        assertThat(result).isEqualTo("some_effect")
    }

    @Test
    fun `Should prioritize protein impact over coding impact`() {
        val result = formatVariantImpact("p.Met1Leu", "c.123A>T", false, false, "")
        assertThat(result).isEqualTo("Met1Leu")
    }

    @Test
    fun `Should prioritize protein impact over upstream`() {
        val result = formatVariantImpact("p.Met1Leu", "", false, true, "")
        assertThat(result).isEqualTo("Met1Leu")
    }

    @Test
    fun `Should prioritize protein impact over effects`() {
        val result = formatVariantImpact("p.Met1Leu", "", false, false, "some_effect")
        assertThat(result).isEqualTo("Met1Leu")
    }

    @Test
    fun `Should prioritize coding impact over upstream`() {
        val result = formatVariantImpact("", "c.123A>T", false, true, "")
        assertThat(result).isEqualTo("c.123A>T")
    }

    @Test
    fun `Should prioritize coding impact over effects`() {
        val result = formatVariantImpact("", "c.123A>T", false, false, "some_effect")
        assertThat(result).isEqualTo("c.123A>T")
    }

    @Test
    fun `Should display fusions correctly with exon if exons known`() {
        assertThat(
            formatFusionEvent(
                geneUp = geneUp,
                exonUp = exonUp,
                geneDown = geneDown,
                exonDown = exonDown
            )
        ).isEqualTo("geneUp(exon5)::geneDown(exon10) fusion")
    }

    @Test
    fun `Should display fusions correctly without exon if exons not known`() {
        assertThat(
            formatFusionEvent(
                geneUp = geneUp,
                exonUp = null,
                geneDown = geneDown,
                exonDown = null
            )
        ).isEqualTo("geneUp::geneDown fusion")
    }

    @Test
    fun `Should display fusions correctly if only geneUp and exonUp known`() {
        assertThat(
            formatFusionEvent(
                geneUp = geneUp,
                exonUp = exonUp,
                geneDown = null,
                exonDown = null
            )
        ).isEqualTo("geneUp(exon5) fusion")
    }

    @Test
    fun `Should display fusions correctly if only geneUp known`() {
        assertThat(
            formatFusionEvent(
                geneUp = geneUp,
                exonUp = null,
                geneDown = null,
                exonDown = null
            )
        ).isEqualTo("geneUp fusion")
    }

    @Test
    fun `Should display fusions correctly if only geneDown and exonDown known`() {
        assertThat(
            formatFusionEvent(
                geneUp = null,
                exonUp = null,
                geneDown = geneDown,
                exonDown = exonDown
            )
        ).isEqualTo("geneDown(exon10) fusion")
    }

    @Test
    fun `Should display fusions correctly if only geneDown known`() {
        assertThat(
            formatFusionEvent(
                geneUp = null,
                exonUp = null,
                geneDown = geneDown,
                exonDown = null
            )
        ).isEqualTo("geneDown fusion")
    }

    @Test(expected = IllegalStateException::class)
    fun `Should throw exception if geneUp and geneDown are null`() {
        formatFusionEvent(geneUp = null, exonUp = null, geneDown = null, exonDown = null)
    }
}