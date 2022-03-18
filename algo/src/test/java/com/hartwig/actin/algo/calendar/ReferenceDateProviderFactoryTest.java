package com.hartwig.actin.algo.calendar;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.hartwig.actin.clinical.datamodel.ClinicalRecord;
import com.hartwig.actin.clinical.datamodel.TestClinicalDataFactory;

import org.junit.Test;

public class ReferenceDateProviderFactoryTest {

    @Test
    public void canCreateAllFlavors() {
        ClinicalRecord clinical = TestClinicalDataFactory.createMinimalTestClinicalRecord();

        ReferenceDateProvider provider1 = ReferenceDateProviderFactory.create(clinical, true);
        assertNotNull(provider1.date());
        assertFalse(provider1.isLive());

        ReferenceDateProvider provider2 = ReferenceDateProviderFactory.create(clinical, false);
        assertNotNull(provider2.date());
        assertTrue(provider2.isLive());
    }
}