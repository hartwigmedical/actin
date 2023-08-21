package com.hartwig.actin.report.interpretation

import com.hartwig.actin.clinical.datamodel.ImmutableTumorDetails
import com.hartwig.actin.report.interpretation.TumorDetailsInterpreter.isCUP
import org.junit.Assert
import org.junit.Test

class TumorDetailsInterpreterTest {
    @Test
    fun canDetermineIfTumorIsCUP() {
        Assert.assertFalse(isCUP(ImmutableTumorDetails.builder().build()))
        Assert.assertFalse(
            isCUP(
                ImmutableTumorDetails.builder()
                    .primaryTumorLocation(TumorDetailsInterpreter.CUP_LOCATION)
                    .build()
            )
        )
        Assert.assertTrue(
            isCUP(
                ImmutableTumorDetails.builder()
                    .primaryTumorLocation(TumorDetailsInterpreter.CUP_LOCATION)
                    .primaryTumorSubLocation(TumorDetailsInterpreter.CUP_SUB_LOCATION)
                    .build()
            )
        )
    }
}