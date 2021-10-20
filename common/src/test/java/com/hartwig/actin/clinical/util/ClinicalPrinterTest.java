package com.hartwig.actin.clinical.util;

import com.hartwig.actin.clinical.datamodel.TestClinicalDataFactory;

import org.junit.Test;

public class ClinicalPrinterTest {

    @Test
    public void canPrintClinicalRecords() {
        ClinicalPrinter.printRecord(TestClinicalDataFactory.createProperTestClinicalRecord());
        ClinicalPrinter.printRecord(TestClinicalDataFactory.createMinimalTestClinicalRecord());
    }
}