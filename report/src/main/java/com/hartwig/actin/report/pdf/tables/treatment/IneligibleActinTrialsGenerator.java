package com.hartwig.actin.report.pdf.tables.treatment;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.hartwig.actin.report.interpretation.EvaluatedCohort;
import com.hartwig.actin.report.pdf.tables.TableGenerator;
import com.hartwig.actin.report.pdf.util.Cells;
import com.hartwig.actin.report.pdf.util.Tables;
import com.hartwig.actin.treatment.TreatmentConstants;
import com.itextpdf.layout.element.Table;

import org.jetbrains.annotations.NotNull;

public class IneligibleActinTrialsGenerator implements TableGenerator {

    @NotNull
    private final List<EvaluatedCohort> cohorts;
    @NotNull
    private final String source;
    private final float trialColWidth;
    private final float cohortColWidth;
    private final float ineligibilityReasonColWith;
    private final boolean skipMatchingTrialDetails;

    @NotNull
    public static IneligibleActinTrialsGenerator fromEvaluatedCohorts(@NotNull List<EvaluatedCohort> cohorts, float contentWidth,
            boolean skipMatchingTrialDetails) {
        List<EvaluatedCohort> ineligibleCohorts = cohorts.stream()
                .filter(cohort -> !cohort.isPotentiallyEligible() && (cohort.isOpen() || !skipMatchingTrialDetails))
                .collect(Collectors.toList());

        float trialColWidth = contentWidth / 9;
        float cohortColWidth = contentWidth / 2;
        float ineligibilityReasonColWidth = contentWidth - (trialColWidth + cohortColWidth);

        return new IneligibleActinTrialsGenerator(ineligibleCohorts,
                TreatmentConstants.ACTIN_SOURCE,
                trialColWidth,
                cohortColWidth,
                ineligibilityReasonColWidth,
                skipMatchingTrialDetails);
    }

    private IneligibleActinTrialsGenerator(@NotNull final List<EvaluatedCohort> cohorts, @NotNull final String source,
            final float trialColWidth, final float cohortColWidth, final float ineligibilityReasonColWith,
            final boolean skipMatchingTrialDetails) {
        this.cohorts = cohorts;
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
                cohorts.size());
    }

    @NotNull
    @Override
    public Table contents() {
        Table table = Tables.createFixedWidthCols(trialColWidth, cohortColWidth + ineligibilityReasonColWith);

        if (!cohorts.isEmpty()) {
            table.addHeaderCell(Cells.createContentNoBorder(Cells.createHeader("Trial")));

            Table headerSubTable = Tables.createFixedWidthCols(cohortColWidth, ineligibilityReasonColWith);
            headerSubTable.addHeaderCell(Cells.createHeader("Cohort"));
            headerSubTable.addHeaderCell(Cells.createHeader("Ineligibility reasons"));

            table.addHeaderCell(Cells.createContentNoBorder(headerSubTable));
        }

        ActinTrialGeneratorFunctions.streamSortedCohorts(cohorts).forEach(cohortList -> {
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
        if (cohorts.stream().anyMatch(cohort -> !cohort.isOpen())) {
            subNote += " Cohorts shown in grey are closed.";
        }
        if (cohorts.stream().anyMatch(cohort -> cohort.isOpen() && !cohort.hasSlotsAvailable())) {
            subNote += " Cohorts with no slots available are indicated by an asterisk (*).";
        }
        if (!subNote.isEmpty()) {
            table.addCell(Cells.createSpanningSubNote(subNote, table));
        }

        return Tables.makeWrapping(table);
    }
}
