package com.hartwig.actin.clinical.feed.lab

import com.hartwig.actin.clinical.datamodel.LabUnit
import com.hartwig.actin.clinical.feed.lab.LabUnitResolver.resolve
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class LabUnitResolverTest {
    @Test
    fun canResolveLabUnits() {
        val unit = LabUnit.CELLS_PER_CUBIC_MILLIMETER
        assertThat(resolve(unit.display().lowercase())).isEqualTo(unit)
    }

    @Test
    fun canCurateLabUnits() {
        val firstCurated = LabUnitResolver.CURATION_MAP.keys.iterator().next()
        assertThat(resolve(firstCurated)).isEqualTo(LabUnitResolver.CURATION_MAP[firstCurated])
    }
}