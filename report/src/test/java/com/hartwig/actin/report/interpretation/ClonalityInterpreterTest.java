package com.hartwig.actin.report.interpretation;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.hartwig.actin.molecular.datamodel.driver.TestVariantFactory;
import com.hartwig.actin.molecular.datamodel.driver.Variant;

import org.jetbrains.annotations.NotNull;
import org.junit.Test;

public class ClonalityInterpreterTest {

    @Test
    public void canDetermineClonality() {
        assertFalse(ClonalityInterpreter.isPotentiallySubclonal(create(ClonalityInterpreter.CLONAL_CUTOFF + 0.01)));
        assertTrue(ClonalityInterpreter.isPotentiallySubclonal(create(ClonalityInterpreter.CLONAL_CUTOFF - 0.01)));
    }

    @NotNull
    private static Variant create(double clonalLikelihood) {
        return TestVariantFactory.builder().clonalLikelihood(clonalLikelihood).build();
    }
}