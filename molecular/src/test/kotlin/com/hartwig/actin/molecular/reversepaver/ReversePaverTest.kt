package com.hartwig.actin.molecular.reversepaver

import com.hartwig.actin.molecular.panel.GENE
import com.hartwig.actin.molecular.panel.HGVS_CODING
import com.hartwig.hmftools.common.gene.TranscriptData
import com.hartwig.hmftools.pavereverse.BaseSequenceVariants
import com.hartwig.hmftools.pavereverse.ReversePave
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.Test
import com.hartwig.hmftools.pavereverse.BaseSequenceChange as HmfBaseSequenceChange

class ReversePaverTest {
    private val reversePave = mockk<ReversePave>()

    @Test
    fun `Should throw exception on null output from reverse-pave`() {
        every { reversePave.calculateDnaVariant(GENE, null, HGVS_CODING) } returns null
        assertThatThrownBy { ReversePaver(reversePave).resolve(GENE, null, HGVS_CODING) }
            .isInstanceOf(IllegalStateException::class.java)
            .hasMessageContaining("Unable to resolve variant '$GENE $HGVS_CODING' in variant annotator.")
    }


    @Test
    fun `Should throw exception on invalid HGVS format`() {
        assertThatThrownBy { ReversePaver(reversePave).resolve(GENE, null, "invalid") }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("Invalid HGVS format: invalid")
    }

    @Test
    fun `Should resolve coding variant`() {
        val baseSequenceChange = HmfBaseSequenceChange("A", "T", "chr1", 100)
        every { reversePave.calculateDnaVariant(GENE, null, HGVS_CODING) } returns baseSequenceChange

        val result = ReversePaver(reversePave).resolve(GENE, null, HGVS_CODING)
        assert(result.chromosome == "chr1" && result.position == 100 && result.ref == "A" && result.alt == "T")
    }

    @Test
    fun `Should resolve protein variant`() {
        // TODO KZ BaseSequenceVariants seems to remove the 'chr' prefix on chromosome, verify it does the same on the chromosome in Changes
        val baseSequenceChange = HmfBaseSequenceChange("G", "C", "1", 200)
        val transcript = mockk<TranscriptData>()
        every { reversePave.calculateProteinVariant(GENE, null, "p.Lys2Asn") } returns BaseSequenceVariants(transcript, "1", setOf(baseSequenceChange))

        val result = ReversePaver(reversePave).resolve(GENE, null, "p.Lys2Asn")
        assert(result.chromosome == "1" && result.position == 200 && result.ref == "G" && result.alt == "C")
    }

    @Test
    fun `Should select the best base sequence change from multiple options`() {
        val change1 = HmfBaseSequenceChange("A", "T", "1", 150) // abs length diff = 0
        val change2 = HmfBaseSequenceChange("AG", "T", "1", 150) // abs length diff = 1
        val change3 = HmfBaseSequenceChange("A", "TC", "1", 150) // abs length diff = 1
        val transcript = mockk<TranscriptData>()
        every { reversePave.calculateProteinVariant(GENE, null, "p.HGVS") } returns BaseSequenceVariants(transcript, "2", setOf(change2, change1, change3))

        val result = ReversePaver(reversePave).resolve(GENE, null, "p.HGVS")
        assert(result.chromosome == "1" && result.position == 150 && result.ref == "A" && result.alt == "T")
    }
}