package com.hartwig.actin.molecular.evidence.matching

import com.hartwig.actin.datamodel.molecular.TestMolecularFactory
import com.hartwig.actin.datamodel.molecular.driver.CodingEffect
import com.hartwig.actin.datamodel.molecular.driver.VariantType
import com.hartwig.actin.molecular.evidence.known.TestServeKnownFactory
import com.hartwig.serve.datamodel.molecular.MutationType
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

private val VARIANT = TestMolecularFactory.createMinimalVariant().copy(
    gene = "gene 1",
    canonicalImpact = TestMolecularFactory.createMinimalTranscriptImpact().copy(codingEffect = CodingEffect.MISSENSE),
    type = VariantType.SNV,
    chromosome = "12",
    position = 13,
    ref = "A",
    alt = "T",
    isReportable = true,
)

private val RANGE = TestServeKnownFactory.codonBuilder()
    .gene("gene 1")
    .chromosome("12")
    .start(12)
    .end(14)
    .applicableMutationType(MutationType.ANY)
    .build()

class RangeMatchingTest {

    @Test
    fun `Should return true on matching range`() {
        assertThat(RangeMatching.isMatch(RANGE, VARIANT)).isTrue()
    }

    @Test
    fun `Should return false on non-matching gene`() {
        assertThat(RangeMatching.isMatch(RANGE, VARIANT.copy(gene = "gene 2"))).isFalse()
    }

    @Test
    fun `Should return false on non-matching position`() {
        assertThat(RangeMatching.isMatch(RANGE, VARIANT.copy(position = 1))).isFalse()
    }

    @Test
    fun `Should return false on non-matching coding effect`() {
        assertThat(RangeMatching.isMatch(RANGE, VARIANT
            .copy(canonicalImpact = VARIANT.canonicalImpact.copy(
                codingEffect = CodingEffect.NONE)))
        ).isFalse()
    }
}