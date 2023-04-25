package com.hartwig.actin.algo.evaluation.general;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;

public class PerformanceScoreTest  {

    @Test
    public void canConvertAllDisplays() {
        for (PerformanceScore performanceScore : PerformanceScore.values()) {
            assertNotNull(performanceScore.display());
        }
    }
}