package com.hartwig.actin.util

import com.hartwig.actin.util.TabularFile.createFields
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class TabularFileTest {

    @Test
    fun `Should create fields`() {
        val header = arrayOf("header0", "header1", "header2")
        val fields = createFields(header)
        assertThat(fields["header0"]).isEqualTo(0)
        assertThat(fields["header1"]).isEqualTo(1)
        assertThat(fields["header2"]).isEqualTo(2)
    }
}