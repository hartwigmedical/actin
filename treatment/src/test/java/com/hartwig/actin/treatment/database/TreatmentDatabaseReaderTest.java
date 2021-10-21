package com.hartwig.actin.treatment.database;

import static org.junit.Assert.assertNotNull;

import java.io.IOException;

import com.google.common.io.Resources;

import org.junit.Test;

public class TreatmentDatabaseReaderTest {

    private static final String TREATMENT_DIRECTORY = Resources.getResource("treatment").getPath();

    @Test
    public void canReadFromTestDirectory() throws IOException {
        TreatmentDatabase database = TreatmentDatabaseReader.read(TREATMENT_DIRECTORY);

        assertNotNull(database);
    }
}