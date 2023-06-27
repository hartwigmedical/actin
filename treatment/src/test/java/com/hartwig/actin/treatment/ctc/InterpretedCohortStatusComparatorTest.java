package com.hartwig.actin.treatment.ctc;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

public class InterpretedCohortStatusComparatorTest {

    @Test
    public void shouldRankCohortStatesCorrectly() {
        InterpretedCohortStatus openWithSlots = ImmutableInterpretedCohortStatus.builder().open(true).slotsAvailable(true).build();
        InterpretedCohortStatus openWithoutSlots = ImmutableInterpretedCohortStatus.builder().open(true).slotsAvailable(false).build();
        InterpretedCohortStatus closedWithoutSlots = ImmutableInterpretedCohortStatus.builder().open(false).slotsAvailable(false).build();

        List<InterpretedCohortStatus> states = new ArrayList<>();
        states.add(openWithoutSlots);
        states.add(openWithSlots);
        states.add(closedWithoutSlots);

        states.sort(new InterpretedCohortStatusComparator());

        assertEquals(closedWithoutSlots, states.get(0));
        assertEquals(openWithoutSlots, states.get(1));
        assertEquals(openWithSlots, states.get(2));
    }

}