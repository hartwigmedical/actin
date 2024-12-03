package com.hartwig.actin.molecular.orange

import com.hartwig.actin.datamodel.molecular.orange.immunology.HlaAllele
import com.hartwig.actin.molecular.orange.datamodel.TestOrangeFactory
import com.hartwig.actin.molecular.orange.datamodel.lilac.TestLilacFactory
import com.hartwig.hmftools.datamodel.hla.ImmutableLilacRecord
import com.hartwig.hmftools.datamodel.hla.LilacAllele
import com.hartwig.hmftools.datamodel.orange.ImmutableOrangeRecord
import com.hartwig.hmftools.datamodel.orange.OrangeRecord
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.data.Offset
import org.junit.Test

private const val EPSILON = 1.0E-10

class ImmunologyExtractionTest {

    @Test
    fun `Should extract immunology`() {
        val allele1 = TestLilacFactory.builder()
            .allele("allele 1")
            .tumorCopyNumber(1.2)
            .somaticMissense(1.0)
            .somaticInframeIndel(1.0)
            .somaticSplice(1.0)
            .somaticNonsenseOrFrameshift(0.0)
            .build()

        val allele2 = TestLilacFactory.builder()
            .allele("allele 2")
            .tumorCopyNumber(1.3)
            .somaticMissense(0.0)
            .somaticInframeIndel(0.0)
            .somaticSplice(0.0)
            .somaticNonsenseOrFrameshift(0.0)
            .build()

        val orange = withLilacData(allele1, allele2)

        val immunology = ImmunologyExtraction.extract(orange)
        assertThat(immunology.isReliable).isTrue
        assertThat(immunology.hlaAlleles).hasSize(2)

        val hlaAllele1 = findByName(immunology.hlaAlleles, "allele 1")
        assertThat(hlaAllele1.tumorCopyNumber).isEqualTo(1.2, Offset.offset(EPSILON))
        assertThat(hlaAllele1.hasSomaticMutations).isTrue

        val hlaAllele2 = findByName(immunology.hlaAlleles, "allele 2")
        assertThat(hlaAllele2.tumorCopyNumber).isEqualTo(1.3, Offset.offset(EPSILON))
        assertThat(hlaAllele2.hasSomaticMutations).isFalse
    }

    private fun findByName(hlaAlleles: Set<HlaAllele>, nameToFind: String): HlaAllele {
        return hlaAlleles.find { it.name == nameToFind }
            ?: throw IllegalStateException("Could not find hla allele with name: $nameToFind")
    }

    private fun withLilacData(vararg alleles: LilacAllele): OrangeRecord {
        val base = TestOrangeFactory.createMinimalTestOrangeRecord()
        return ImmutableOrangeRecord.builder()
            .from(base)
            .lilac(
                ImmutableLilacRecord.builder().from(base.lilac()).qc(ImmunologyExtraction.LILAC_QC_PASS).addAlleles(*alleles).build()
            )
            .build()
    }
}