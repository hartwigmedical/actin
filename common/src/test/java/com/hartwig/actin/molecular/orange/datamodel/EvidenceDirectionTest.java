package com.hartwig.actin.molecular.orange.datamodel;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class EvidenceDirectionTest {

    @Test
    public void everyEvidenceIsEitherResponsiveOrResistant() {
        for (EvidenceDirection direction : EvidenceDirection.values()) {
            assertTrue(direction.isResponsive() || direction.isResistant());
            assertFalse(direction.isResponsive() && direction.isResistant());
        }
    }
}