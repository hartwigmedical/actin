package com.hartwig.actin.molecular.orange.interpretation

import com.hartwig.actin.molecular.datamodel.pharmaco.Haplotype
import com.hartwig.actin.molecular.orange.datamodel.TestOrangeFactory
import com.hartwig.actin.molecular.orange.datamodel.peach.TestPeachFactory
import com.hartwig.hmftools.datamodel.orange.ImmutableOrangeRecord
import com.hartwig.hmftools.datamodel.orange.OrangeRecord
import com.hartwig.hmftools.datamodel.peach.PeachGenotype
import org.junit.Assert
import org.junit.Test

class PharmacoExtractionTest {
    @Test
    fun canExtractPharmaco() {
        val peachEntry1: PeachGenotype? = TestPeachFactory.builder().gene("gene 1").haplotype("haplotype 1").function("function 1").build()
        val peachEntry2: PeachGenotype? = TestPeachFactory.builder().gene("gene 1").haplotype("haplotype 2").function("function 2").build()
        val orange = withPeachEntries(peachEntry1, peachEntry2)
        val entries = PharmacoExtraction.extract(orange)
        Assert.assertEquals(1, entries.size.toLong())
        val entry = entries.iterator().next()
        Assert.assertEquals("gene 1", entry.gene())
        Assert.assertEquals(2, entry.haplotypes().size.toLong())
        val haplotype1 = findByName(entry.haplotypes(), "haplotype 1")
        Assert.assertEquals("function 1", haplotype1.function())
        val haplotype2 = findByName(entry.haplotypes(), "haplotype 2")
        Assert.assertEquals("function 2", haplotype2.function())
    }

    companion object {
        private fun withPeachEntries(vararg peachEntries: PeachGenotype): OrangeRecord {
            val base = TestOrangeFactory.createMinimalTestOrangeRecord()
            return ImmutableOrangeRecord.builder().from(base).addPeach(*peachEntries).build()
        }

        private fun findByName(haplotypes: MutableSet<Haplotype?>, nameToFind: String): Haplotype {
            for (haplotype in haplotypes) {
                if (haplotype.name() == nameToFind) {
                    return haplotype
                }
            }
            throw IllegalStateException("Could not find haplotype with name: $nameToFind")
        }
    }
}