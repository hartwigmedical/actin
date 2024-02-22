package com.hartwig.actin.molecular.evidence.matching

import com.hartwig.actin.molecular.evidence.TestMolecularFactory.minimalVariant
import com.hartwig.actin.molecular.evidence.known.TestServeKnownFactory
import com.hartwig.serve.datamodel.hotspot.VariantHotspot
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class HotspotMatchingTest {

    @Test
    fun canMatchHotspots() {
        val hotspot: VariantHotspot = TestServeKnownFactory.hotspotBuilder().gene("gene 1").chromosome("12").position(10).ref("A").alt("T").build()
        val match = minimalVariant().copy(gene = "gene 1", chromosome = "12", position = 10, ref = "A", alt = "T")
        assertTrue(HotspotMatching.isMatch(hotspot, match))

        val wrongGene = match.copy(gene = "gene 2")
        assertFalse(HotspotMatching.isMatch(hotspot, wrongGene))

        val wrongChromosome = match.copy(chromosome = "13")
        assertFalse(HotspotMatching.isMatch(hotspot, wrongChromosome))

        val wrongPosition = match.copy(position = 12)
        assertFalse(HotspotMatching.isMatch(hotspot, wrongPosition))

        val wrongRef = match.copy(ref = "C")
        assertFalse(HotspotMatching.isMatch(hotspot, wrongRef))

        val wrongAlt = match.copy(alt = "G")
        assertFalse(HotspotMatching.isMatch(hotspot, wrongAlt))
    }
}