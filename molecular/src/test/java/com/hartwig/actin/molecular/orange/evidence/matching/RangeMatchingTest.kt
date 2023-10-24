package com.hartwig.actin.molecular.orange.evidence.matching

import com.hartwig.actin.molecular.orange.datamodel.purple.TestPurpleFactory
import com.hartwig.actin.molecular.orange.evidence.known.TestServeKnownFactory
import com.hartwig.hmftools.datamodel.purple.PurpleCodingEffect
import com.hartwig.hmftools.datamodel.purple.PurpleVariant
import com.hartwig.serve.datamodel.MutationType
import com.hartwig.serve.datamodel.range.RangeAnnotation
import org.junit.Assert
import org.junit.Test

class RangeMatchingTest {
    @Test
    fun canMatchRanges() {
        val range: RangeAnnotation? = TestServeKnownFactory.codonBuilder()
            .gene("gene 1")
            .chromosome("12")
            .start(12)
            .end(14)
            .applicableMutationType(MutationType.ANY)
            .build()
        val match: PurpleVariant? = TestPurpleFactory.variantBuilder()
            .gene("gene 1")
            .chromosome("12")
            .position(13)
            .canonicalImpact(TestPurpleFactory.transcriptImpactBuilder().codingEffect(PurpleCodingEffect.MISSENSE).build())
            .build()
        Assert.assertTrue(RangeMatching.isMatch(range, match))
        val wrongGene: PurpleVariant? = TestPurpleFactory.variantBuilder().from(match).gene("gene 2").build()
        Assert.assertFalse(RangeMatching.isMatch(range, wrongGene))
        val wrongPosition: PurpleVariant? = TestPurpleFactory.variantBuilder().from(match).position(5).build()
        Assert.assertFalse(RangeMatching.isMatch(range, wrongPosition))
        val noImpact: PurpleVariant? = TestPurpleFactory.variantBuilder()
            .from(match)
            .canonicalImpact(TestPurpleFactory.transcriptImpactBuilder().codingEffect(PurpleCodingEffect.NONE).build())
            .build()
        Assert.assertFalse(RangeMatching.isMatch(range, noImpact))
    }
}