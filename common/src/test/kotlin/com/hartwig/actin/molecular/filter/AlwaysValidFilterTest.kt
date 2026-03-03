package com.hartwig.actin.molecular.filter

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class AlwaysValidFilterTest {

    @Test
    fun `Should return true for any input`() {
        assertThat(AlwaysValidFilter().include("not a gene")).isTrue
    }
}