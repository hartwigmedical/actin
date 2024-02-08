package com.hartwig.actin.molecular.orange.evidence.matching

import com.hartwig.actin.molecular.datamodel.driver.CodingEffect
import com.hartwig.actin.molecular.orange.evidence.TestMolecularFactory.minimalTestVariant
import com.hartwig.actin.molecular.orange.evidence.TestMolecularFactory.minimalTranscriptImpact
import com.hartwig.actin.molecular.orange.evidence.known.TestServeKnownFactory
import com.hartwig.serve.datamodel.MutationType
import com.hartwig.serve.datamodel.range.RangeAnnotation
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RangeMatchingTest {

    @Test
    fun canMatchRanges() {
        val range: RangeAnnotation = TestServeKnownFactory.codonBuilder()
            .gene("gene 1")
            .chromosome("12")
            .start(12)
            .end(14)
            .applicableMutationType(MutationType.ANY)
            .build()

//        val match: PurpleVariant = TestPurpleFactory.variantBuilder()
//            .gene("gene 1")
//            .chromosome("12")
//            .position(13)
//            .canonicalImpact(TestPurpleFactory.transcriptImpactBuilder().codingEffect(PurpleCodingEffect.MISSENSE).build())
//            .build()
        val match = minimalTestVariant().copy(
            gene = "gene 1",
            chromosome = "12",
            position = 13,
            canonicalImpact = minimalTranscriptImpact().copy(codingEffect = CodingEffect.MISSENSE)
        )
        assertTrue(RangeMatching.isMatch(range, match))

//        val wrongGene: PurpleVariant = TestPurpleFactory.variantBuilder().from(match).gene("gene 2").build()
        val wrongGene = match.copy(gene = "gene 2")
        assertFalse(RangeMatching.isMatch(range, wrongGene))

//        val wrongPosition: PurpleVariant = TestPurpleFactory.variantBuilder().from(match).position(5).build()
        val wrongPosition = match.copy(position = 5)
        assertFalse(RangeMatching.isMatch(range, wrongPosition))

//        val noImpact: PurpleVariant = TestPurpleFactory.variantBuilder()
//            .from(match)
//            .canonicalImpact(TestPurpleFactory.transcriptImpactBuilder().codingEffect(PurpleCodingEffect.NONE).build())
//            .build()
        val noImpact = match.copy(canonicalImpact = minimalTranscriptImpact().copy(codingEffect = CodingEffect.NONE))
        assertFalse(RangeMatching.isMatch(range, noImpact))
    }
}