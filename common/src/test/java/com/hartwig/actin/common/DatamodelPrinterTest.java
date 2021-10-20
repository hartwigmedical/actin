package com.hartwig.actin.common;

import org.junit.Test;

public class DatamodelPrinterTest {

    @Test
    public void canPrintWithVaryingIndents() {
        for (int i = 0; i < 10; i++) {
            DatamodelPrinter printer = new DatamodelPrinter(i);
            printer.print("hi");
        }
    }
}