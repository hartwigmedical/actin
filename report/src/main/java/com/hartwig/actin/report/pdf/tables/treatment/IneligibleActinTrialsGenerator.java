package com.hartwig.actin.report.pdf.tables.treatment;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.hartwig.actin.report.interpretation.EvaluatedTrial;
import com.hartwig.actin.report.pdf.tables.TableGenerator;
import com.hartwig.actin.report.pdf.util.Cells;
import com.hartwig.actin.report.pdf.util.Tables;
import com.hartwig.actin.treatment.TreatmentConstants;
import com.itextpdf.layout.element.Table;

import org.jetbrains.annotations.NotNull;

public class IneligibleActinTrialsGenerator implements TableGenerator {

    @NotNull
    private final List<EvaluatedTrial> trials;
    @NotNull
    private final String source;
    private final float trialColWidth;
    private final float cohortColWidth;
    private final float ineligibilityReasonColWith;
    private final boolean skipMatchingTrialDetails;

    @NotNull
    public static IneligibleActinTrialsGenerator fromEvaluatedTrials(@NotNull List<EvaluatedTrial> trials, float contentWidth,
            boolean skipMatchingTrialDetails) {
        List<EvaluatedTrial> ineligibleTrials = trials.stream()
                .filter(trial -> !trial.isPotentiallyEligible() && (trial.isOpen() || !skipMatchingTrialDetails))
                .collect(Collectors.toList());

        float trialColWidth = contentWidth / 9;
        float cohortColWidth = contentWidth / 2;
        float ineligibilityReasonColWidth = contentWidth - (trialColWidth + cohortColWidth);

        return new IneligibleActinTrialsGenerator(ineligibleTrials,
                TreatmentConstants.ACTIN_SOURCE,
                trialColWidth,
                cohortColWidth,
                ineligibilityReasonColWidth,
                skipMatchingTrialDetails);
    }

    private IneligibleActinTrialsGenerator(@NotNull final List<EvaluatedTrial> trials, @NotNull final String source,
            final float trialColWidth, final float cohortColWidth, final float ineligibilityReasonColWith,
            final boolean skipMatchingTrialDetails) {
        this.trials = trials;
        this.source = source;
        this.trialColWidth = trialColWidth;
        this.cohortColWidth = cohortColWidth;
        this.ineligibilityReasonColWith = ineligibilityReasonColWith;
        this.skipMatchingTrialDetails = skipMatchingTrialDetails;
    }

    @NotNull
    @Override
    public String title() {
        return String.format("%s trials and cohorts that are %sconsidered ineligible (%s)",
                source,
                skipMatchingTrialDetails ? "open but " : "",
                trials.size());
    }

    @NotNull
    @Override
    public Table contents() {
        Table table = Tables.createFixedWidthCols(trialColWidth, cohortColWidth + ineligibilityReasonColWith);

        if (!trials.isEmpty()) {
            table.addHeaderCell(Cells.createContentNoBorder(Cells.createHeader("Trial")));

            Table headerSubTable = Tables.createFixedWidthCols(cohortColWidth, ineligibilityReasonColWith);
            headerSubTable.addHeaderCell(Cells.createHeader("Cohort"));
            headerSubTable.addHeaderCell(Cells.createHeader("Ineligibility reasons"));

            table.addHeaderCell(Cells.createContentNoBorder(headerSubTable));
        }

        ActinTrialGeneratorFunctions.streamSortedCohorts(trials).forEach(cohortList -> {
            Table trialSubTable = Tables.createFixedWidthCols(cohortColWidth, ineligibilityReasonColWith);

            cohortList.forEach(cohort -> {
                String cohortText = ActinTrialGeneratorFunctions.createCohortString(cohort);
                String ineligibilityText = cohort.fails().isEmpty() ? "?" : String.join(", ", cohort.fails());

                ActinTrialGeneratorFunctions.addContentStreamToTable(Stream.of(cohortText, ineligibilityText),
                        !cohort.isOpen(),
                        trialSubTable);
            });
            ActinTrialGeneratorFunctions.insertTrialRow(cohortList, table, trialSubTable);
        });

        String subNote = "";
        if (trials.stream().anyMatch(trial -> !trial.isOpen())) {
            subNote += " Cohorts shown in grey are closed.";
        }
        if (trials.stream().anyMatch(trial -> trial.isOpen() && !trial.hasSlotsAvailable())) {
            subNote += " Cohorts with no slots available are indicated by an asterisk (*).";
        }
        if (!subNote.isEmpty()) {
            table.addCell(Cells.createSpanningSubNote(subNote, table));
        }

        return Tables.makeWrapping(table);
    }
}
