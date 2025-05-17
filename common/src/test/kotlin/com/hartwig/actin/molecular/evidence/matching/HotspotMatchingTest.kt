package com.hartwig.actin.molecular.evidence.matching

import com.hartwig.actin.datamodel.molecular.TestMolecularFactory
import com.hartwig.actin.molecular.evidence.TestServeMolecularFactory
import com.hartwig.actin.molecular.evidence.known.TestServeKnownFactory
import com.hartwig.serve.datamodel.molecular.hotspot.ImmutableActionableHotspot
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

private val KNOWN_HOTSPOT = TestServeKnownFactory.hotspotBuilder().gene("gene 1").chromosome("12").position(10).ref("A").alt("T").build()

private val VARIANT_ANNOTATION =
    TestServeMolecularFactory.createVariantAnnotation(gene = "gene 1", chromosome = "12", position = 10, ref = "A", alt = "T")
private val VARIANT_ANNOTATION2 =
    TestServeMolecularFactory.createVariantAnnotation(gene = "gene 1", chromosome = "12", position = 11, ref = "C", alt = "G")
private val ACTIONABLE_HOTSPOT = ImmutableActionableHotspot.builder()
    .from(TestServeMolecularFactory.createActionableEvent())
    .addVariants(VARIANT_ANNOTATION, VARIANT_ANNOTATION2)
    .build()

private val VARIANT = TestMolecularFactory.createMinimalVariant()
    .copy(gene = "gene 1", chromosome = "12", position = 10, ref = "A", alt = "T", isReportable = true)
private val VARIANT2 = TestMolecularFactory.createMinimalVariant()
    .copy(gene = "gene 1", chromosome = "12", position = 11, ref = "C", alt = "G", isReportable = true)

class HotspotMatchingTest {

    @Test
    fun `Should return true on matching hotspot`() {
        assertThat(HotspotMatching.isMatch(KNOWN_HOTSPOT, VARIANT)).isTrue()
        assertThat(HotspotMatching.isMatch(ACTIONABLE_HOTSPOT, VARIANT)).isTrue()
        assertThat(HotspotMatching.isMatch(ACTIONABLE_HOTSPOT, VARIANT2)).isTrue()
    }

    @Test
    fun `Should return false on non-matching gene to hotspot`() {
        assertThat(HotspotMatching.isMatch(KNOWN_HOTSPOT, VARIANT.copy(gene = "gene 2"))).isFalse()
        assertThat(HotspotMatching.isMatch(ACTIONABLE_HOTSPOT, VARIANT.copy(gene = "gene 2"))).isFalse()
    }

    @Test
    fun `Should return false on non-matching chromosome to hotspot`() {
        assertThat(HotspotMatching.isMatch(KNOWN_HOTSPOT, VARIANT.copy(chromosome = "13"))).isFalse()
        assertThat(HotspotMatching.isMatch(ACTIONABLE_HOTSPOT, VARIANT.copy(chromosome = "13"))).isFalse()
    }

    @Test
    fun `Should return false on non-matching position to hotspot`() {
        assertThat(HotspotMatching.isMatch(KNOWN_HOTSPOT, VARIANT.copy(position = 12))).isFalse()
        assertThat(HotspotMatching.isMatch(ACTIONABLE_HOTSPOT, VARIANT.copy(position = 12))).isFalse()
    }

    @Test
    fun `Should return false on non-matching ref to hotspot`() {
        assertThat(HotspotMatching.isMatch(KNOWN_HOTSPOT, VARIANT.copy(ref = "C"))).isFalse()
        assertThat(HotspotMatching.isMatch(ACTIONABLE_HOTSPOT, VARIANT.copy(ref = "C"))).isFalse()
    }

    @Test
    fun `Should return false on non-matching alt to hotspot`() {
        assertThat(HotspotMatching.isMatch(KNOWN_HOTSPOT, VARIANT.copy(alt = "G"))).isFalse()
        assertThat(HotspotMatching.isMatch(ACTIONABLE_HOTSPOT, VARIANT.copy(alt = "G"))).isFalse()
    }
}