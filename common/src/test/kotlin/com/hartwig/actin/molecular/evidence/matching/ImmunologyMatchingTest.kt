package com.hartwig.actin.molecular.evidence.matching

import com.hartwig.actin.datamodel.molecular.immunology.TestHlaAlleleFactory
import com.hartwig.actin.molecular.evidence.TestServeMolecularFactory
import com.hartwig.serve.datamodel.molecular.immuno.ImmutableActionableHLA
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatIllegalStateException
import org.junit.Test

private const val GENE = "A"
private const val ALLELE_GROUP = "02"
private const val HLA_PROTEIN = "01"
private val MATCHING_HLA_ALLELE =
    TestHlaAlleleFactory.createMinimal().copy(name = "$GENE*$ALLELE_GROUP:$HLA_PROTEIN", event = "HLA-$GENE*$ALLELE_GROUP:$HLA_PROTEIN")
private val ANNOTATION =
    ImmutableActionableHLA.builder().from(TestServeMolecularFactory.createActionableEvent()).gene("HLA-$GENE").alleleGroup(ALLELE_GROUP)
        .hlaProtein(HLA_PROTEIN).build()

class ImmunologyMatchingTest {

    @Test
    fun `Should return true on matching hla`() {
        assertThat(ImmunologyMatching.isMatch(ANNOTATION, MATCHING_HLA_ALLELE)).isTrue()
    }

    @Test
    fun `Should return false on non-matching hla`() {
        assertThat(ImmunologyMatching.isMatch(ANNOTATION, MATCHING_HLA_ALLELE.copy(name = "B*02:01"))).isFalse()
    }

    @Test
    fun `Should throw exception on incorrect hla allele format`() {
        assertThatIllegalStateException().isThrownBy { ImmunologyMatching.isMatch(ANNOTATION, MATCHING_HLA_ALLELE.copy(name = "B^02")) }
    }
}