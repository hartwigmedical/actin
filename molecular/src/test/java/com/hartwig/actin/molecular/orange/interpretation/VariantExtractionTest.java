package com.hartwig.actin.molecular.orange.interpretation;

import static org.junit.Assert.assertEquals;

import com.hartwig.actin.molecular.datamodel.driver.DriverLikelihood;
import com.hartwig.actin.molecular.orange.datamodel.purple.PurpleTestFactory;
import com.hartwig.actin.molecular.orange.datamodel.purple.PurpleVariant;

import org.junit.Test;

public class VariantExtractionTest {

    @Test
    public void canDetermineDriverLikelihoods() {
        PurpleVariant high = PurpleTestFactory.variantBuilder().driverLikelihood(1D).build();
        assertEquals(DriverLikelihood.HIGH, VariantExtraction.determineDriverLikelihood(high));

        PurpleVariant medium = PurpleTestFactory.variantBuilder().driverLikelihood(0.5).build();
        assertEquals(DriverLikelihood.MEDIUM, VariantExtraction.determineDriverLikelihood(medium));

        PurpleVariant low = PurpleTestFactory.variantBuilder().driverLikelihood(0D).build();
        assertEquals(DriverLikelihood.LOW, VariantExtraction.determineDriverLikelihood(low));
    }
}