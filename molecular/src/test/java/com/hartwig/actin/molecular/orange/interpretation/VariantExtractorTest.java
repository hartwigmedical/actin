package com.hartwig.actin.molecular.orange.interpretation;

import static org.junit.Assert.assertEquals;

import com.hartwig.actin.molecular.datamodel.driver.DriverLikelihood;
import com.hartwig.actin.molecular.orange.datamodel.purple.PurpleVariant;
import com.hartwig.actin.molecular.orange.datamodel.purple.TestPurpleFactory;

import org.junit.Test;

public class VariantExtractorTest {

    @Test
    public void canDetermineDriverLikelihoods() {
        PurpleVariant high = TestPurpleFactory.variantBuilder().driverLikelihood(1D).build();
        assertEquals(DriverLikelihood.HIGH, VariantExtractor.determineDriverLikelihood(high));

        PurpleVariant medium = TestPurpleFactory.variantBuilder().driverLikelihood(0.5).build();
        assertEquals(DriverLikelihood.MEDIUM, VariantExtractor.determineDriverLikelihood(medium));

        PurpleVariant low = TestPurpleFactory.variantBuilder().driverLikelihood(0D).build();
        assertEquals(DriverLikelihood.LOW, VariantExtractor.determineDriverLikelihood(low));
    }
}