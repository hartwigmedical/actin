package com.hartwig.actin.molecular.orange.evidence.curation;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class ExternalTrialMapperTest {

    @Test
    public void canMapExternalTrials() {
        ExternalTrialMapper mapper = TestExternalTreatmentMapperFactory.create("EXT 1", "ACT 1");

        assertEquals("ACT 1", mapper.map("EXT 1"));
        assertEquals("ACT 1", mapper.map("ACT 1"));
        assertEquals("random", mapper.map("random"));
    }
}