package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.clinical.datamodel.TumorDetails
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class TumorTypeEvaluationFunctionsTest {
    @Test
    fun canDetermineIfTumorHasType() {
        val validTypes = setOf("Valid")
        assertThat(TumorTypeEvaluationFunctions.hasTumorWithType(TumorDetails(), validTypes)).isFalse
        assertThat(TumorTypeEvaluationFunctions.hasTumorWithType(TumorDetails(primaryTumorType = "wrong"), validTypes)).isFalse
        assertThat(TumorTypeEvaluationFunctions.hasTumorWithType(TumorDetails(primaryTumorType = "valid type"), validTypes)).isTrue
        assertThat(TumorTypeEvaluationFunctions.hasTumorWithType(TumorDetails(primaryTumorSubType = "valid sub-type"), validTypes)).isTrue
    }

    @Test
    fun canDetermineIfTumorHasDetails() {
        val validDetails = setOf("Valid")
        assertThat(TumorTypeEvaluationFunctions.hasTumorWithDetails(TumorDetails(), validDetails)).isFalse
        assertThat(TumorTypeEvaluationFunctions.hasTumorWithDetails(TumorDetails(primaryTumorExtraDetails = "wrong"), validDetails)).isFalse
        assertThat(TumorTypeEvaluationFunctions.hasTumorWithDetails(TumorDetails(primaryTumorExtraDetails = "valid details"), validDetails))
            .isTrue
    }
}