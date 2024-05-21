package com.hartwig.actin.molecular.evidence.matching

import com.hartwig.actin.molecular.datamodel.driver.CodingEffect
import com.hartwig.actin.molecular.datamodel.driver.VariantType
import com.hartwig.serve.datamodel.MutationType
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class MutationTypeMatchingTest {

    @Test
    fun `Should not match for all mutation types when coding effect is none`() {
        val nonCoding = VARIANT_CRITERIA.copy(codingEffect = CodingEffect.NONE)
        val nothing = setOf<MutationType>()
        shouldMatch(nonCoding, nothing)
    }

    @Test
    fun `Should match for nonsense or frameshift`() {
        val nonsenseOrFrameshift =
            VARIANT_CRITERIA.copy(codingEffect = CodingEffect.NONSENSE_OR_FRAMESHIFT)
        shouldMatch(nonsenseOrFrameshift, setOf(MutationType.NONSENSE_OR_FRAMESHIFT, MutationType.ANY))
    }

    @Test
    fun `Should match for splice`() {
        val splice = VARIANT_CRITERIA.copy(codingEffect = CodingEffect.SPLICE)
        assertThat(MutationTypeMatching.matches(MutationType.SPLICE, splice)).isTrue()
        assertThat(MutationTypeMatching.matches(MutationType.ANY, splice)).isTrue()
    }

    @Test
    fun `Should match for inframe missense`() {
        val inframe = VARIANT_CRITERIA.copy(
            codingEffect = CodingEffect.MISSENSE,
            type = VariantType.MNV,
            ref = "AAG",
            alt = "TTG"
        )
        shouldMatch(inframe, setOf(MutationType.MISSENSE, MutationType.INFRAME, MutationType.ANY))
    }

    @Test
    fun `Should match for inframe deletion`() {
        val inframeDeletion = VARIANT_CRITERIA.copy(
            codingEffect = CodingEffect.MISSENSE,
            type = VariantType.DELETE,
            ref = "ATGATG",
            alt = "TTT"
        )
        shouldMatch(inframeDeletion, setOf(MutationType.MISSENSE, MutationType.INFRAME, MutationType.INFRAME_DELETION, MutationType.ANY))
    }

    @Test
    fun `Should match for inframe insertion`() {
        val inframeInsertion = VARIANT_CRITERIA.copy(
            codingEffect = CodingEffect.MISSENSE,
            type = VariantType.INSERT,
            ref = "TTT",
            alt = "ATGATG"
        )
        shouldMatch(inframeInsertion, setOf(MutationType.MISSENSE, MutationType.INFRAME, MutationType.INFRAME_INSERTION, MutationType.ANY))
    }

    @Test
    fun `Should match for snv missense`() {
        val missense = VARIANT_CRITERIA.copy(
            codingEffect = CodingEffect.MISSENSE,
            type = VariantType.SNV
        )
        shouldMatch(missense, setOf(MutationType.MISSENSE, MutationType.ANY))
    }

    private fun shouldMatch(nonsenseOrFrameshift: VariantMatchCriteria, matchingTypes: Set<MutationType>) {
        matchingTypes.forEach {
            assertThat(MutationTypeMatching.matches(it, nonsenseOrFrameshift)).withFailMessage { "Expected $it to match" }.isTrue()
        }
        (MutationType.values().toSet() - matchingTypes).forEach {
            assertThat(MutationTypeMatching.matches(it, nonsenseOrFrameshift)).withFailMessage { "Expected $it not to match" }.isFalse()
        }
    }
}