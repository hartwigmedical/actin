package com.hartwig.actin.report.pdf.tables.treatment;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.StringJoiner;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.common.collect.Lists;
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

public class EligibleActinTrialsGenerator implements TableGenerator {

    @NotNull
    private final List<EvaluatedTrial> trials;
    @NotNull
    private final String title;

    private final float trialColWidth;
    private final float acronymColWidth;
    private final float cohortColWidth;
    private final float molecularEventColWidth;
    private final float checksColWidth;

    private EligibleActinTrialsGenerator(@NotNull final List<EvaluatedTrial> trials, @NotNull final String title, final float trialColWidth,
            final float acronymColWidth, final float cohortColWidth, final float molecularEventColWidth, final float checksColWidth) {
        this.trials = trials;
        this.title = title;
        this.trialColWidth = trialColWidth;
        this.acronymColWidth = acronymColWidth;
        this.cohortColWidth = cohortColWidth;
        this.molecularEventColWidth = molecularEventColWidth;
        this.checksColWidth = checksColWidth;
    }

    @NotNull
    public static EligibleActinTrialsGenerator forOpenTrials(@NotNull List<EvaluatedTrial> trials, float width) {
        List<EvaluatedTrial> recruitingAndEligible = Lists.newArrayList();
        for (EvaluatedTrial trial : trials) {
            if (trial.isPotentiallyEligible() && trial.isOpen()) {
                recruitingAndEligible.add(trial);
            }
        }

        String title = String.format("%s trials that are open and considered eligible (%s)",
                TreatmentConstants.ACTIN_SOURCE,
                recruitingAndEligible.size());

        return create(recruitingAndEligible, title, width);
    }

    @NotNull
    public static EligibleActinTrialsGenerator forClosedTrials(@NotNull List<EvaluatedTrial> trials, float contentWidth) {
        List<EvaluatedTrial> unavailableAndEligible = Lists.newArrayList();
        for (EvaluatedTrial trial : trials) {
            if (trial.isPotentiallyEligible() && !trial.isOpen()) {
                unavailableAndEligible.add(trial);
            }
        }

        String title = String.format("%s trials and cohorts that are considered ineligible (%s)",
                TreatmentConstants.ACTIN_SOURCE,
                unavailableAndEligible.size());
        return create(unavailableAndEligible, title, contentWidth);
    }

    @NotNull
    private static EligibleActinTrialsGenerator create(@NotNull List<EvaluatedTrial> trials, @NotNull String title, float width) {
        float trialColWidth = width / 9;
        float acronymColWidth = width / 11;
        float cohortColWidth = width / 3;
        float molecularColWidth = width / 8;
        float checksColWidth = width - (trialColWidth + acronymColWidth + cohortColWidth + molecularColWidth);

        return new EligibleActinTrialsGenerator(trials,
                title,
                trialColWidth,
                acronymColWidth,
                cohortColWidth,
                molecularColWidth,
                checksColWidth);
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
        return Formats.valueOrDefault(joiner.toString(), "None");
    }

    @NotNull
    @Override
    public String title() {
        return title;
    }

    @NotNull
    @Override
    public Table contents() {
        Table table = Tables.createFixedWidthCols(trialColWidth, acronymColWidth, cohortColWidth, molecularEventColWidth, checksColWidth);

        table.addHeaderCell(Cells.createHeader("Trial"));
        table.addHeaderCell(Cells.createHeader("Acronym"));
        table.addHeaderCell(Cells.createHeader("Cohort"));
        table.addHeaderCell(Cells.createHeader("Molecular"));
        table.addHeaderCell(Cells.createHeader("Warnings"));

        boolean hasTrialWithNoSlots = false;
        for (EvaluatedTrial trial : sort(trials)) {
            String trialIdText = trial.trialId();
            boolean noSlotsAvailable = trial.isOpen() && !trial.hasSlotsAvailable();
            if (noSlotsAvailable) {
                trialIdText += " *";
                hasTrialWithNoSlots = true;
            }
            Stream.of(trialIdText,
                    trial.acronym(),
                    Optional.ofNullable(trial.cohort()).orElse(Strings.EMPTY),
                    concat(trial.molecularEvents()),
                    concat(trial.warnings())).map(text -> {
                if (noSlotsAvailable) {
                    return Cells.createContentGrey(text);
                } else {
                    return Cells.createContent(text);
                }
            }).forEach(table::addCell);
        }

        if (hasTrialWithNoSlots) {
            table.addCell(Cells.createSpanningSubNote(" * Cohort currently has no slots available", table));
        }

        return Tables.makeWrapping(table);
    }
}
