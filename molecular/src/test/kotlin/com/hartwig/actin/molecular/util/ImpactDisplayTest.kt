package com.hartwig.actin.molecular.util

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class ImpactDisplayTest {

    @Test
    fun `Should format variant impact with protein impact`() {
        val result = formatVariantImpact("p.Met1Leu", "", false, false, "")
        assertThat(result).isEqualTo("M1L")
    }

    @Test
    fun `Should format variant impact with coding impact and splice`() {
        val result = formatVariantImpact("", "c.123A>T", true, false, "")
        assertThat(result).isEqualTo("c.123A>T splice")
    }

    @Test
    fun `Should format variant impact with coding impact`() {
        val result = formatVariantImpact("", "c.123A>T", false, false, "")
        assertThat(result).isEqualTo("c.123A>T")
    }

    @Test
    fun `Should format variant impact with upstream`() {
        val result = formatVariantImpact("", "", false, true, "")
        assertThat(result).isEqualTo("upstream")
    }

    @Test
    fun `Should format variant impact with effects`() {
        val result = formatVariantImpact("", "", false, false, "some_effect")
        assertThat(result).isEqualTo("some_effect")
    }

    @Test
    fun `Should prioritize protein impact over coding impact`() {
        val result = formatVariantImpact("p.Met1Leu", "c.123A>T", false, false, "")
        assertThat(result).isEqualTo("M1L")
    }

    @Test
    fun `Should prioritize protein impact over upstream`() {
        val result = formatVariantImpact("p.Met1Leu", "", false, true, "")
        assertThat(result).isEqualTo("M1L")
    }

    @Test
    fun `Should prioritize protein impact over effects`() {
        val result = formatVariantImpact("p.Met1Leu", "", false, false, "some_effect")
        assertThat(result).isEqualTo("M1L")
    }

    @Test
    fun `Should prioritize coding impact over upstream`() {
        val result = formatVariantImpact("", "c.123A>T", false, true, "")
        assertThat(result).isEqualTo("c.123A>T")
    }

    @Test
    fun `Should prioritize coding impact over effects`() {
        val result = formatVariantImpact("", "c.123A>T", false, false, "some_effect")
        assertThat(result).isEqualTo("c.123A>T")
    }
}