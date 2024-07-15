package com.hartwig.actin.molecular.evidence.known

import com.hartwig.actin.molecular.TestMolecularFactory.minimalCopyNumber
import com.hartwig.actin.molecular.TestMolecularFactory.minimalHomozygousDisruption
import com.hartwig.actin.molecular.datamodel.orange.driver.CopyNumberType
import com.hartwig.serve.datamodel.gene.GeneEvent
import com.hartwig.serve.datamodel.gene.KnownCopyNumber
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class CopyNumberLookupTest {

    @Test
    fun `Should lookup copy numbers`() {
        val amp: KnownCopyNumber = TestServeKnownFactory.copyNumberBuilder().gene("gene 1").event(GeneEvent.AMPLIFICATION).build()
        val del: KnownCopyNumber = TestServeKnownFactory.copyNumberBuilder().gene("gene 2").event(GeneEvent.DELETION).build()
        val knownCopyNumbers = listOf(amp, del)

        val ampOnGene1 = minimalCopyNumber().copy(gene = "gene 1", type = CopyNumberType.FULL_GAIN)
        assertThat(CopyNumberLookup.findForCopyNumber(knownCopyNumbers, ampOnGene1)).isEqualTo(amp)

        val ampOnGene2 = minimalCopyNumber().copy(gene = "gene 2", type = CopyNumberType.FULL_GAIN)
        assertThat(CopyNumberLookup.findForCopyNumber(knownCopyNumbers, ampOnGene2)).isNull()

        val delOnGene1 = minimalCopyNumber().copy(gene = "gene 1", type = CopyNumberType.LOSS)
        assertThat(CopyNumberLookup.findForCopyNumber(knownCopyNumbers, delOnGene1)).isNull()

        val delOnGene2 = minimalCopyNumber().copy(gene = "gene 2", type = CopyNumberType.LOSS)
        assertThat(CopyNumberLookup.findForCopyNumber(knownCopyNumbers, delOnGene2)).isEqualTo(del)
    }

    @Test
    fun `Should lookup homozygous disruptions`() {
        val amp: KnownCopyNumber = TestServeKnownFactory.copyNumberBuilder().gene("gene 1").event(GeneEvent.AMPLIFICATION).build()
        val del: KnownCopyNumber = TestServeKnownFactory.copyNumberBuilder().gene("gene 1").event(GeneEvent.DELETION).build()
        val knownCopyNumbers = listOf(amp, del)

        val homDisruptionGene1 = minimalHomozygousDisruption().copy(gene = "gene 1")
        val homDisruptionGene2 = minimalHomozygousDisruption().copy(gene = "gene 2")
        assertThat(CopyNumberLookup.findForHomozygousDisruption(knownCopyNumbers, homDisruptionGene1)).isEqualTo(del)
        assertThat(CopyNumberLookup.findForHomozygousDisruption(knownCopyNumbers, homDisruptionGene2)).isNull()
    }
}