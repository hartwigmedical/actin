package com.hartwig.actin.report.interpretation

import com.hartwig.actin.clinical.datamodel.ImmutableTumorDetails
import com.hartwig.actin.report.interpretation.TumorDetailsInterpreter.isCUP
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class TumorDetailsInterpreterTest {

    @Test
    fun shouldCorrectlyDetermineIfTumorIsCUP() {
        assertThat(isCUP(ImmutableTumorDetails.builder().build())).isFalse

        assertThat(isCUP(ImmutableTumorDetails.builder().primaryTumorLocation(TumorDetailsInterpreter.CUP_LOCATION).build())).isFalse

        assertThat(
            isCUP(
                ImmutableTumorDetails.builder()
                    .primaryTumorLocation(TumorDetailsInterpreter.CUP_LOCATION)
                    .primaryTumorSubLocation(TumorDetailsInterpreter.CUP_SUB_LOCATION)
                    .build()
            )
        ).isTrue
    }
}