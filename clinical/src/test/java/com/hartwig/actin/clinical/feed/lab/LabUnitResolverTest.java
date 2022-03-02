package com.hartwig.actin.clinical.feed.lab;

import static org.junit.Assert.assertEquals;

import com.hartwig.actin.clinical.datamodel.LabUnit;

import org.junit.Test;

public class LabUnitResolverTest {

    @Test
    public void canResolveLabUnits() {
        LabUnit unit = LabUnit.CELLS_PER_CUBIC_MILLIMETER;

        assertEquals(unit, LabUnitResolver.resolve(unit.display().toLowerCase()));
    }

    @Test
    public void canCurateLabUnits() {
        String firstCurated = LabUnitResolver.CURATION_MAP.keySet().iterator().next();

        LabUnit unit = LabUnitResolver.resolve(firstCurated);

        assertEquals(LabUnitResolver.CURATION_MAP.get(firstCurated), unit);
    }
}