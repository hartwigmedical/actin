package com.hartwig.actin.molecular.orange.datamodel.protect;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class EvidenceLevelTest {

    @Test
    public void canCompareEvidenceLevels() {
        assertTrue(EvidenceLevel.A.isBetterOrEqual(EvidenceLevel.A));
        assertTrue(EvidenceLevel.A.isBetterOrEqual(EvidenceLevel.B));
        assertTrue(EvidenceLevel.D.isBetterOrEqual(EvidenceLevel.D));

        assertFalse(EvidenceLevel.B.isBetterOrEqual(EvidenceLevel.A));
        assertFalse(EvidenceLevel.D.isBetterOrEqual(EvidenceLevel.B));
    }
}