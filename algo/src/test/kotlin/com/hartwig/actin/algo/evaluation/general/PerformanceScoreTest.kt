package com.hartwig.actin.algo.evaluation.general

import org.junit.Assert
import org.junit.Test

class PerformanceScoreTest {
    @Test
    fun canConvertAllDisplays() {
        for (performanceScore in PerformanceScore.values()) {
            Assert.assertNotNull(performanceScore.display())
        }
    }
}