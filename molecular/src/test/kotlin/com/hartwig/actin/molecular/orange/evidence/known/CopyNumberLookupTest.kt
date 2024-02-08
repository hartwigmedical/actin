package com.hartwig.actin.molecular.orange.evidence.known

import com.google.common.collect.Lists
import com.hartwig.actin.molecular.datamodel.driver.CopyNumberType
import com.hartwig.actin.molecular.orange.datamodel.purple.TestPurpleFactory
import com.hartwig.actin.molecular.orange.evidence.TestMolecularFactory.minimalTestCopyNumber
import com.hartwig.actin.molecular.orange.evidence.TestMolecularFactory.minimalTestHomozygousDisruption
import com.hartwig.hmftools.datamodel.purple.CopyNumberInterpretation
import com.hartwig.hmftools.datamodel.purple.PurpleGainLoss
import com.hartwig.serve.datamodel.gene.GeneEvent
import com.hartwig.serve.datamodel.gene.KnownCopyNumber
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CopyNumberLookupTest {

    @Test
    fun canLookupCopyNumbers() {
        val amp: KnownCopyNumber = TestServeKnownFactory.copyNumberBuilder().gene("gene 1").event(GeneEvent.AMPLIFICATION).build()
        val del: KnownCopyNumber = TestServeKnownFactory.copyNumberBuilder().gene("gene 2").event(GeneEvent.DELETION).build()
        val knownCopyNumbers: MutableList<KnownCopyNumber> = Lists.newArrayList(amp, del)

//        val ampOnGene1 = create("gene 1", CopyNumberInterpretation.FULL_GAIN)
        val ampOnGene1 = minimalTestCopyNumber().copy(gene = "gene 1", type = CopyNumberType.FULL_GAIN)
        assertEquals(amp, CopyNumberLookup.findForCopyNumber(knownCopyNumbers, ampOnGene1))

//        val ampOnGene2 = create("gene 2", CopyNumberInterpretation.FULL_GAIN)
        val ampOnGene2 = minimalTestCopyNumber().copy(gene = "gene 2", type = CopyNumberType.FULL_GAIN)
        assertNull(CopyNumberLookup.findForCopyNumber(knownCopyNumbers, ampOnGene2))

//        val delOnGene1 = create("gene 1", CopyNumberInterpretation.FULL_LOSS)
        val delOnGene1 = minimalTestCopyNumber().copy(gene = "gene 1", type = CopyNumberType.LOSS)
        assertNull(CopyNumberLookup.findForCopyNumber(knownCopyNumbers, delOnGene1))

//        val delOnGene2 = create("gene 2", CopyNumberInterpretation.FULL_LOSS)
        val delOnGene2 = minimalTestCopyNumber().copy(gene = "gene 2", type = CopyNumberType.LOSS)
        assertEquals(del, CopyNumberLookup.findForCopyNumber(knownCopyNumbers, delOnGene2))
    }

    @Test
    fun canLookupHomozygousDisruptions() {
        val amp: KnownCopyNumber = TestServeKnownFactory.copyNumberBuilder().gene("gene 1").event(GeneEvent.AMPLIFICATION).build()
        val del: KnownCopyNumber = TestServeKnownFactory.copyNumberBuilder().gene("gene 1").event(GeneEvent.DELETION).build()
        val knownCopyNumbers: MutableList<KnownCopyNumber> = Lists.newArrayList(amp, del)

//        val homDisruptionGene1: LinxHomozygousDisruption = TestLinxFactory.homozygousDisruptionBuilder().gene("gene 1").build()
//        val homDisruptionGene2: LinxHomozygousDisruption = TestLinxFactory.homozygousDisruptionBuilder().gene("gene 2").build()
        val homDisruptionGene1 = minimalTestHomozygousDisruption().copy(gene = "gene 1")
        val homDisruptionGene2 = minimalTestHomozygousDisruption().copy(gene = "gene 2")
        assertEquals(del, CopyNumberLookup.findForHomozygousDisruption(knownCopyNumbers, homDisruptionGene1))
        assertNull(CopyNumberLookup.findForHomozygousDisruption(knownCopyNumbers, homDisruptionGene2))
    }

    companion object {
        private fun create(gene: String, interpretation: CopyNumberInterpretation): PurpleGainLoss {
            return TestPurpleFactory.gainLossBuilder().gene(gene).interpretation(interpretation).build()
        }
    }
}