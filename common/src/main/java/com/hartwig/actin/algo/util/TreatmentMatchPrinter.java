package com.hartwig.actin.algo.util;

import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

import com.google.common.collect.Lists;
import com.hartwig.actin.algo.datamodel.CohortMatch;
import com.hartwig.actin.algo.datamodel.TreatmentMatch;
import com.hartwig.actin.algo.datamodel.TrialMatch;
import com.hartwig.actin.algo.interpretation.EvaluationSummarizer;
import com.hartwig.actin.algo.interpretation.EvaluationSummary;
import com.hartwig.actin.algo.interpretation.TrialMatchSummarizer;
import com.hartwig.actin.algo.interpretation.TrialMatchSummary;
import com.hartwig.actin.treatment.datamodel.CohortMetadata;
import com.hartwig.actin.treatment.datamodel.TrialIdentification;
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
        printer.print("Patient: " + treatmentMatch.patientId());

        TrialMatchSummary matchSummary = TrialMatchSummarizer.summarize(treatmentMatch.trialMatches());
        printer.print("Trials: " + matchSummary.trialCount());
        printer.print("Eligible trials: " + trialString(matchSummary.eligibleTrialMap()));
        printer.print("Cohorts: " + matchSummary.cohortCount());
        printer.print("Eligible cohorts: " + cohortString(matchSummary.eligibleTrialMap()));
        printer.print("Eligible and recruiting cohorts: " + recruitingCohortString(matchSummary.eligibleTrialMap()));

        List<EvaluationSummary> summaries = Lists.newArrayList();
        for (TrialMatch trialMatch : treatmentMatch.trialMatches()) {
            summaries.add(EvaluationSummarizer.summarize(trialMatch.evaluations().values()));
            for (CohortMatch cohortMatch : trialMatch.cohorts()) {
                summaries.add(EvaluationSummarizer.summarize(cohortMatch.evaluations().values()));
            }
        }

        EvaluationSummary evaluationSummary = EvaluationSummarizer.sum(summaries);
        printer.print("# Rules evaluated: " + evaluationSummary.count());
        printer.print("# Rules with PASS evaluation: " + evaluationSummary.passedCount());
        printer.print("# Rules with WARN evaluation: " + evaluationSummary.warningCount());
        printer.print("# Rules with FAIL evaluation: " + evaluationSummary.failedCount());
        printer.print("# Rules with UNDETERMINED evaluation: " + evaluationSummary.undeterminedCount());
        printer.print("# Rules which have not been evaluated: " + evaluationSummary.notEvaluatedCount());
        printer.print("# Rules which have not been implemented: " + evaluationSummary.nonImplementedCount());
    }

    @NotNull
    private static String trialString(@NotNull Map<TrialIdentification, List<CohortMetadata>> eligibleTrialMap) {
        if (eligibleTrialMap.isEmpty()) {
            return "None";
        }

        StringJoiner joiner = new StringJoiner(", ");
        for (TrialIdentification trial : eligibleTrialMap.keySet()) {
            joiner.add(trialName(trial));
        }
        return eligibleTrialMap.keySet().size() + " (" + joiner + ")";
    }

    @NotNull
    private static String trialName(@NotNull TrialIdentification trial) {
        return trial.trialId() + " (" + trial.acronym() + ")";
    }

    @NotNull
    private String cohortString(@NotNull Map<TrialIdentification, List<CohortMetadata>> eligibleTrialMap) {
        int cohortCount = 0;
        StringJoiner joiner = new StringJoiner(", ");
        for (Map.Entry<TrialIdentification, List<CohortMetadata>> entry : eligibleTrialMap.entrySet()) {
            for (CohortMetadata cohort : entry.getValue()) {
                cohortCount++;
                joiner.add(cohortName(entry.getKey(), cohort));
            }
        }

        return cohortCount > 0 ? cohortCount + " (" + joiner + ")" : "None";
    }

    @NotNull
    private static String recruitingCohortString(@NotNull Map<TrialIdentification, List<CohortMetadata>> eligibleTrialMap) {
        int recruitingCohortCount = 0;
        StringJoiner joiner = new StringJoiner(", ");
        for (Map.Entry<TrialIdentification, List<CohortMetadata>> entry : eligibleTrialMap.entrySet()) {
            for (CohortMetadata cohort : entry.getValue()) {
                if (cohort.open() && cohort.slotsAvailable()) {
                    recruitingCohortCount++;
                    joiner.add(cohortName(entry.getKey(), cohort));
                }
            }
        }

        return recruitingCohortCount > 0 ? recruitingCohortCount + " (" + joiner + ")" : "None";
    }

    @NotNull
    private static String cohortName(@NotNull TrialIdentification trial, @NotNull CohortMetadata cohort) {
        return trial.trialId() + " - " + cohort.description();
    }
}
