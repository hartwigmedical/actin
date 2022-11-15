package com.hartwig.actin.molecular.orange.interpretation;

import static org.junit.Assert.assertEquals;

import com.hartwig.actin.molecular.datamodel.driver.DriverLikelihood;
import com.hartwig.actin.molecular.orange.datamodel.virus.TestVirusFactory;
import com.hartwig.actin.molecular.orange.datamodel.virus.VirusDriverLikelihood;
import com.hartwig.actin.molecular.orange.datamodel.virus.VirusInterpreterEntry;

import org.junit.Test;

public class VirusExtractionTest {

    @Test
    public void canDetermineDriverLikelihoodForAllViruses() {
        VirusInterpreterEntry high = TestVirusFactory.builder().driverLikelihood(VirusDriverLikelihood.HIGH).build();
        assertEquals(DriverLikelihood.HIGH, VirusExtraction.determineDriverLikelihood(high));

        VirusInterpreterEntry low = TestVirusFactory.builder().driverLikelihood(VirusDriverLikelihood.LOW).build();
        assertEquals(DriverLikelihood.LOW, VirusExtraction.determineDriverLikelihood(low));

        VirusInterpreterEntry unknown = TestVirusFactory.builder().driverLikelihood(VirusDriverLikelihood.UNKNOWN).build();
        assertEquals(DriverLikelihood.LOW, VirusExtraction.determineDriverLikelihood(unknown));
    }
}