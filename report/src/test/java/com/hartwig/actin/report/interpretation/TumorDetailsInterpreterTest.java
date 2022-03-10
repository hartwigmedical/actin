package com.hartwig.actin.report.interpretation;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.hartwig.actin.clinical.datamodel.ImmutableTumorDetails;

import org.junit.Test;

public class TumorDetailsInterpreterTest {

    @Test
    public void canDetermineIfTumorIsCUP() {
        assertFalse(TumorDetailsInterpreter.isCUP(ImmutableTumorDetails.builder().build()));
        assertFalse(TumorDetailsInterpreter.isCUP(ImmutableTumorDetails.builder().primaryTumorLocation(TumorDetailsInterpreter.CUP_LOCATION).build()));
        assertTrue(TumorDetailsInterpreter.isCUP(ImmutableTumorDetails.builder()
                .primaryTumorLocation(TumorDetailsInterpreter.CUP_LOCATION)
                .primaryTumorSubLocation(TumorDetailsInterpreter.CUP_SUB_LOCATION)
                .build()));
    }
}