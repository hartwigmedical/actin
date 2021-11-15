package com.hartwig.actin.algo.util;

import com.hartwig.actin.algo.datamodel.TreatmentMatch;
import com.hartwig.actin.util.DatamodelPrinter;

import org.jetbrains.annotations.NotNull;

public class TreatmentMatchPrinter {

    @NotNull
    private final DatamodelPrinter printer;

    public static void printMatch(@NotNull TreatmentMatch treatmentMatch) {
        new TreatmentMatchPrinter(DatamodelPrinter.withDefaultIndentation()).print(treatmentMatch);
    }

    private TreatmentMatchPrinter(@NotNull final DatamodelPrinter printer) {
        this.printer = printer;
    }

    public void print(@NotNull TreatmentMatch treatmentMatch) {
        printer.print("Sample: " + treatmentMatch.sampleId());
        printer.print("Trial count: " + treatmentMatch.trialMatches().size());
    }
}
