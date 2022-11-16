package com.hartwig.actin.molecular.orange.interpretation;

import static org.junit.Assert.assertEquals;

import com.hartwig.actin.molecular.datamodel.driver.DriverLikelihood;
import com.hartwig.actin.molecular.orange.datamodel.virus.TestVirusInterpreterFactory;
import com.hartwig.actin.molecular.orange.datamodel.virus.VirusDriverLikelihood;
import com.hartwig.actin.molecular.orange.datamodel.virus.VirusInterpreterEntry;

import org.junit.Test;

public class VirusExtractionTest {

    @Test
    public void canDetermineDriverLikelihoodForAllViruses() {
        VirusInterpreterEntry high = TestVirusInterpreterFactory.builder().driverLikelihood(VirusDriverLikelihood.HIGH).build();
        assertEquals(DriverLikelihood.HIGH, VirusExtraction.determineDriverLikelihood(high));

        VirusInterpreterEntry low = TestVirusInterpreterFactory.builder().driverLikelihood(VirusDriverLikelihood.LOW).build();
        assertEquals(DriverLikelihood.LOW, VirusExtraction.determineDriverLikelihood(low));

        VirusInterpreterEntry unknown = TestVirusInterpreterFactory.builder().driverLikelihood(VirusDriverLikelihood.UNKNOWN).build();
        assertEquals(DriverLikelihood.LOW, VirusExtraction.determineDriverLikelihood(unknown));
    }
}