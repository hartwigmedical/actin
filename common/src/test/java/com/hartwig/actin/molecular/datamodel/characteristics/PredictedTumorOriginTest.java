package com.hartwig.actin.molecular.datamodel.characteristics;

import com.hartwig.hmftools.datamodel.cuppa.ImmutableCuppaPrediction;

import static org.junit.Assert.assertEquals;

import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.Test;

public class PredictedTumorOriginTest {

    private static final double EPSILON = 0.001;

    @Test
    public void shouldIdentifyBestPredictionInUnsortedList() {
        PredictedTumorOrigin predictedTumorOrigin = withPredictions(0.1, 0.08, 0.4, 0.2);
        assertEquals("type 3", predictedTumorOrigin.cancerType());
        assertEquals(0.4, predictedTumorOrigin.likelihood(), EPSILON);
    }

    private static PredictedTumorOrigin withPredictions(double... likelihoods) {
        return ImmutablePredictedTumorOrigin.builder()
                .predictions(IntStream.range(0, likelihoods.length)
                        .mapToObj(i -> ImmutableCuppaPrediction.builder()
                                .cancerType(String.format("type %s", i + 1))
                                .likelihood(likelihoods[i])
                                .snvPairwiseClassifier(likelihoods[i])
                                .genomicPositionClassifier(likelihoods[i])
                                .featureClassifier(likelihoods[i])
                                .build())
                        .collect(Collectors.toList()))
                .build();
    }
}