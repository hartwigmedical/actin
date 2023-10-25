package com.hartwig.actin.molecular.orange.evidence.known

import com.google.common.collect.Lists
import com.hartwig.actin.molecular.orange.datamodel.linx.TestLinxFactory
import com.hartwig.actin.molecular.orange.datamodel.purple.TestPurpleFactory
import com.hartwig.hmftools.datamodel.linx.HomozygousDisruption
import com.hartwig.hmftools.datamodel.purple.CopyNumberInterpretation
import com.hartwig.hmftools.datamodel.purple.PurpleGainLoss
import com.hartwig.serve.datamodel.gene.GeneEvent
import com.hartwig.serve.datamodel.gene.KnownCopyNumber
import org.junit.Assert
import org.junit.Test

class CopyNumberLookupTest {
    @Test
    fun canLookupCopyNumbers() {
        val amp: KnownCopyNumber = TestServeKnownFactory.copyNumberBuilder().gene("gene 1").event(GeneEvent.AMPLIFICATION).build()
        val del: KnownCopyNumber = TestServeKnownFactory.copyNumberBuilder().gene("gene 2").event(GeneEvent.DELETION).build()
        val knownCopyNumbers: MutableList<KnownCopyNumber> = Lists.newArrayList(amp, del)
        val ampOnGene1 = create("gene 1", CopyNumberInterpretation.FULL_GAIN)
        Assert.assertEquals(amp, CopyNumberLookup.findForCopyNumber(knownCopyNumbers, ampOnGene1))
        val ampOnGene2 = create("gene 2", CopyNumberInterpretation.FULL_GAIN)
        Assert.assertNull(CopyNumberLookup.findForCopyNumber(knownCopyNumbers, ampOnGene2))
        val delOnGene1 = create("gene 1", CopyNumberInterpretation.FULL_LOSS)
        Assert.assertNull(CopyNumberLookup.findForCopyNumber(knownCopyNumbers, delOnGene1))
        val delOnGene2 = create("gene 2", CopyNumberInterpretation.FULL_LOSS)
        Assert.assertEquals(del, CopyNumberLookup.findForCopyNumber(knownCopyNumbers, delOnGene2))
    }

    @Test
    fun canLookupHomozygousDisruptions() {
        val amp: KnownCopyNumber = TestServeKnownFactory.copyNumberBuilder().gene("gene 1").event(GeneEvent.AMPLIFICATION).build()
        val del: KnownCopyNumber = TestServeKnownFactory.copyNumberBuilder().gene("gene 1").event(GeneEvent.DELETION).build()
        val knownCopyNumbers: MutableList<KnownCopyNumber> = Lists.newArrayList(amp, del)
        val homDisruptionGene1: HomozygousDisruption = TestLinxFactory.homozygousDisruptionBuilder().gene("gene 1").build()
        val homDisruptionGene2: HomozygousDisruption = TestLinxFactory.homozygousDisruptionBuilder().gene("gene 2").build()
        Assert.assertEquals(del, CopyNumberLookup.findForHomozygousDisruption(knownCopyNumbers, homDisruptionGene1))
        Assert.assertNull(CopyNumberLookup.findForHomozygousDisruption(knownCopyNumbers, homDisruptionGene2))
    }

    companion object {
        private fun create(gene: String, interpretation: CopyNumberInterpretation): PurpleGainLoss {
            return TestPurpleFactory.gainLossBuilder().gene(gene).interpretation(interpretation).build()
        }
    }
}