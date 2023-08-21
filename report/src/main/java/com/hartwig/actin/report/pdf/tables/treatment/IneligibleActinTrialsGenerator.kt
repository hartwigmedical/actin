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
    private final boolean enableExtendedMode;

    @NotNull
    public static IneligibleActinTrialsGenerator fromEvaluatedCohorts(@NotNull List<EvaluatedCohort> cohorts, float contentWidth,
            boolean enableExtendedMode) {
        List<EvaluatedCohort> ineligibleCohorts = cohorts.stream()
                .filter(cohort -> !cohort.isPotentiallyEligible() && (cohort.isOpen() || enableExtendedMode))
                .collect(Collectors.toList());

        float trialColWidth = contentWidth / 9;
        float cohortColWidth = contentWidth / 4;
        float ineligibilityReasonColWidth = contentWidth - (trialColWidth + cohortColWidth);

        return new IneligibleActinTrialsGenerator(ineligibleCohorts,
                TreatmentConstants.ACTIN_SOURCE,
                trialColWidth,
                cohortColWidth,
                ineligibilityReasonColWidth,
                enableExtendedMode);
    }

    private IneligibleActinTrialsGenerator(@NotNull final List<EvaluatedCohort> cohorts, @NotNull final String source,
            final float trialColWidth, final float cohortColWidth, final float ineligibilityReasonColWith,
            final boolean enableExtendedMode) {
        this.cohorts = cohorts;
        this.source = source;
        this.trialColWidth = trialColWidth;
        this.cohortColWidth = cohortColWidth;
        this.ineligibilityReasonColWith = ineligibilityReasonColWith;
        this.enableExtendedMode = enableExtendedMode;
    }

    @NotNull
    @Override
    public String title() {
        return String.format("%s trials and cohorts that are %sconsidered ineligible (%s)",
                source,
                enableExtendedMode ? "" : "open but ",
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
                        !cohort.isOpen() || !cohort.hasSlotsAvailable(),
                        trialSubTable);
            });
            ActinTrialGeneratorFunctions.insertTrialRow(cohortList, table, trialSubTable);
        });

        String subNote = "";
        if (cohorts.stream().anyMatch(cohort -> !cohort.isOpen())) {
            subNote += " Cohorts shown in grey are closed or have no slots available.";
        }
        if (cohorts.stream().anyMatch(cohort -> cohort.isOpen() && !cohort.hasSlotsAvailable())) {
            subNote += " Open cohorts with no slots available are indicated by an asterisk (*).";
        }
        if (!subNote.isEmpty()) {
            table.addCell(Cells.createSpanningSubNote(subNote, table));
        }

        return Tables.makeWrapping(table);
    }
}
