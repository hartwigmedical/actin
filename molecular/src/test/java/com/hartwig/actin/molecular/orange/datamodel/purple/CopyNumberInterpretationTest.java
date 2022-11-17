package com.hartwig.actin.molecular.orange.datamodel.purple;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class CopyNumberInterpretationTest {

    @Test
    public void hasCorrectConfigurationOfInterpretedValues() {
        assertTrue(CopyNumberInterpretation.FULL_GAIN.isGain());
        assertFalse(CopyNumberInterpretation.FULL_GAIN.isLoss());

        assertTrue(CopyNumberInterpretation.PARTIAL_GAIN.isGain());
        assertFalse(CopyNumberInterpretation.PARTIAL_GAIN.isLoss());

        assertFalse(CopyNumberInterpretation.FULL_LOSS.isGain());
        assertTrue(CopyNumberInterpretation.FULL_LOSS.isLoss());

        assertFalse(CopyNumberInterpretation.PARTIAL_LOSS.isGain());
        assertTrue(CopyNumberInterpretation.PARTIAL_LOSS.isLoss());
    }
}