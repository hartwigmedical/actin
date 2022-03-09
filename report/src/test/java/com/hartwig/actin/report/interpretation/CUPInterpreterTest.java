package com.hartwig.actin.report.interpretation;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.hartwig.actin.clinical.datamodel.ImmutableTumorDetails;

import org.junit.Test;

public class CUPInterpreterTest {

    @Test
    public void canDetermineIfTumorIsCUP() {
        assertFalse(CUPInterpreter.isCUP(ImmutableTumorDetails.builder().build()));
        assertFalse(CUPInterpreter.isCUP(ImmutableTumorDetails.builder().primaryTumorLocation(CUPInterpreter.CUP_LOCATION).build()));
        assertTrue(CUPInterpreter.isCUP(ImmutableTumorDetails.builder()
                .primaryTumorLocation(CUPInterpreter.CUP_LOCATION)
                .primaryTumorSubLocation(CUPInterpreter.CUP_SUB_LOCATION)
                .build()));
    }
}