package com.hartwig.actin.clinical.feed.lab

import com.hartwig.actin.clinical.datamodel.LabUnit
import com.hartwig.actin.clinical.feed.lab.LabUnitResolver.resolve
import org.junit.Assert
import org.junit.Test
import java.util.*

class LabUnitResolverTest {
    @Test
    fun canResolveLabUnits() {
        val unit = LabUnit.CELLS_PER_CUBIC_MILLIMETER
        Assert.assertEquals(unit, resolve(unit.display().lowercase(Locale.getDefault())))
    }

    @Test
    fun canCurateLabUnits() {
        val firstCurated = LabUnitResolver.CURATION_MAP.keys.iterator().next()
        val unit = resolve(firstCurated)
        Assert.assertEquals(LabUnitResolver.CURATION_MAP[firstCurated], unit)
    }
}