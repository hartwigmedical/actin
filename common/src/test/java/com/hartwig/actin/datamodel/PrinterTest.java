package com.hartwig.actin.datamodel;

import com.hartwig.actin.datamodel.clinical.ClinicalRecord;
import com.hartwig.actin.datamodel.clinical.TestClinicalDataFactory;
import com.hartwig.actin.datamodel.molecular.MolecularRecord;
import com.hartwig.actin.datamodel.molecular.TestMolecularDataFactory;

import org.junit.Test;

public class PrinterTest {

    @Test
    public void canPrintDatamodel() {
        ClinicalRecord clinical = TestClinicalDataFactory.createProperTestClinicalRecord();
        Printer.printClinicalRecord(clinical);
        Printer.printClinicalRecord(clinical, 2);

        MolecularRecord molecular = TestMolecularDataFactory.createProperTestMolecularRecord();
        Printer.printMolecularRecord(molecular);
        Printer.printMolecularRecord(molecular, 2);
    }
}