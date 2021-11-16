package com.hartwig.actin.algo.util;

import com.hartwig.actin.algo.datamodel.TreatmentMatch;
import com.hartwig.actin.algo.interpretation.TreatmentMatchSummarizer;
import com.hartwig.actin.algo.interpretation.TreatmentMatchSummary;
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

        TreatmentMatchSummary summary = TreatmentMatchSummarizer.summarize(treatmentMatch);
        printer.print("Trial count: " + summary.trialCount());
        printer.print("Eligible trial count: " + summary.eligibleTrialCount());
        printer.print("Cohort count: " + summary.cohortCount());
        printer.print("Eligible cohort count: " + summary.eligibleCohortCount());
        printer.print("Eligible and open cohort count: " + summary.eligibleOpenCohortCount());
    }
}
