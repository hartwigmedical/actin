package com.hartwig.actin.clinical.util;

import com.hartwig.actin.clinical.datamodel.TestClinicalFactory;

import org.junit.Test;

public class ClinicalPrinterTest {

    @Test
    public void canPrintClinicalRecords() {
        ClinicalPrinter.printRecord(TestClinicalFactory.createProperTestClinicalRecord());
        ClinicalPrinter.printRecord(TestClinicalFactory.createMinimalTestClinicalRecord());
    }
}