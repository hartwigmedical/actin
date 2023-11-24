package com.hartwig.actin.molecular.orange.evidence.matching

import com.hartwig.actin.molecular.orange.datamodel.purple.TestPurpleFactory
import com.hartwig.hmftools.datamodel.purple.ImmutablePurpleVariant
import com.hartwig.hmftools.datamodel.purple.PurpleCodingEffect
import com.hartwig.hmftools.datamodel.purple.PurpleVariant
import com.hartwig.hmftools.datamodel.purple.PurpleVariantType
import com.hartwig.serve.datamodel.MutationType
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MutationTypeMatchingTest {

    @Test
    fun worksForEveryCodingEffect() {
        val nonCoding: PurpleVariant = withCodingEffect(PurpleCodingEffect.NONE).build()
        for (type in MutationType.values()) {
            assertFalse(MutationTypeMatching.matches(type, nonCoding))
        }
    }

    @Test
    fun canMatchMutationTypes() {
        val nonsenseOrFrameshift: PurpleVariant = withCodingEffect(PurpleCodingEffect.NONSENSE_OR_FRAMESHIFT).build()
        assertTrue(MutationTypeMatching.matches(MutationType.NONSENSE_OR_FRAMESHIFT, nonsenseOrFrameshift))
        assertTrue(MutationTypeMatching.matches(MutationType.ANY, nonsenseOrFrameshift))

        val splice: PurpleVariant = withCodingEffect(PurpleCodingEffect.SPLICE).build()
        assertTrue(MutationTypeMatching.matches(MutationType.SPLICE, splice))
        assertTrue(MutationTypeMatching.matches(MutationType.ANY, splice))

        val inframe: PurpleVariant = withCodingEffect(PurpleCodingEffect.MISSENSE).type(PurpleVariantType.INDEL).ref("AAG").alt("TTG").build()
        assertTrue(MutationTypeMatching.matches(MutationType.MISSENSE, inframe))
        assertTrue(MutationTypeMatching.matches(MutationType.INFRAME, inframe))
        assertFalse(MutationTypeMatching.matches(MutationType.INFRAME_DELETION, inframe))
        assertFalse(MutationTypeMatching.matches(MutationType.INFRAME_INSERTION, inframe))
        assertTrue(MutationTypeMatching.matches(MutationType.ANY, splice))

        val inframeDeletion: PurpleVariant = withCodingEffect(PurpleCodingEffect.MISSENSE).type(PurpleVariantType.INDEL).ref("ATGATG").alt("TTT").build()
        assertTrue(MutationTypeMatching.matches(MutationType.MISSENSE, inframeDeletion))
        assertTrue(MutationTypeMatching.matches(MutationType.INFRAME, inframeDeletion))
        assertTrue(MutationTypeMatching.matches(MutationType.INFRAME_DELETION, inframeDeletion))
        assertFalse(MutationTypeMatching.matches(MutationType.INFRAME_INSERTION, inframeDeletion))
        assertTrue(MutationTypeMatching.matches(MutationType.ANY, inframeDeletion))

        val inframeInsertion: PurpleVariant = withCodingEffect(PurpleCodingEffect.MISSENSE).type(PurpleVariantType.INDEL).ref("TTT").alt("ATGATG").build()
        assertTrue(MutationTypeMatching.matches(MutationType.MISSENSE, inframeInsertion))
        assertTrue(MutationTypeMatching.matches(MutationType.INFRAME, inframeInsertion))
        assertFalse(MutationTypeMatching.matches(MutationType.INFRAME_DELETION, inframeInsertion))
        assertTrue(MutationTypeMatching.matches(MutationType.INFRAME_INSERTION, inframeInsertion))
        assertTrue(MutationTypeMatching.matches(MutationType.ANY, inframeInsertion))

        val missense: PurpleVariant = withCodingEffect(PurpleCodingEffect.MISSENSE).type(PurpleVariantType.SNP).build()
        assertTrue(MutationTypeMatching.matches(MutationType.MISSENSE, missense))
        assertFalse(MutationTypeMatching.matches(MutationType.INFRAME, missense))
        assertFalse(MutationTypeMatching.matches(MutationType.INFRAME_DELETION, missense))
        assertFalse(MutationTypeMatching.matches(MutationType.INFRAME_INSERTION, missense))
        assertTrue(MutationTypeMatching.matches(MutationType.ANY, missense))
    }

    companion object {
        private fun withCodingEffect(codingEffect: PurpleCodingEffect): ImmutablePurpleVariant.Builder {
            return TestPurpleFactory.variantBuilder()
                .canonicalImpact(TestPurpleFactory.transcriptImpactBuilder().codingEffect(codingEffect).build())
        }
    }
}