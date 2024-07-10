package com.hartwig.actin.molecular.orange.interpretation

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class ExtractionUtilTest {

    @Test
    fun `Should keep three digits`() {
        assertThat(ExtractionUtil.keep3Digits(3.0)).isEqualTo(3.0)
        assertThat(ExtractionUtil.keep3Digits(3.123)).isEqualTo(3.123)
        assertThat(ExtractionUtil.keep3Digits(3.123456789)).isEqualTo(3.123)
    }
}