package com.hartwig.actin.molecular.evidence.matching

import com.hartwig.actin.datamodel.molecular.immunology.TestHlaAlleleFactory
import com.hartwig.actin.molecular.evidence.TestServeMolecularFactory
import com.hartwig.serve.datamodel.molecular.immuno.ImmutableActionableHLA
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

private val MATCHING_HLA_ALLELE = TestHlaAlleleFactory.createMinimal().copy(name = "A*02:01", event = "HLA-A*02:01")
private val ANNOTATION =
    ImmutableActionableHLA.builder().from(TestServeMolecularFactory.createActionableEvent()).gene("HLA-A").alleleGroup("02")
        .hlaProtein("01").build()

class ImmunologyMatchingTest {

    @Test
    fun `Should return false on matching hla`() {
        assertThat(ImmunologyMatching.isMatch(ANNOTATION, MATCHING_HLA_ALLELE)).isTrue()
    }

    @Test
    fun `Should return true on non-matching hla`() {
        assertThat(ImmunologyMatching.isMatch(ANNOTATION, MATCHING_HLA_ALLELE.copy(name = "B*02:01"))).isFalse()
    }
}