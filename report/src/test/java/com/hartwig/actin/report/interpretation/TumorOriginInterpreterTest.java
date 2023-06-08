package com.hartwig.actin.report.interpretation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.hartwig.actin.molecular.datamodel.characteristics.CuppaPrediction;
import com.hartwig.actin.molecular.datamodel.characteristics.ImmutableCuppaPrediction;
import com.hartwig.actin.molecular.datamodel.characteristics.ImmutablePredictedTumorOrigin;
import com.hartwig.actin.molecular.datamodel.characteristics.PredictedTumorOrigin;
import com.hartwig.actin.report.pdf.util.Formats;

import org.jetbrains.annotations.NotNull;
import org.junit.Test;

public class TumorOriginInterpreterTest {

    public static final double EPSILON = 0.0001;

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

        PredictedTumorOrigin origin = ImmutablePredictedTumorOrigin.builder().tumorType("something").likelihood(0.9).build();
        assertEquals("something (90%)", TumorOriginInterpreter.interpret(origin));
    }

    @Test
    public void shouldReturnEmptyListForDisplayWhenPredictedTumorOriginIsNull() {
        assertEquals(Collections.emptyList(), TumorOriginInterpreter.predictionsToDisplay(null));
    }

    @Test
    public void shouldReturnEmptyListForDisplayWhenAllPredictionsAreBelowThreshold() {
        assertEquals(Collections.emptyList(), TumorOriginInterpreter.predictionsToDisplay(withPredictions(0.09, 0.02, 0.05, 0.08)));
    }

    @Test
    public void shouldOmitPredictionsBelowThresholdForDisplay() {
        List<CuppaPrediction> predictions = TumorOriginInterpreter.predictionsToDisplay(withPredictions(0.4, 0.02, 0.05, 0.08));
        assertEquals(1, predictions.size());
        assertEquals(0.4, predictions.iterator().next().likelihood(), EPSILON);
    }

    @Test
    public void shouldDisplayAtMostThreePredictions() {
        List<CuppaPrediction> predictions = TumorOriginInterpreter.predictionsToDisplay(withPredictions(0.4, 0.12, 0.15, 0.25));
        assertEquals(3, predictions.size());
        assertEquals(Set.of(0.4, 0.25, 0.15), predictions.stream().map(CuppaPrediction::likelihood).collect(Collectors.toSet()));
    }

    @Test
    public void shouldReturnGreatestLikelihoodLimitedByThreshold() {
        assertEquals(0.08, TumorOriginInterpreter.greatestOmittedLikelihood(withPredictions(0.4, 0.02, 0.05, 0.08)), EPSILON);
    }

    @Test
    public void shouldReturnGreatestLikelihoodLimitedByCount() {
        assertEquals(0.12, TumorOriginInterpreter.greatestOmittedLikelihood(withPredictions(0.4, 0.12, 0.15, 0.25)), EPSILON);
    }

    @NotNull
    private static PredictedTumorOrigin withPredictions(double... likelihoods) {
        return ImmutablePredictedTumorOrigin.copyOf(withLikelihood(likelihoods[0]))
                .withPredictions(IntStream.range(0, likelihoods.length)
                        .mapToObj(i -> ImmutableCuppaPrediction.builder()
                                .cancerType(String.format("type %s", i + 1))
                                .likelihood(likelihoods[i])
                                .snvPairwiseClassifier(likelihoods[i])
                                .genomicPositionClassifier(likelihoods[i])
                                .featureClassifier(likelihoods[i])
                                .build())
                        .collect(Collectors.toList()));
    }

    @NotNull
    private static PredictedTumorOrigin withLikelihood(double likelihood) {
        return ImmutablePredictedTumorOrigin.builder().tumorType("type 1").likelihood(likelihood).build();
    }
}