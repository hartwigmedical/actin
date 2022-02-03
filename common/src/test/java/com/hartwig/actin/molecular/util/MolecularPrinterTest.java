package com.hartwig.actin.molecular.util;

import com.hartwig.actin.molecular.datamodel.TestMolecularDataFactory;

import org.junit.Test;

public class MolecularPrinterTest {

    @Test
    public void canPrintMolecularRecords() {
        MolecularPrinter.printRecord(TestMolecularDataFactory.createExhaustiveTestMolecularRecord());
        MolecularPrinter.printRecord(TestMolecularDataFactory.createProperTestMolecularRecord());
        MolecularPrinter.printRecord(TestMolecularDataFactory.createMinimalTestMolecularRecord());
    }
}