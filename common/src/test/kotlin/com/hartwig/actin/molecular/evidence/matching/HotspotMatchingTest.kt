package com.hartwig.actin.molecular.evidence.matching

import com.hartwig.actin.molecular.evidence.known.TestServeKnownFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

private val HOTSPOT = TestServeKnownFactory.hotspotBuilder().gene("gene 1").chromosome("12").position(10).ref("A").alt("T").build()

private val MATCHING_CRITERIA =
    VariantMatchCriteria(gene = "gene 1", chromosome = "12", position = 10, ref = "A", alt = "T", isReportable = true)

class HotspotMatchingTest {

    @Test
    fun `Should return true on matching hotspot`() {
        assertThat(HotspotMatching.isMatch(HOTSPOT, MATCHING_CRITERIA)).isTrue()
    }

    @Test
    fun `Should return false on non-matching gene`() {
        assertThat(HotspotMatching.isMatch(HOTSPOT, MATCHING_CRITERIA.copy(gene = "gene 2"))).isFalse()
    }

    @Test
    fun `Should return false on non-matching chromosome`() {
        assertThat(HotspotMatching.isMatch(HOTSPOT, MATCHING_CRITERIA.copy(chromosome = "13"))).isFalse()
    }

    @Test
    fun `Should return false on non-matching position`() {
        assertThat(HotspotMatching.isMatch(HOTSPOT, MATCHING_CRITERIA.copy(position = 12))).isFalse()
    }

    @Test
    fun `Should return false on non-matching ref`() {
        assertThat(HotspotMatching.isMatch(HOTSPOT, MATCHING_CRITERIA.copy(ref = "C"))).isFalse()
    }

    @Test
    fun `Should return false on non-matching alt`() {
        assertThat(HotspotMatching.isMatch(HOTSPOT, MATCHING_CRITERIA.copy(alt = "G"))).isFalse()
    }
}