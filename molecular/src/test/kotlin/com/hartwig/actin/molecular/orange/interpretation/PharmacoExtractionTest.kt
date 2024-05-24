package com.hartwig.actin.molecular.orange.interpretation

import com.hartwig.actin.molecular.datamodel.wgs.pharmaco.Haplotype
import com.hartwig.actin.molecular.orange.datamodel.TestOrangeFactory
import com.hartwig.actin.molecular.orange.datamodel.peach.TestPeachFactory
import com.hartwig.hmftools.datamodel.orange.ImmutableOrangeRecord
import com.hartwig.hmftools.datamodel.orange.OrangeRecord
import com.hartwig.hmftools.datamodel.peach.PeachGenotype
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class PharmacoExtractionTest {

    @Test
    fun `Should extract pharmaco`() {
        val peachEntry1 = TestPeachFactory.builder().gene("gene 1").haplotype("haplotype 1").function("function 1").build()
        val peachEntry2 = TestPeachFactory.builder().gene("gene 1").haplotype("haplotype 2").function("function 2").build()
        val orange = withPeachEntries(peachEntry1, peachEntry2)

        val entries = PharmacoExtraction.extract(orange)
        assertThat(entries).hasSize(1)

        val entry = entries.iterator().next()
        assertThat(entry.gene).isEqualTo("gene 1")
        assertThat(entry.haplotypes).hasSize(2)

        val haplotype1 = findByName(entry.haplotypes, "haplotype 1")
        assertThat(haplotype1.function).isEqualTo("function 1")

        val haplotype2 = findByName(entry.haplotypes, "haplotype 2")
        assertThat(haplotype2.function).isEqualTo("function 2")
    }

    companion object {
        private fun withPeachEntries(vararg peachEntries: PeachGenotype): OrangeRecord {
            val base = TestOrangeFactory.createMinimalTestOrangeRecord()
            return ImmutableOrangeRecord.builder().from(base).addPeach(*peachEntries).build()
        }

        private fun findByName(haplotypes: Set<Haplotype>, nameToFind: String): Haplotype {
            return haplotypes.find { it.name == nameToFind }
                ?: throw IllegalStateException("Could not find haplotype with name: $nameToFind")
        }
    }
}