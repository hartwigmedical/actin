package com.hartwig.actin.molecular.filter

import com.hartwig.actin.datamodel.molecular.TestMolecularFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

private val BASE_WGS_TEST = TestMolecularFactory.createMinimalWholeGenomeTest()

class MolecularTestFilterTest {

    @Test
    fun `Should filter out records with insufficient quality if useInsufficientQualityRecords is false`() {
        val filterInsufficientQuality = MolecularTestFilter(false)
        val filtered = filterInsufficientQuality.apply(listOf(BASE_WGS_TEST, BASE_WGS_TEST.copy(hasSufficientQuality = false)))
        assertThat(filtered).containsOnly(BASE_WGS_TEST)
    }
}