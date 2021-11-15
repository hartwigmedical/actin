package com.hartwig.actin.algo.util;

import com.hartwig.actin.algo.datamodel.TestTreatmentMatchFactory;

import org.junit.Test;

public class TreatmentMatchPrinterTest {

    @Test
    public void canPrintTreatmentMatches() {
        TreatmentMatchPrinter.printMatch(TestTreatmentMatchFactory.createMinimalTreatmentMatch());
        TreatmentMatchPrinter.printMatch(TestTreatmentMatchFactory.createProperTreatmentMatch());
    }
}