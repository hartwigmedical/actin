package com.hartwig.actin.molecular.orange.evidence.matching

import com.hartwig.actin.molecular.orange.evidence.TestMolecularFactory.minimalTestVariant
import com.hartwig.actin.molecular.orange.evidence.known.TestServeKnownFactory
import com.hartwig.serve.datamodel.hotspot.VariantHotspot
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class HotspotMatchingTest {

    @Test
    fun canMatchHotspots() {
        val hotspot: VariantHotspot = TestServeKnownFactory.hotspotBuilder().gene("gene 1").chromosome("12").position(10).ref("A").alt("T").build()
//        val match: PurpleVariant = TestPurpleFactory.variantBuilder().gene("gene 1").chromosome("12").position(10).ref("A").alt("T").build()
        val match = minimalTestVariant().copy(gene = "gene 1", chromosome = "12", position = 10, ref = "A", alt = "T")
        assertTrue(HotspotMatching.isMatch(hotspot, match))

//        val wrongGene: PurpleVariant = TestPurpleFactory.variantBuilder().from(match).gene("gene 2").build()
        val wrongGene = match.copy(gene = "gene 2")
        assertFalse(HotspotMatching.isMatch(hotspot, wrongGene))

//        val wrongChromosome: PurpleVariant = TestPurpleFactory.variantBuilder().from(match).chromosome("13").build()
        val wrongChromosome = match.copy(chromosome = "13")
        assertFalse(HotspotMatching.isMatch(hotspot, wrongChromosome))

//        val wrongPosition: PurpleVariant = TestPurpleFactory.variantBuilder().from(match).position(12).build()
        val wrongPosition = match.copy(position = 12)
        assertFalse(HotspotMatching.isMatch(hotspot, wrongPosition))

//        val wrongRef: PurpleVariant = TestPurpleFactory.variantBuilder().from(match).ref("C").build()
        val wrongRef = match.copy(ref = "C")
        assertFalse(HotspotMatching.isMatch(hotspot, wrongRef))

//        val wrongAlt: PurpleVariant = TestPurpleFactory.variantBuilder().from(match).alt("G").build()
        val wrongAlt = match.copy(alt = "G")
        assertFalse(HotspotMatching.isMatch(hotspot, wrongAlt))
    }
}