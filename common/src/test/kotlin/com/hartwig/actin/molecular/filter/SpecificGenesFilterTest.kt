package com.hartwig.actin.molecular.filter

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class SpecificGenesFilterTest {

    @Test
    fun `Should filter specific genes`() {
        val filter = SpecificGenesFilter(setOf("gene A"))
        assertThat(filter.include("gene A")).isTrue
        assertThat(filter.include("gene B")).isFalse
    }
}