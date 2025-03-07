package com.hartwig.actin.molecular.evidence.known

import com.hartwig.actin.datamodel.molecular.TestMolecularFactory
import com.hartwig.actin.datamodel.molecular.driver.TestTranscriptCopyNumberImpactFactory
import com.hartwig.actin.datamodel.molecular.driver.CopyNumber
import com.hartwig.actin.datamodel.molecular.driver.CopyNumberType
import com.hartwig.serve.datamodel.molecular.gene.GeneEvent
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class CopyNumberLookupTest {

    @Test
    fun `Should lookup copy numbers`() {
        val amp = TestServeKnownFactory.copyNumberBuilder().gene("gene 1").event(GeneEvent.AMPLIFICATION).build()
        val del = TestServeKnownFactory.copyNumberBuilder().gene("gene 2").event(GeneEvent.DELETION).build()
        val knownCopyNumbers = listOf(amp, del)

        val ampOnGene1 = create("gene 1", CopyNumberType.FULL_GAIN)
        assertThat(CopyNumberLookup.findForCopyNumber(knownCopyNumbers, ampOnGene1)).isEqualTo(amp)

        val ampOnGene2 = create("gene 2", CopyNumberType.FULL_GAIN)
        assertThat(CopyNumberLookup.findForCopyNumber(knownCopyNumbers, ampOnGene2)).isNull()

        val delOnGene1 = create("gene 1", CopyNumberType.LOSS)
        assertThat(CopyNumberLookup.findForCopyNumber(knownCopyNumbers, delOnGene1)).isNull()

        val delOnGene2 = create("gene 2", CopyNumberType.LOSS)
        assertThat(CopyNumberLookup.findForCopyNumber(knownCopyNumbers, delOnGene2)).isEqualTo(del)

        val noneOnGene1 = create("gene 1", CopyNumberType.NONE)
        assertThat(CopyNumberLookup.findForCopyNumber(knownCopyNumbers, noneOnGene1)).isNull()
    }

    @Test
    fun `Should lookup homozygous disruptions`() {
        val amp = TestServeKnownFactory.copyNumberBuilder().gene("gene 1").event(GeneEvent.AMPLIFICATION).build()
        val del = TestServeKnownFactory.copyNumberBuilder().gene("gene 1").event(GeneEvent.DELETION).build()
        val knownCopyNumbers = listOf(amp, del)

        val homDisruptionGene1 = TestMolecularFactory.minimalHomozygousDisruption().copy(gene = "gene 1")
        val homDisruptionGene2 = TestMolecularFactory.minimalHomozygousDisruption().copy(gene = "gene 2")
        assertThat(CopyNumberLookup.findForHomozygousDisruption(knownCopyNumbers, homDisruptionGene1)).isEqualTo(del)
        assertThat(CopyNumberLookup.findForHomozygousDisruption(knownCopyNumbers, homDisruptionGene2)).isNull()
    }

    private fun create(gene: String, copyNumberType: CopyNumberType): CopyNumber {
        return TestMolecularFactory.minimalCopyNumber().copy(
            gene = gene,
            canonicalImpact = TestTranscriptCopyNumberImpactFactory.createTranscriptCopyNumberImpact(copyNumberType)
        )
    }
}