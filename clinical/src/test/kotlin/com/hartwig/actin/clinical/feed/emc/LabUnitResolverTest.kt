package com.hartwig.actin.clinical.feed.emc

import com.hartwig.actin.datamodel.clinical.LabUnit
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class LabUnitResolverTest {

    @Test
    fun `Should resolve lab units`() {
        val unit = LabUnit.CELLS_PER_CUBIC_MILLIMETER
        assertThat(LabUnitResolver.resolve(unit.display().lowercase())).isEqualTo(unit)
    }

    @Test
    fun `Should curate lab units`() {
        val firstCurated = LabUnitResolver.CURATION_MAP.keys.iterator().next()
        assertThat(LabUnitResolver.resolve(firstCurated)).isEqualTo(LabUnitResolver.CURATION_MAP[firstCurated])
    }
}