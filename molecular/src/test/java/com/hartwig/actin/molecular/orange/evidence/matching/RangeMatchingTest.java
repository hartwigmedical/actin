package com.hartwig.actin.molecular.orange.evidence.matching;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.hartwig.hmftools.datamodel.purple.PurpleCodingEffect;
import com.hartwig.hmftools.datamodel.purple.PurpleVariant;
import com.hartwig.actin.molecular.orange.datamodel.purple.TestPurpleFactory;
import com.hartwig.actin.molecular.orange.evidence.known.TestServeKnownFactory;
import com.hartwig.serve.datamodel.MutationType;
import com.hartwig.serve.datamodel.range.RangeAnnotation;

import org.junit.Test;

public class RangeMatchingTest {

    @Test
    public void canMatchRanges() {
        RangeAnnotation range = TestServeKnownFactory.codonBuilder()
                .gene("gene 1")
                .chromosome("12")
                .start(12)
                .end(14)
                .applicableMutationType(MutationType.ANY)
                .build();

        PurpleVariant match = TestPurpleFactory.variantBuilder()
                .gene("gene 1")
                .chromosome("12")
                .position(13)
                .canonicalImpact(TestPurpleFactory.transcriptImpactBuilder().codingEffect(PurpleCodingEffect.MISSENSE).build())
                .build();
        assertTrue(RangeMatching.isMatch(range, match));

        PurpleVariant wrongGene = TestPurpleFactory.variantBuilder().from(match).gene("gene 2").build();
        assertFalse(RangeMatching.isMatch(range, wrongGene));

        PurpleVariant wrongPosition = TestPurpleFactory.variantBuilder().from(match).position(5).build();
        assertFalse(RangeMatching.isMatch(range, wrongPosition));

        PurpleVariant noImpact = TestPurpleFactory.variantBuilder()
                .from(match)
                .canonicalImpact(TestPurpleFactory.transcriptImpactBuilder().codingEffect(PurpleCodingEffect.NONE).build())
                .build();
        assertFalse(RangeMatching.isMatch(range, noImpact));
    }
}