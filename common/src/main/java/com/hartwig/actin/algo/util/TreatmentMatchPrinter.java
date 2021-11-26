package com.hartwig.actin.algo.util;

import java.util.List;

import com.google.common.collect.Lists;
import com.hartwig.actin.algo.datamodel.CohortEligibility;
import com.hartwig.actin.algo.datamodel.TreatmentMatch;
import com.hartwig.actin.algo.datamodel.TrialEligibility;
import com.hartwig.actin.algo.interpretation.EvaluationSummarizer;
import com.hartwig.actin.algo.interpretation.EvaluationSummary;
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

        TreatmentMatchSummary matchSummary = TreatmentMatchSummarizer.summarize(treatmentMatch);
        printer.print("Trial count: " + matchSummary.trialCount());
        printer.print("Eligible trial count: " + matchSummary.eligibleTrialCount());
        printer.print("Cohort count: " + matchSummary.cohortCount());
        printer.print("Eligible cohort count: " + matchSummary.eligibleCohortCount());
        printer.print("Eligible and open cohort count: " + matchSummary.eligibleOpenCohortCount());

        List<EvaluationSummary> summaries = Lists.newArrayList();
        for (TrialEligibility trialMatch : treatmentMatch.trialMatches()) {
            summaries.add(EvaluationSummarizer.summarize(trialMatch.evaluations().values()));
            for (CohortEligibility cohortMatch : trialMatch.cohorts()) {
                summaries.add(EvaluationSummarizer.summarize(cohortMatch.evaluations().values()));
            }
        }

        EvaluationSummary evaluationSummary = EvaluationSummarizer.sum(summaries);
        printer.print("# Rules evaluated: " + evaluationSummary.count());
        printer.print("# Rules that PASS evaluation: " + evaluationSummary.passedCount());
        printer.print("# Rules that PASS evaluation with WARN: " + evaluationSummary.warningCount());
        printer.print("# Rules that FAIL evaluation: " + evaluationSummary.failedCount());
        printer.print("# Rules for which evaluation could not be determined: " + evaluationSummary.undeterminedCount());
        printer.print("# Rules which have not been evaluated: " + evaluationSummary.notEvaluatedCount());
        printer.print("# Rules which have not been implemented: " + evaluationSummary.nonImplementedCount());
    }
}
