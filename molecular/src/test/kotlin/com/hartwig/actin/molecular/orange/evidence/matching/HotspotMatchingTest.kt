package com.hartwig.actin.molecular.orange.evidence.matching

import com.hartwig.actin.molecular.orange.datamodel.purple.TestPurpleFactory
import com.hartwig.actin.molecular.orange.evidence.known.TestServeKnownFactory
import com.hartwig.hmftools.datamodel.purple.PurpleVariant
import com.hartwig.serve.datamodel.hotspot.VariantHotspot
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class HotspotMatchingTest {

    @Test
    fun canMatchHotspots() {
        val hotspot: VariantHotspot = TestServeKnownFactory.hotspotBuilder().gene("gene 1").chromosome("12").position(10).ref("A").alt("T").build()
        val match: PurpleVariant = TestPurpleFactory.variantBuilder().gene("gene 1").chromosome("12").position(10).ref("A").alt("T").build()
        assertTrue(HotspotMatching.isMatch(hotspot, match))

        val wrongGene: PurpleVariant = TestPurpleFactory.variantBuilder().from(match).gene("gene 2").build()
        assertFalse(HotspotMatching.isMatch(hotspot, wrongGene))

        val wrongChromosome: PurpleVariant = TestPurpleFactory.variantBuilder().from(match).chromosome("13").build()
        assertFalse(HotspotMatching.isMatch(hotspot, wrongChromosome))

        val wrongPosition: PurpleVariant = TestPurpleFactory.variantBuilder().from(match).position(12).build()
        assertFalse(HotspotMatching.isMatch(hotspot, wrongPosition))

        val wrongRef: PurpleVariant = TestPurpleFactory.variantBuilder().from(match).ref("C").build()
        assertFalse(HotspotMatching.isMatch(hotspot, wrongRef))

        val wrongAlt: PurpleVariant = TestPurpleFactory.variantBuilder().from(match).alt("G").build()
        assertFalse(HotspotMatching.isMatch(hotspot, wrongAlt))
    }
}