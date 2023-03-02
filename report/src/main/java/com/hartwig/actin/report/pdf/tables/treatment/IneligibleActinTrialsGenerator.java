package com.hartwig.actin.report.pdf.tables.treatment;

import java.util.List;
import java.util.Set;
import java.util.StringJoiner;
import java.util.stream.Collectors;

import com.hartwig.actin.report.interpretation.EvaluatedTrial;
import com.hartwig.actin.report.interpretation.EvaluatedTrialComparator;
import com.hartwig.actin.report.pdf.tables.TableGenerator;
import com.hartwig.actin.report.pdf.util.Cells;
import com.hartwig.actin.report.pdf.util.Formats;
import com.hartwig.actin.report.pdf.util.Tables;
import com.hartwig.actin.treatment.TreatmentConstants;
import com.itextpdf.layout.element.Table;

import org.apache.logging.log4j.util.Strings;
import org.jetbrains.annotations.NotNull;

public class IneligibleActinTrialsGenerator implements TableGenerator {

    @NotNull
    private final List<EvaluatedTrial> trials;
    @NotNull
    private final String source;
    private final float trialColWidth;
    private final float acronymColWidth;
    private final float cohortColWidth;
    private final float recruitingColWidth;
    private final float ineligibilityReasonColWith;
    private final boolean skipMatchingTrialDetails;

    @NotNull
    public static IneligibleActinTrialsGenerator fromEvaluatedTrials(@NotNull List<EvaluatedTrial> trials, float contentWidth,
            boolean skipMatchingTrialDetails) {
        List<EvaluatedTrial> ineligibleTrials = trials.stream()
                .filter(trial -> !trial.isPotentiallyEligible() && (trial.isOpen() || !skipMatchingTrialDetails))
                .collect(Collectors.toList());

        float trialColWidth = contentWidth / 9;
        float acronymColWidth = contentWidth / 9;
        float cohortColWidth = contentWidth / 3;
        float recruitingColWidth = contentWidth / 11;
        float ineligibilityReasonColWidth = contentWidth - (trialColWidth + acronymColWidth + cohortColWidth + recruitingColWidth);

        return new IneligibleActinTrialsGenerator(ineligibleTrials,
                TreatmentConstants.ACTIN_SOURCE,
                trialColWidth,
                acronymColWidth,
                cohortColWidth,
                recruitingColWidth,
                ineligibilityReasonColWidth, skipMatchingTrialDetails);
    }

    private IneligibleActinTrialsGenerator(@NotNull final List<EvaluatedTrial> trials, @NotNull final String source,
            final float trialColWidth, final float acronymColWidth, final float cohortColWidth, final float recruitingColWidth,
            final float ineligibilityReasonColWith, final boolean skipMatchingTrialDetails) {
        this.trials = trials;
        this.source = source;
        this.trialColWidth = trialColWidth;
        this.acronymColWidth = acronymColWidth;
        this.cohortColWidth = cohortColWidth;
        this.recruitingColWidth = recruitingColWidth;
        this.ineligibilityReasonColWith = ineligibilityReasonColWith;
        this.skipMatchingTrialDetails = skipMatchingTrialDetails;
    }

    @NotNull
    @Override
    public String title() {
        return String.format("%s trials and cohorts that are %sconsidered ineligible (%s)", source,
                skipMatchingTrialDetails ? "open but " : "", trials.size());
    }

    @NotNull
    @Override
    public Table contents() {
        Table table =
                Tables.createFixedWidthCols(trialColWidth, acronymColWidth, cohortColWidth, recruitingColWidth, ineligibilityReasonColWith);

        table.addHeaderCell(Cells.createHeader("Trial"));
        table.addHeaderCell(Cells.createHeader("Acronym"));
        table.addHeaderCell(Cells.createHeader("Cohort"));
        table.addHeaderCell(Cells.createHeader("Open"));
        table.addHeaderCell(Cells.createHeader("Ineligibility reasons"));

        boolean hasTrialWithNoSlots = false;
        for (EvaluatedTrial trial : sort(trials)) {
            String addon = Strings.EMPTY;
            if (trial.isOpen() && !trial.hasSlotsAvailable()) {
                addon = " *";
                hasTrialWithNoSlots = true;
            }
            table.addCell(Cells.createContent(trial.trialId() + addon));
            table.addCell(Cells.createContent(trial.acronym()));
            table.addCell(Cells.createContent(trial.cohort() != null ? trial.cohort() : Strings.EMPTY));
            table.addCell(Cells.createContentYesNo(trial.isOpen() ? "Yes" : "No"));
            table.addCell(Cells.createContent(concat(trial.fails())));
        }

        if (hasTrialWithNoSlots) {
            table.addCell(Cells.createSpanningSubNote(" * Cohort currently has no slots available", table));
        }

        return Tables.makeWrapping(table);
    }

    @NotNull
    private static List<EvaluatedTrial> sort(@NotNull List<EvaluatedTrial> trials) {
        return trials.stream().sorted(new EvaluatedTrialComparator()).collect(Collectors.toList());
    }

    @NotNull
    private static String concat(@NotNull Set<String> strings) {
        StringJoiner joiner = Formats.commaJoiner();
        for (String string : strings) {
            joiner.add(string);
        }
        return Formats.valueOrDefault(joiner.toString(), "?");
    }
}
