package com.hartwig.actin.molecular.orange.interpretation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import com.hartwig.actin.molecular.datamodel.driver.DriverLikelihood;
import com.hartwig.actin.molecular.orange.datamodel.linx.LinxFusion;
import com.hartwig.actin.molecular.orange.datamodel.linx.LinxFusionDriverLikelihood;
import com.hartwig.actin.molecular.orange.datamodel.linx.LinxFusionType;
import com.hartwig.actin.molecular.orange.datamodel.linx.TestLinxFactory;

import org.junit.Test;

public class FusionExtractorTest {

    @Test
    public void canDetermineDriverTypeForAllFusions() {
        for (LinxFusionType type : LinxFusionType.values()) {
            LinxFusion fusion = TestLinxFactory.fusionBuilder().type(type).build();
            assertNotNull(FusionExtractor.determineDriverType(fusion));
        }
    }

    @Test
    public void canDetermineDriverLikelihoodForAllFusions() {
        LinxFusion high = TestLinxFactory.fusionBuilder().driverLikelihood(LinxFusionDriverLikelihood.HIGH).build();
        assertEquals(DriverLikelihood.HIGH, FusionExtractor.determineDriverLikelihood(high));

        LinxFusion low = TestLinxFactory.fusionBuilder().driverLikelihood(LinxFusionDriverLikelihood.LOW).build();
        assertEquals(DriverLikelihood.LOW, FusionExtractor.determineDriverLikelihood(low));

        LinxFusion na = TestLinxFactory.fusionBuilder().driverLikelihood(LinxFusionDriverLikelihood.NA).build();
        assertNull(FusionExtractor.determineDriverLikelihood(na));
    }
}