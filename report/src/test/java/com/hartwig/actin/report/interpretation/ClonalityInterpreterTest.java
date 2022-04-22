package com.hartwig.actin.report.interpretation;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.hartwig.actin.molecular.datamodel.driver.DriverLikelihood;
import com.hartwig.actin.molecular.datamodel.driver.ImmutableVariant;
import com.hartwig.actin.molecular.datamodel.driver.Variant;
import com.hartwig.actin.molecular.datamodel.driver.VariantDriverType;

import org.apache.logging.log4j.util.Strings;
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
        return ImmutableVariant.builder()
                .event(Strings.EMPTY)
                .driverLikelihood(DriverLikelihood.HIGH)
                .gene(Strings.EMPTY)
                .impact(Strings.EMPTY)
                .variantCopyNumber(0D)
                .totalCopyNumber(0D)
                .driverType(VariantDriverType.VUS)
                .clonalLikelihood(clonalLikelihood)
                .build();
    }
}