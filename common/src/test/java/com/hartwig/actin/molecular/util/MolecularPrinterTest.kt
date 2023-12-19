package com.hartwig.actin.molecular.util;

import com.hartwig.actin.molecular.datamodel.TestMolecularFactory;

import org.junit.Test;

public class MolecularPrinterTest {

    @Test
    public void canPrintMolecularRecords() {
        MolecularPrinter.printRecord(TestMolecularFactory.createExhaustiveTestMolecularRecord());
        MolecularPrinter.printRecord(TestMolecularFactory.createProperTestMolecularRecord());
        MolecularPrinter.printRecord(TestMolecularFactory.createMinimalTestMolecularRecord());
    }
}