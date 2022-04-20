package com.hartwig.actin.molecular.orange.curation;

import static org.junit.Assert.assertEquals;

import java.util.List;

import com.google.common.collect.Lists;

import org.junit.Test;

public class ExternalTreatmentMapperTest {

    @Test
    public void canMapExternalTreatments() {
        List<ExternalTreatmentMapping> mappings =
                Lists.newArrayList(ImmutableExternalTreatmentMapping.builder().actinTreatment("ACT 1").externalTreatment("EXT 1").build());
        ExternalTreatmentMapper mapper = new ExternalTreatmentMapper(mappings);

        assertEquals("ACT 1", mapper.map("EXT 1"));
        assertEquals("ACT 1", mapper.map("ACT 1"));
        assertEquals("random", mapper.map("random"));
    }
}