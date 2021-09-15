package com.hartwig.actin.clinical.feed.lab;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import com.google.common.io.Resources;

import org.junit.Test;

public class LabFileTest {

    private static final String TEST_LAB_TSV = Resources.getResource("clinical/lab.tsv").getPath();

    @Test
    public void canReadTestFile() throws IOException {
        assertEquals(2, LabFile.read(TEST_LAB_TSV).size());
    }
}