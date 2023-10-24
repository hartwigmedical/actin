package com.hartwig.actin.molecular.orange.evidence.matching

import com.hartwig.actin.molecular.orange.datamodel.purple.TestPurpleFactory
import com.hartwig.hmftools.datamodel.purple.ImmutablePurpleVariant
import com.hartwig.hmftools.datamodel.purple.PurpleCodingEffect
import com.hartwig.hmftools.datamodel.purple.PurpleVariant
import com.hartwig.hmftools.datamodel.purple.PurpleVariantType
import com.hartwig.serve.datamodel.MutationType
import org.junit.Assert
import org.junit.Test

class MutationTypeMatchingTest {
    @Test
    fun worksForEveryCodingEffect() {
        val nonCoding: PurpleVariant? = withCodingEffect(PurpleCodingEffect.NONE).build()
        for (type in MutationType.values()) {
            Assert.assertFalse(MutationTypeMatching.matches(type, nonCoding))
        }
    }

    @Test
    fun canMatchMutationTypes() {
        val nonsenseOrFrameshift: PurpleVariant? = withCodingEffect(PurpleCodingEffect.NONSENSE_OR_FRAMESHIFT).build()
        Assert.assertTrue(MutationTypeMatching.matches(MutationType.NONSENSE_OR_FRAMESHIFT, nonsenseOrFrameshift))
        Assert.assertTrue(MutationTypeMatching.matches(MutationType.ANY, nonsenseOrFrameshift))
        val splice: PurpleVariant? = withCodingEffect(PurpleCodingEffect.SPLICE).build()
        Assert.assertTrue(MutationTypeMatching.matches(MutationType.SPLICE, splice))
        Assert.assertTrue(MutationTypeMatching.matches(MutationType.ANY, splice))
        val inframe: PurpleVariant? = withCodingEffect(PurpleCodingEffect.MISSENSE).type(PurpleVariantType.INDEL).ref("AAG").alt("TTG").build()
        Assert.assertTrue(MutationTypeMatching.matches(MutationType.MISSENSE, inframe))
        Assert.assertTrue(MutationTypeMatching.matches(MutationType.INFRAME, inframe))
        Assert.assertFalse(MutationTypeMatching.matches(MutationType.INFRAME_DELETION, inframe))
        Assert.assertFalse(MutationTypeMatching.matches(MutationType.INFRAME_INSERTION, inframe))
        Assert.assertTrue(MutationTypeMatching.matches(MutationType.ANY, splice))
        val inframeDeletion: PurpleVariant? = withCodingEffect(PurpleCodingEffect.MISSENSE).type(PurpleVariantType.INDEL).ref("ATGATG").alt("TTT").build()
        Assert.assertTrue(MutationTypeMatching.matches(MutationType.MISSENSE, inframeDeletion))
        Assert.assertTrue(MutationTypeMatching.matches(MutationType.INFRAME, inframeDeletion))
        Assert.assertTrue(MutationTypeMatching.matches(MutationType.INFRAME_DELETION, inframeDeletion))
        Assert.assertFalse(MutationTypeMatching.matches(MutationType.INFRAME_INSERTION, inframeDeletion))
        Assert.assertTrue(MutationTypeMatching.matches(MutationType.ANY, inframeDeletion))
        val inframeInsertion: PurpleVariant? = withCodingEffect(PurpleCodingEffect.MISSENSE).type(PurpleVariantType.INDEL).ref("TTT").alt("ATGATG").build()
        Assert.assertTrue(MutationTypeMatching.matches(MutationType.MISSENSE, inframeInsertion))
        Assert.assertTrue(MutationTypeMatching.matches(MutationType.INFRAME, inframeInsertion))
        Assert.assertFalse(MutationTypeMatching.matches(MutationType.INFRAME_DELETION, inframeInsertion))
        Assert.assertTrue(MutationTypeMatching.matches(MutationType.INFRAME_INSERTION, inframeInsertion))
        Assert.assertTrue(MutationTypeMatching.matches(MutationType.ANY, inframeInsertion))
        val missense: PurpleVariant? = withCodingEffect(PurpleCodingEffect.MISSENSE).type(PurpleVariantType.SNP).build()
        Assert.assertTrue(MutationTypeMatching.matches(MutationType.MISSENSE, missense))
        Assert.assertFalse(MutationTypeMatching.matches(MutationType.INFRAME, missense))
        Assert.assertFalse(MutationTypeMatching.matches(MutationType.INFRAME_DELETION, missense))
        Assert.assertFalse(MutationTypeMatching.matches(MutationType.INFRAME_INSERTION, missense))
        Assert.assertTrue(MutationTypeMatching.matches(MutationType.ANY, missense))
    }

    companion object {
        private fun withCodingEffect(codingEffect: PurpleCodingEffect): ImmutablePurpleVariant.Builder {
            return TestPurpleFactory.variantBuilder()
                .canonicalImpact(TestPurpleFactory.transcriptImpactBuilder().codingEffect(codingEffect).build())
        }
    }
}