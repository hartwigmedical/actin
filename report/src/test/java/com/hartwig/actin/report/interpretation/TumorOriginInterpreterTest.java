package com.hartwig.actin.report.interpretation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.hartwig.actin.molecular.datamodel.characteristics.ImmutablePredictedTumorOrigin;
import com.hartwig.actin.molecular.datamodel.characteristics.PredictedTumorOrigin;
import com.hartwig.actin.report.pdf.util.Formats;

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

    @Test
    public void canInterpretPredictedTumorOrigins() {
        assertEquals(Formats.VALUE_UNKNOWN, TumorOriginInterpreter.interpret(null));

        PredictedTumorOrigin lowConfidence = ImmutablePredictedTumorOrigin.builder().tumorType("something").likelihood(0.4).build();
        assertEquals(TumorOriginInterpreter.INCONCLUSIVE_STRING, TumorOriginInterpreter.interpret(lowConfidence));

        PredictedTumorOrigin highConfidence = ImmutablePredictedTumorOrigin.builder().tumorType("something").likelihood(0.9).build();
        assertEquals("something (90%)", TumorOriginInterpreter.interpret(highConfidence));
    }

    @NotNull
    private static PredictedTumorOrigin withLikelihood(double likelihood) {
        return ImmutablePredictedTumorOrigin.builder().tumorType(Strings.EMPTY).likelihood(likelihood).build();
    }
}