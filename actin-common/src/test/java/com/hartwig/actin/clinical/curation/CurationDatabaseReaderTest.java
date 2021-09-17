package com.hartwig.actin.clinical.curation;

import static org.junit.Assert.assertNotNull;

import java.io.IOException;

import com.google.common.io.Resources;

import org.junit.Test;

public class CurationDatabaseReaderTest {

    private static final String CURATION_DIRECTORY = Resources.getResource("clinical/curation").getPath();

    @Test
    public void canReadFromTestDirectory() throws IOException {
        CurationDatabase database = CurationDatabaseReader.read(CURATION_DIRECTORY);

        assertNotNull(database);
    }
}