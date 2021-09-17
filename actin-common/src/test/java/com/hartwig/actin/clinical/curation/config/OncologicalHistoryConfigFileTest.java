package com.hartwig.actin.clinical.curation.config;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.List;

import com.google.common.io.Resources;

import org.junit.Test;

public class OncologicalHistoryConfigFileTest {

    private static final String ONCOLOGICAL_HISTORY_TSV = Resources.getResource("clinical/curation/oncological_history.tsv").getPath();

    @Test
    public void canReadFromTestFile() throws IOException {
        List<OncologicalHistoryConfig> oncologicalHistories = OncologicalHistoryConfigFile.read(ONCOLOGICAL_HISTORY_TSV);

        assertEquals(1, oncologicalHistories.size());
    }
}