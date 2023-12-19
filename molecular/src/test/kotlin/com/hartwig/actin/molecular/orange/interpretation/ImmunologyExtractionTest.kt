package com.hartwig.actin.molecular.orange.interpretation

import com.hartwig.actin.molecular.datamodel.immunology.HlaAllele
import com.hartwig.actin.molecular.orange.datamodel.TestOrangeFactory
import com.hartwig.actin.molecular.orange.datamodel.lilac.TestLilacFactory
import com.hartwig.hmftools.datamodel.hla.ImmutableLilacRecord
import com.hartwig.hmftools.datamodel.hla.LilacAllele
import com.hartwig.hmftools.datamodel.orange.ImmutableOrangeRecord
import com.hartwig.hmftools.datamodel.orange.OrangeRecord
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ImmunologyExtractionTest {

    @Test
    fun canExtractImmunology() {
        val allele1: LilacAllele = TestLilacFactory.builder()
            .allele("allele 1")
            .tumorCopyNumber(1.2)
            .somaticMissense(1.0)
            .somaticInframeIndel(1.0)
            .somaticSplice(1.0)
            .somaticNonsenseOrFrameshift(0.0)
            .build()
        val allele2: LilacAllele = TestLilacFactory.builder()
            .allele("allele 2")
            .tumorCopyNumber(1.3)
            .somaticMissense(0.0)
            .somaticInframeIndel(0.0)
            .somaticSplice(0.0)
            .somaticNonsenseOrFrameshift(0.0)
            .build()
        val orange = withLilacData(ImmunologyExtraction.LILAC_QC_PASS, allele1, allele2)

        val immunology = ImmunologyExtraction.extract(orange)
        assertTrue(immunology.isReliable)
        assertEquals(2, immunology.hlaAlleles().size.toLong())

        val hlaAllele1 = findByName(immunology.hlaAlleles(), "allele 1")
        assertEquals(1.2, hlaAllele1.tumorCopyNumber(), EPSILON)
        assertTrue(hlaAllele1.hasSomaticMutations())

        val hlaAllele2 = findByName(immunology.hlaAlleles(), "allele 2")
        assertEquals(1.3, hlaAllele2.tumorCopyNumber(), EPSILON)
        assertFalse(hlaAllele2.hasSomaticMutations())
    }

    companion object {
        private const val EPSILON = 1.0E-10

        private fun findByName(hlaAlleles: MutableSet<HlaAllele>, nameToFind: String): HlaAllele {
            for (hlaAllele in hlaAlleles) {
                if (hlaAllele.name() == nameToFind) {
                    return hlaAllele
                }
            }
            throw IllegalStateException("Could not find hla allele with name: $nameToFind")
        }

        private fun withLilacData(lilacQc: String, vararg alleles: LilacAllele): OrangeRecord {
            val base = TestOrangeFactory.createMinimalTestOrangeRecord()
            return ImmutableOrangeRecord.builder()
                .from(base)
                .lilac(ImmutableLilacRecord.builder().from(base.lilac()).qc(lilacQc).addAlleles(*alleles).build())
                .build()
        }
    }
}