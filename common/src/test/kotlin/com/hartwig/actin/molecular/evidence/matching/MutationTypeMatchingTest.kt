package com.hartwig.actin.molecular.evidence.matching

import com.hartwig.actin.datamodel.molecular.TestMolecularFactory
import com.hartwig.actin.datamodel.molecular.driver.CodingEffect
import com.hartwig.actin.datamodel.molecular.driver.Variant
import com.hartwig.actin.datamodel.molecular.driver.VariantType
import com.hartwig.serve.datamodel.molecular.MutationType
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

private val VARIANT_CRITERIA = TestMolecularFactory.createMinimalVariant().copy(
    gene = "gene 1",
    canonicalImpact = TestMolecularFactory.createMinimalTranscriptImpact().copy(codingEffect = CodingEffect.MISSENSE),
    type = VariantType.SNV,
    chromosome = "12",
    position = 13,
    ref = "A",
    alt = "T",
    isReportable = true
)

class MutationTypeMatchingTest {

    @Test
    fun `Should not match for all mutation types when coding effect is none`() {
        val nonCoding = VARIANT_CRITERIA.withCodingEffect(CodingEffect.NONE)
        val nothing = emptySet<MutationType>()
        assertMatch(nonCoding, nothing)
    }

    @Test
    fun `Should match for nonsense or frameshift`() {
        val nonsenseOrFrameshift = VARIANT_CRITERIA.withCodingEffect(CodingEffect.NONSENSE_OR_FRAMESHIFT)
        assertMatch(nonsenseOrFrameshift, setOf(MutationType.NONSENSE_OR_FRAMESHIFT, MutationType.ANY))
    }

    @Test
    fun `Should match for splice`() {
        val splice = VARIANT_CRITERIA.withCodingEffect(CodingEffect.SPLICE)
        assertThat(MutationTypeMatching.matches(MutationType.SPLICE, splice)).isTrue()
        assertThat(MutationTypeMatching.matches(MutationType.ANY, splice)).isTrue()
    }

    @Test
    fun `Should match for inframe missense`() {
        val inframe = VARIANT_CRITERIA.copy(
            type = VariantType.MNV,
            ref = "AAG",
            alt = "TTG"
        ).withCodingEffect(CodingEffect.MISSENSE)

        assertMatch(inframe, setOf(MutationType.MISSENSE, MutationType.INFRAME, MutationType.ANY))
    }

    @Test
    fun `Should match for inframe deletion`() {
        val inframeDeletion = VARIANT_CRITERIA.copy(
            type = VariantType.DELETE,
            ref = "ATGATG",
            alt = "TTT"
        ).withCodingEffect(CodingEffect.MISSENSE)
        assertMatch(inframeDeletion, setOf(MutationType.MISSENSE, MutationType.INFRAME, MutationType.INFRAME_DELETION, MutationType.ANY))
    }

    @Test
    fun `Should match for inframe insertion`() {
        val inframeInsertion = VARIANT_CRITERIA.copy(
            type = VariantType.INSERT,
            ref = "TTT",
            alt = "ATGATG"
        ).withCodingEffect(CodingEffect.MISSENSE)
        assertMatch(inframeInsertion, setOf(MutationType.MISSENSE, MutationType.INFRAME, MutationType.INFRAME_INSERTION, MutationType.ANY))
    }

    @Test
    fun `Should match for snv missense`() {
        val missense = VARIANT_CRITERIA.copy(type = VariantType.SNV)
            .withCodingEffect(CodingEffect.MISSENSE)
        assertMatch(missense, setOf(MutationType.MISSENSE, MutationType.ANY))
    }

    private fun assertMatch(nonsenseOrFrameshift: Variant, matchingTypes: Set<MutationType>) {
        matchingTypes.forEach {
            assertThat(MutationTypeMatching.matches(it, nonsenseOrFrameshift)).withFailMessage { "Expected $it to match" }.isTrue()
        }
        (MutationType.values().toSet() - matchingTypes).forEach {
            assertThat(MutationTypeMatching.matches(it, nonsenseOrFrameshift)).withFailMessage { "Expected $it not to match" }.isFalse()
        }
    }


    private fun Variant.withCodingEffect(codingEffect: CodingEffect): Variant {
        return this.copy(canonicalImpact = this.canonicalImpact.copy(codingEffect = codingEffect))
    }
}