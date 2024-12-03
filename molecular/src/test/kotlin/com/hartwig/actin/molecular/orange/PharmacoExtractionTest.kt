package com.hartwig.actin.molecular.orange

import com.hartwig.actin.datamodel.molecular.orange.pharmaco.Haplotype
import com.hartwig.actin.datamodel.molecular.orange.pharmaco.HaplotypeFunction
import com.hartwig.actin.datamodel.molecular.orange.pharmaco.PharmacoGene
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
        val peachEntry1 = TestPeachFactory.builder()
            .gene("DPYD")
            .allele("*1")
            .alleleCount(1)
            .function("normal function")
            .build()
        val peachEntry2 = TestPeachFactory.builder()
            .gene("DPYD")
            .allele("*2")
            .alleleCount(2)
            .function("reduced function")
            .build()
        val orange = withPeachEntries(peachEntry1, peachEntry2)

        val entries = PharmacoExtraction.extract(orange)
        assertThat(entries).hasSize(1)

        val entry = entries.iterator().next()
        assertThat(entry.gene).isEqualTo(PharmacoGene.DPYD)
        assertThat(entry.haplotypes).hasSize(2)

        val haplotype1 = findByHaplotype(entry.haplotypes, "*1_HET")
        assertThat(haplotype1.function).isEqualTo(HaplotypeFunction.NORMAL_FUNCTION)

        val haplotype2 = findByHaplotype(entry.haplotypes, "*2_HOM")
        assertThat(haplotype2.function).isEqualTo(HaplotypeFunction.REDUCED_FUNCTION)
    }

    @Test(expected = IllegalStateException::class)
    fun `Should throw exception when unexpected pharmaco gene`() {
        val peachEntry = TestPeachFactory.builder()
            .gene("unknown gene")
            .build()
        val orange = withPeachEntries(peachEntry)
        PharmacoExtraction.extract(orange)
    }

    @Test(expected = IllegalStateException::class)
    fun `Should throw exception when unexpected haplotype function`() {
        val peachEntry = TestPeachFactory.builder()
            .function("unexpected function")
            .build()
        val orange = withPeachEntries(peachEntry)
        PharmacoExtraction.extract(orange)
    }

    private fun withPeachEntries(vararg peachEntries: PeachGenotype): OrangeRecord {
        val base = TestOrangeFactory.createMinimalTestOrangeRecord()
        return ImmutableOrangeRecord.builder().from(base).addPeach(*peachEntries).build()
    }

    private fun findByHaplotype(haplotypes: Set<Haplotype>, nameToFind: String): Haplotype {
        return haplotypes.find { it.toHaplotypeString() == nameToFind }
            ?: throw IllegalStateException("Could not find haplotype with name: $nameToFind")
    }
}