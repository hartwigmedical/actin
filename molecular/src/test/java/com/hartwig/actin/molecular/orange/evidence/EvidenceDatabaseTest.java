package com.hartwig.actin.molecular.orange.evidence;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;

public class EvidenceDatabaseTest {

    @Test
    public void canMatchEvidence() {
        // TODO Implement
        EvidenceDatabase database = TestEvidenceDatabaseFactory.createProperDatabase();
        assertNotNull(database);
    }
}