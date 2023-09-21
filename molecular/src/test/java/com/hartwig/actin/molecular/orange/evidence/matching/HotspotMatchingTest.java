package com.hartwig.actin.molecular.orange.evidence.matching;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.hartwig.hmftools.datamodel.purple.PurpleVariant;
import com.hartwig.actin.molecular.orange.datamodel.purple.TestPurpleFactory;
import com.hartwig.actin.molecular.orange.evidence.known.TestServeKnownFactory;
import com.hartwig.serve.datamodel.hotspot.VariantHotspot;

import org.junit.Test;

public class HotspotMatchingTest {

    @Test
    public void canMatchHotspots() {
        VariantHotspot hotspot =
                TestServeKnownFactory.hotspotBuilder().gene("gene 1").chromosome("12").position(10).ref("A").alt("T").build();

        PurpleVariant match = TestPurpleFactory.variantBuilder().gene("gene 1").chromosome("12").position(10).ref("A").alt("T").build();
        assertTrue(HotspotMatching.isMatch(hotspot, match));

        PurpleVariant wrongGene = TestPurpleFactory.variantBuilder().from(match).gene("gene 2").build();
        assertFalse(HotspotMatching.isMatch(hotspot, wrongGene));

        PurpleVariant wrongChromosome = TestPurpleFactory.variantBuilder().from(match).chromosome("13").build();
        assertFalse(HotspotMatching.isMatch(hotspot, wrongChromosome));

        PurpleVariant wrongPosition = TestPurpleFactory.variantBuilder().from(match).position(12).build();
        assertFalse(HotspotMatching.isMatch(hotspot, wrongPosition));

        PurpleVariant wrongRef = TestPurpleFactory.variantBuilder().from(match).ref("C").build();
        assertFalse(HotspotMatching.isMatch(hotspot, wrongRef));

        PurpleVariant wrongAlt = TestPurpleFactory.variantBuilder().from(match).alt("G").build();
        assertFalse(HotspotMatching.isMatch(hotspot, wrongAlt));
    }
}