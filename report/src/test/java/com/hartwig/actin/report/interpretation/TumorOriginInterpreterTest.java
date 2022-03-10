package com.hartwig.actin.report.interpretation;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.hartwig.actin.molecular.datamodel.ImmutablePredictedTumorOrigin;
import com.hartwig.actin.molecular.datamodel.PredictedTumorOrigin;

import org.apache.logging.log4j.util.Strings;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;

public class TumorOriginInterpreterTest {

    @Test
    public void canDetermineConfidenceOfPredictedTumorOrigin() {
        assertFalse(TumorOriginInterpreter.hasConfidentPrediction(null));
        assertFalse(TumorOriginInterpreter.hasConfidentPrediction(withLikelihood(0.4)));
        assertTrue(TumorOriginInterpreter.hasConfidentPrediction(withLikelihood(0.8)));
        assertTrue(TumorOriginInterpreter.hasConfidentPrediction(withLikelihood(0.99)));
    }

    @NotNull
    private static PredictedTumorOrigin withLikelihood(double likelihood) {
        return ImmutablePredictedTumorOrigin.builder().tumorType(Strings.EMPTY).likelihood(likelihood).build();
    }
}