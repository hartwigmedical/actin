package com.hartwig.actin.molecular.evidence.matching

import com.hartwig.actin.molecular.datamodel.driver.CodingEffect
import com.hartwig.actin.molecular.datamodel.driver.VariantType
import com.hartwig.serve.datamodel.MutationType
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class MutationTypeMatchingTest {

    @Test
    fun `Should work for every coding effect`() {
        val nonCoding = VARIANT_CRITERIA.copy(codingEffect = CodingEffect.NONE)
        for (type in MutationType.values()) {
            assertThat(MutationTypeMatching.matches(type, nonCoding)).isFalse()
        }
    }

    @Test
    fun `Should match for nonsense or frameshift`() {
        val nonsenseOrFrameshift =
            VARIANT_CRITERIA.copy(codingEffect = CodingEffect.NONSENSE_OR_FRAMESHIFT)
        assertThat(MutationTypeMatching.matches(MutationType.NONSENSE_OR_FRAMESHIFT, nonsenseOrFrameshift)).isTrue()
        assertThat(MutationTypeMatching.matches(MutationType.ANY, nonsenseOrFrameshift)).isTrue()
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
        assertThat(MutationTypeMatching.matches(MutationType.MISSENSE, inframe)).isTrue()
        assertThat(MutationTypeMatching.matches(MutationType.INFRAME, inframe)).isTrue()
        assertThat(MutationTypeMatching.matches(MutationType.INFRAME_DELETION, inframe)).isFalse()
        assertThat(MutationTypeMatching.matches(MutationType.INFRAME_INSERTION, inframe)).isFalse()
    }

    @Test
    fun `Should match for inframe deletion`() {
        val inframeDeletion = VARIANT_CRITERIA.copy(
            codingEffect = CodingEffect.MISSENSE,
            type = VariantType.DELETE,
            ref = "ATGATG",
            alt = "TTT"
        )
        assertThat(MutationTypeMatching.matches(MutationType.MISSENSE, inframeDeletion)).isTrue()
        assertThat(MutationTypeMatching.matches(MutationType.INFRAME, inframeDeletion)).isTrue()
        assertThat(MutationTypeMatching.matches(MutationType.INFRAME_DELETION, inframeDeletion)).isTrue()
        assertThat(MutationTypeMatching.matches(MutationType.INFRAME_INSERTION, inframeDeletion)).isFalse()
        assertThat(MutationTypeMatching.matches(MutationType.ANY, inframeDeletion)).isTrue()
    }

    @Test
    fun `Should match for inframe insertion`() {
        val inframeInsertion = VARIANT_CRITERIA.copy(
            codingEffect = CodingEffect.MISSENSE,
            type = VariantType.INSERT,
            ref = "TTT",
            alt = "ATGATG"
        )
        assertThat(MutationTypeMatching.matches(MutationType.MISSENSE, inframeInsertion)).isTrue()
        assertThat(MutationTypeMatching.matches(MutationType.INFRAME, inframeInsertion)).isTrue()
        assertThat(MutationTypeMatching.matches(MutationType.INFRAME_DELETION, inframeInsertion)).isFalse()
        assertThat(MutationTypeMatching.matches(MutationType.INFRAME_INSERTION, inframeInsertion)).isTrue()
        assertThat(MutationTypeMatching.matches(MutationType.ANY, inframeInsertion)).isTrue()
    }

    @Test
    fun `Should match for snv missense`() {
        val missense = VARIANT_CRITERIA.copy(
            codingEffect = CodingEffect.MISSENSE,
            type = VariantType.SNV
        )
        assertThat(MutationTypeMatching.matches(MutationType.MISSENSE, missense)).isTrue()
        assertThat(MutationTypeMatching.matches(MutationType.INFRAME, missense)).isFalse()
        assertThat(MutationTypeMatching.matches(MutationType.INFRAME_DELETION, missense)).isFalse()
        assertThat(MutationTypeMatching.matches(MutationType.INFRAME_INSERTION, missense)).isFalse()
        assertThat(MutationTypeMatching.matches(MutationType.ANY, missense)).isTrue()
    }
}