package com.hartwig.actin.molecular.orange.evidence.curation;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.List;

import com.google.common.io.Resources;

import org.jetbrains.annotations.NotNull;
import org.junit.Test;

public class ExternalTrialMappingFileTest {

    private static final String EXAMPLE_TSV = Resources.getResource("curation/external_trial_mapping.tsv").getPath();

    @Test
    public void canReadExternalTrialMappingTsv() throws IOException {
        List<ExternalTrialMapping> mappings = ExternalTrialMappingFile.read(EXAMPLE_TSV);

        assertEquals(2, mappings.size());
        assertEquals("Trial 1", findByExternalTrial(mappings, "TR1").actinTrial());
        assertEquals("TR2", findByExternalTrial(mappings, "TR2").actinTrial());
    }

    @NotNull
    private static ExternalTrialMapping findByExternalTrial(@NotNull List<ExternalTrialMapping> mappings,
            @NotNull String externalTrialToFind) {
        for (ExternalTrialMapping mapping : mappings) {
            if (mapping.externalTrial().equals(externalTrialToFind)) {
                return mapping;
            }
        }

        throw new IllegalStateException("Could not find external trial in mapping list: " + externalTrialToFind);
    }
}