package com.hartwig.actin.molecular.evidence.known

import com.google.common.collect.Lists
import com.hartwig.actin.molecular.datamodel.wgs.driver.CopyNumberType
import com.hartwig.actin.molecular.evidence.TestMolecularFactory.minimalCopyNumber
import com.hartwig.actin.molecular.evidence.TestMolecularFactory.minimalHomozygousDisruption
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

        val ampOnGene1 = minimalCopyNumber().copy(gene = "gene 1", type = CopyNumberType.FULL_GAIN)
        assertEquals(amp, CopyNumberLookup.findForCopyNumber(knownCopyNumbers, ampOnGene1))

        val ampOnGene2 = minimalCopyNumber().copy(gene = "gene 2", type = CopyNumberType.FULL_GAIN)
        assertNull(CopyNumberLookup.findForCopyNumber(knownCopyNumbers, ampOnGene2))

        val delOnGene1 = minimalCopyNumber().copy(gene = "gene 1", type = CopyNumberType.LOSS)
        assertNull(CopyNumberLookup.findForCopyNumber(knownCopyNumbers, delOnGene1))

        val delOnGene2 = minimalCopyNumber().copy(gene = "gene 2", type = CopyNumberType.LOSS)
        assertEquals(del, CopyNumberLookup.findForCopyNumber(knownCopyNumbers, delOnGene2))
    }

    @Test
    fun canLookupHomozygousDisruptions() {
        val amp: KnownCopyNumber = TestServeKnownFactory.copyNumberBuilder().gene("gene 1").event(GeneEvent.AMPLIFICATION).build()
        val del: KnownCopyNumber = TestServeKnownFactory.copyNumberBuilder().gene("gene 1").event(GeneEvent.DELETION).build()
        val knownCopyNumbers: MutableList<KnownCopyNumber> = Lists.newArrayList(amp, del)

        val homDisruptionGene1 = minimalHomozygousDisruption().copy(gene = "gene 1")
        val homDisruptionGene2 = minimalHomozygousDisruption().copy(gene = "gene 2")
        assertEquals(del, CopyNumberLookup.findForHomozygousDisruption(knownCopyNumbers, homDisruptionGene1))
        assertNull(CopyNumberLookup.findForHomozygousDisruption(knownCopyNumbers, homDisruptionGene2))
    }
}