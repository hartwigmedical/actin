package com.hartwig.actin.molecular.orange.curation;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.List;

import com.google.common.io.Resources;

import org.jetbrains.annotations.NotNull;
import org.junit.Test;

public class ExternalTreatmentMappingTsvTest {

    private static final String EXAMPLE_TSV = Resources.getResource("curation/external_treatment_mapping.tsv").getPath();

    @Test
    public void canReadExternalTreatmentMappingTsv() throws IOException {
        List<ExternalTreatmentMapping> mappings = ExternalTreatmentMappingTsv.read(EXAMPLE_TSV);

        assertEquals(2, mappings.size());
        assertEquals("Trial 1", findByExternalTreatment(mappings, "TR1").actinTreatment());
        assertEquals("TR2", findByExternalTreatment(mappings, "TR2").actinTreatment());
    }

    @NotNull
    private static ExternalTreatmentMapping findByExternalTreatment(@NotNull List<ExternalTreatmentMapping> mappings,
            @NotNull String externalTreatmentToFind) {
        for (ExternalTreatmentMapping mapping : mappings) {
            if (mapping.externalTreatment().equals(externalTreatmentToFind)) {
                return mapping;
            }
        }

        throw new IllegalStateException("Could not find external treatment in mapping list: " + externalTreatmentToFind);
    }
}