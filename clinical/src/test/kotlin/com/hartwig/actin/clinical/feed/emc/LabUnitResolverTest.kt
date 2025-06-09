package com.hartwig.actin.clinical.feed.emc

import com.hartwig.actin.datamodel.clinical.LabUnit
import org.assertj.core.api.Assertions
import org.junit.Test

class LabUnitResolverTest {
    
    @Test
    fun canResolveLabUnits() {
        val unit = LabUnit.CELLS_PER_CUBIC_MILLIMETER
        Assertions.assertThat(LabUnitResolver.resolve(unit.display().lowercase())).isEqualTo(unit)
    }

    @Test
    fun canCurateLabUnits() {
        val firstCurated = LabUnitResolver.CURATION_MAP.keys.iterator().next()
        Assertions.assertThat(LabUnitResolver.resolve(firstCurated)).isEqualTo(LabUnitResolver.CURATION_MAP[firstCurated])
    }
}