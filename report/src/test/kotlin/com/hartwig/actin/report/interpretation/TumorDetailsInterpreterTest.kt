package com.hartwig.actin.report.interpretation

import com.hartwig.actin.datamodel.clinical.TumorDetails
import com.hartwig.actin.report.interpretation.TumorDetailsInterpreter.isCUP
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class TumorDetailsInterpreterTest {

    @Test
    fun shouldCorrectlyDetermineIfTumorIsCUP() {
        assertThat(isCUP(TumorDetails())).isFalse

        assertThat(isCUP(TumorDetails(primaryTumorLocation = TumorDetailsInterpreter.CUP_LOCATION))).isFalse

        assertThat(
            isCUP(
                TumorDetails(
                    primaryTumorLocation = TumorDetailsInterpreter.CUP_LOCATION,
                    primaryTumorSubLocation = TumorDetailsInterpreter.CUP_SUB_LOCATION
                )
            )
        ).isTrue
    }
}