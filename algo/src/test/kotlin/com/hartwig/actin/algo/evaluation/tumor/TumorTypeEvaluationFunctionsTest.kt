package com.hartwig.actin.algo.evaluation.tumor

import org.junit.Assert
import org.junit.Test

class TumorTypeEvaluationFunctionsTest {
    @Test
    fun canDetermineIfTumorHasType() {
        val validTypes = setOf("Valid")
        Assert.assertFalse(TumorTypeEvaluationFunctions.hasTumorWithType(TumorTestFactory.builder().build(), validTypes))
        Assert.assertFalse(
            TumorTypeEvaluationFunctions.hasTumorWithType(
                TumorTestFactory.builder().primaryTumorType("wrong").build(),
                validTypes
            )
        )
        Assert.assertTrue(
            TumorTypeEvaluationFunctions.hasTumorWithType(
                TumorTestFactory.builder().primaryTumorType("valid type").build(),
                validTypes
            )
        )
        Assert.assertTrue(
            TumorTypeEvaluationFunctions.hasTumorWithType(
                TumorTestFactory.builder().primaryTumorSubType("valid sub-type").build(),
                validTypes
            )
        )
    }

    @Test
    fun canDetermineIfTumorHasDetails() {
        val validDetails = setOf("Valid")
        Assert.assertFalse(TumorTypeEvaluationFunctions.hasTumorWithDetails(TumorTestFactory.builder().build(), validDetails))
        Assert.assertFalse(
            TumorTypeEvaluationFunctions.hasTumorWithDetails(
                TumorTestFactory.builder().primaryTumorExtraDetails("wrong").build(),
                validDetails
            )
        )
        Assert.assertTrue(
            TumorTypeEvaluationFunctions.hasTumorWithDetails(
                TumorTestFactory.builder()
                    .primaryTumorExtraDetails("valid details")
                    .build(), validDetails
            )
        )
    }
}