package com.hartwig.actin.report.pdf.tables.treatment;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.hartwig.actin.report.interpretation.EvaluatedTrial;
import com.hartwig.actin.report.pdf.tables.TableGenerator;
import com.hartwig.actin.report.pdf.util.Cells;
import com.hartwig.actin.report.pdf.util.Formats;
import com.hartwig.actin.report.pdf.util.Tables;
import com.hartwig.actin.treatment.TreatmentConstants;
import com.itextpdf.layout.element.Table;

import org.jetbrains.annotations.NotNull;

public class EligibleActinTrialsGenerator implements TableGenerator {

    @NotNull
    private final List<EvaluatedTrial> trials;
    @NotNull
    private final String title;

    private final float trialColWidth;
    private final float cohortColWidth;
    private final float molecularEventColWidth;
    private final float checksColWidth;

    @NotNull
    public static EligibleActinTrialsGenerator forOpenTrials(@NotNull List<EvaluatedTrial> trials, float width) {
        List<EvaluatedTrial> recruitingAndEligible =
                trials.stream().filter(trial -> trial.isPotentiallyEligible() && trial.isOpen()).collect(Collectors.toList());

        String title = String.format("%s trials that are open and considered eligible (%s)",
                TreatmentConstants.ACTIN_SOURCE,
                recruitingAndEligible.size());

        return create(recruitingAndEligible, title, width);
    }

    @NotNull
    public static EligibleActinTrialsGenerator forClosedTrials(@NotNull List<EvaluatedTrial> trials, float contentWidth,
            boolean skipMatchingTrialDetails) {
        List<EvaluatedTrial> unavailableAndEligible = trials.stream()
                .filter(trial -> trial.isPotentiallyEligible() && !trial.isOpen())
                .filter(trial -> !trial.molecularEvents().isEmpty() || !skipMatchingTrialDetails)
                .collect(Collectors.toList());

        String title = String.format("%s trials and cohorts that %smay be eligible, but are closed (%s)",
                TreatmentConstants.ACTIN_SOURCE,
                skipMatchingTrialDetails ? "meet molecular requirements and " : "",
                unavailableAndEligible.size());
        return create(unavailableAndEligible, title, contentWidth);
    }

    @NotNull
    private static EligibleActinTrialsGenerator create(@NotNull List<EvaluatedTrial> trials, @NotNull String title, float width) {
        float trialColWidth = width / 9;
        float cohortColWidth = width / 3;
        float molecularColWidth = width / 5;
        float checksColWidth = width - (trialColWidth + cohortColWidth + molecularColWidth);

        return new EligibleActinTrialsGenerator(trials, title, trialColWidth, cohortColWidth, molecularColWidth, checksColWidth);
    }

    private EligibleActinTrialsGenerator(@NotNull final List<EvaluatedTrial> trials, @NotNull final String title, final float trialColWidth,
            final float cohortColWidth, final float molecularEventColWidth, final float checksColWidth) {
        this.trials = trials;
        this.title = title;
        this.trialColWidth = trialColWidth;
        this.cohortColWidth = cohortColWidth;
        this.molecularEventColWidth = molecularEventColWidth;
        this.checksColWidth = checksColWidth;
    }

    @NotNull
    @Override
    public String title() {
        return title;
    }

    @NotNull
    @Override
    public Table contents() {
        Table table = Tables.createFixedWidthCols(trialColWidth, cohortColWidth + molecularEventColWidth + checksColWidth);

        if (!trials.isEmpty()) {
            table.addHeaderCell(Cells.createContentNoBorder(Cells.createHeader("Trial")));

            Table headerSubTable = Tables.createFixedWidthCols(cohortColWidth, molecularEventColWidth, checksColWidth);
            headerSubTable.addHeaderCell(Cells.createHeader("Cohort"));
            headerSubTable.addHeaderCell(Cells.createHeader("Molecular"));
            headerSubTable.addHeaderCell(Cells.createHeader("Warnings"));

            table.addHeaderCell(Cells.createContentNoBorder(headerSubTable));
        }

        ActinTrialGeneratorFunctions.streamSortedCohorts(trials).forEach(cohortList -> {
            Table trialSubTable = Tables.createFixedWidthCols(cohortColWidth, molecularEventColWidth, checksColWidth);

            cohortList.forEach(cohort -> {
                String cohortText = ActinTrialGeneratorFunctions.createCohortString(cohort);
                Stream<String> cellContents = Stream.of(cohortText, concat(cohort.molecularEvents()), concat(cohort.warnings()));
                ActinTrialGeneratorFunctions.addContentStreamToTable(cellContents,
                        cohort.isOpen() && !cohort.hasSlotsAvailable(),
                        trialSubTable);
            });
            ActinTrialGeneratorFunctions.insertTrialRow(cohortList, table, trialSubTable);
        });

        if (trials.stream().anyMatch(trial -> trial.isOpen() && !trial.hasSlotsAvailable())) {
            table.addCell(Cells.createSpanningSubNote(" * Cohort currently has no slots available", table));
        }

        return Tables.makeWrapping(table);
    }

    @NotNull
    private static String concat(@NotNull Set<String> strings) {
        String concatenatedString = String.join(Formats.COMMA_SEPARATOR, strings);
        return concatenatedString.isEmpty() ? Formats.VALUE_NONE : concatenatedString;
    }
}
