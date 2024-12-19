package com.hartwig.actin.molecular.evidence.known

import com.hartwig.actin.datamodel.molecular.TestMolecularFactory
import com.hartwig.actin.datamodel.molecular.driver.TestTranscriptCopyNumberImpactFactory
import com.hartwig.actin.datamodel.molecular.orange.driver.CopyNumberType
import com.hartwig.serve.datamodel.molecular.gene.GeneEvent
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class CopyNumberLookupTest {

    @Test
    fun `Should lookup copy numbers`() {
        val amp = TestServeKnownFactory.copyNumberBuilder().gene("gene 1").event(GeneEvent.AMPLIFICATION).build()
        val del = TestServeKnownFactory.copyNumberBuilder().gene("gene 2").event(GeneEvent.DELETION).build()
        val knownCopyNumbers = listOf(amp, del)

        val ampOnGene1 = TestMolecularFactory.minimalCopyNumber().copy(
            gene = "gene 1",
            canonicalImpact = TestTranscriptCopyNumberImpactFactory.createTranscriptCopyNumberImpact(CopyNumberType.FULL_GAIN)
        )
        assertThat(CopyNumberLookup.findForCopyNumber(knownCopyNumbers, ampOnGene1)).isEqualTo(amp)

        val ampOnGene2 = TestMolecularFactory.minimalCopyNumber().copy(
            gene = "gene 2",
            canonicalImpact = TestTranscriptCopyNumberImpactFactory.createTranscriptCopyNumberImpact(CopyNumberType.FULL_GAIN)
        )
        assertThat(CopyNumberLookup.findForCopyNumber(knownCopyNumbers, ampOnGene2)).isNull()

        val delOnGene1 = TestMolecularFactory.minimalCopyNumber().copy(
            gene = "gene 1",
            canonicalImpact = TestTranscriptCopyNumberImpactFactory.createTranscriptCopyNumberImpact(CopyNumberType.LOSS)
        )
        assertThat(CopyNumberLookup.findForCopyNumber(knownCopyNumbers, delOnGene1)).isNull()

        val delOnGene2 = TestMolecularFactory.minimalCopyNumber().copy(
            gene = "gene 2",
            canonicalImpact = TestTranscriptCopyNumberImpactFactory.createTranscriptCopyNumberImpact(CopyNumberType.LOSS)
        )
        assertThat(CopyNumberLookup.findForCopyNumber(knownCopyNumbers, delOnGene2)).isEqualTo(del)
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
}