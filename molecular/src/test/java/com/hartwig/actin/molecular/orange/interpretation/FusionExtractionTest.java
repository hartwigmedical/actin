package com.hartwig.actin.molecular.orange.interpretation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import com.hartwig.actin.molecular.datamodel.driver.DriverLikelihood;
import com.hartwig.actin.molecular.orange.datamodel.linx.FusionDriverLikelihood;
import com.hartwig.actin.molecular.orange.datamodel.linx.FusionType;
import com.hartwig.actin.molecular.orange.datamodel.linx.LinxFusion;
import com.hartwig.actin.molecular.orange.datamodel.linx.TestLinxFactory;

import org.junit.Test;

public class FusionExtractionTest {

    @Test
    public void canDetermineDriverTypeForAllFusions() {
        for (FusionType type : FusionType.values()) {
            if (type != FusionType.NONE) {
                LinxFusion fusion = TestLinxFactory.fusionBuilder().type(type).build();
                assertNotNull(FusionExtraction.determineDriverType(fusion));
            }
        }
    }

    @Test
    public void canDetermineDriverLikelihoodForAllFusions() {
        LinxFusion high = TestLinxFactory.fusionBuilder().driverLikelihood(FusionDriverLikelihood.HIGH).build();
        assertEquals(DriverLikelihood.HIGH, FusionExtraction.determineDriverLikelihood(high));

        LinxFusion low = TestLinxFactory.fusionBuilder().driverLikelihood(FusionDriverLikelihood.LOW).build();
        assertEquals(DriverLikelihood.LOW, FusionExtraction.determineDriverLikelihood(low));
    }
}