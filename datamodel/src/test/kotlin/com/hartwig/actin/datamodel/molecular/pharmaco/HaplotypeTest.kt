package com.hartwig.actin.datamodel.molecular.pharmaco

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.Test

class HaplotypeTest {

    @Test
    fun `Should produce correct haplotype string from allele attributes`() {
        val normalHaplotype1 = Haplotype("*1", 2, HaplotypeFunction.NORMAL_FUNCTION)
        assertThat(normalHaplotype1.toHaplotypeString()).isEqualTo("*1_HOM")

        val reducedHaplotype2 = Haplotype("*2", 1, HaplotypeFunction.REDUCED_FUNCTION)
        assertThat(reducedHaplotype2.toHaplotypeString()).isEqualTo("*2_HET")
    }

    @Test
    fun `Should preserve Unresolved Haplotype allele when converting to haplotype string`() {
        val unresolvedHaplotype = Haplotype("Unresolved Haplotype", 1, HaplotypeFunction.NORMAL_FUNCTION)
        assertThat(unresolvedHaplotype.toHaplotypeString()).isEqualTo("Unresolved Haplotype")
    }

    @Test
    fun `Should produce exception for incorrect allele count`() {
        val illegalZygosityHaplotype = Haplotype("*1", 3, HaplotypeFunction.NORMAL_FUNCTION)

        assertThatThrownBy { illegalZygosityHaplotype.toHaplotypeString() }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessage("Could not convert allele count 3 to a zygosity")
    }
}