package com.hartwig.actin.algo.evaluation.tumor;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Set;

import com.google.common.collect.Sets;

import org.junit.Test;

public class TumorTypeEvaluationFunctionsTest {

    @Test
    public void canDetermineIfTumorHasType() {
        Set<String> validTypes = Sets.newHashSet("Valid");

        assertFalse(TumorTypeEvaluationFunctions.hasTumorWithType(TumorTestFactory.builder().build(), validTypes));
        assertFalse(TumorTypeEvaluationFunctions.hasTumorWithType(TumorTestFactory.builder().primaryTumorType("wrong").build(),
                validTypes));

        assertTrue(TumorTypeEvaluationFunctions.hasTumorWithType(TumorTestFactory.builder().primaryTumorType("valid type").build(),
                validTypes));
        assertTrue(TumorTypeEvaluationFunctions.hasTumorWithType(TumorTestFactory.builder().primaryTumorSubType("valid sub-type").build(),
                validTypes));
    }

    @Test
    public void canDetermineIfTumorHasDetails() {
        Set<String> validDetails = Sets.newHashSet("Valid");

        assertFalse(TumorTypeEvaluationFunctions.hasTumorWithDetails(TumorTestFactory.builder().build(), validDetails));
        assertFalse(TumorTypeEvaluationFunctions.hasTumorWithDetails(TumorTestFactory.builder().primaryTumorExtraDetails("wrong").build(),
                validDetails));

        assertTrue(TumorTypeEvaluationFunctions.hasTumorWithDetails(TumorTestFactory.builder()
                .primaryTumorExtraDetails("valid details")
                .build(), validDetails));
    }
}