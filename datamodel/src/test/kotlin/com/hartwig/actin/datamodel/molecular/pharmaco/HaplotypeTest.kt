package com.hartwig.actin.datamodel.molecular.pharmaco

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.Test

class HaplotypeTest {

    @Test
    fun `Should produce correct haplotype string from allele attributes`() {
        assertThat(Haplotype("*1", 2, HaplotypeFunction.NORMAL_FUNCTION).toHaplotypeString()).isEqualTo("*1_HOM")
        assertThat(Haplotype("*2", alleleCount = 1, function = HaplotypeFunction.REDUCED_FUNCTION).toHaplotypeString()).isEqualTo("*2_HET")
    }

    @Test
    fun `Should preserve Unresolved Haplotype allele when converting to haplotype string`() {
        assertThat(Haplotype("Unresolved Haplotype", 1, HaplotypeFunction.NORMAL_FUNCTION).toHaplotypeString()).isEqualTo("Unresolved Haplotype")
    }

    @Test
    fun `Should produce exception for incorrect allele count`() {
        assertThatThrownBy { Haplotype("*1", 3, HaplotypeFunction.NORMAL_FUNCTION).toHaplotypeString() }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessage("Could not convert allele count 3 to a zygosity")
    }
}