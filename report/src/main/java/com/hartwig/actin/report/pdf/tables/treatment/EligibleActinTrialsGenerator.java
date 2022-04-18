package com.hartwig.actin.report.pdf.tables.treatment;

import java.util.List;
import java.util.Set;
import java.util.StringJoiner;

import com.google.common.collect.Lists;
import com.hartwig.actin.algo.datamodel.TreatmentMatch;
import com.hartwig.actin.molecular.datamodel.evidence.MolecularEvidence;
import com.hartwig.actin.report.interpretation.EvaluatedTrial;
import com.hartwig.actin.report.interpretation.EvaluatedTrialFactory;
import com.hartwig.actin.report.pdf.tables.TableGenerator;
import com.hartwig.actin.report.pdf.util.Cells;
import com.hartwig.actin.report.pdf.util.Formats;
import com.hartwig.actin.report.pdf.util.Tables;
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

    @NotNull
    public static EligibleActinTrialsGenerator forOpenTrials(@NotNull TreatmentMatch treatmentMatch, @NotNull MolecularEvidence evidence,
            float width) {
        List<EvaluatedTrial> openAndEligible = Lists.newArrayList();
        for (EvaluatedTrial trial : EvaluatedTrialFactory.create(treatmentMatch, evidence.actinTrials())) {
            if (trial.isPotentiallyEligible() && trial.isOpen()) {
                openAndEligible.add(trial);
            }
        }

        String title = evidence.actinSource() + " trials that are open and considered eligible (" + openAndEligible.size() + ")";
        return create(openAndEligible, title, width);
    }

    @NotNull
    public static EligibleActinTrialsGenerator forClosedTrials(@NotNull TreatmentMatch treatmentMatch, @NotNull MolecularEvidence evidence,
            float contentWidth) {
        List<EvaluatedTrial> closedAndEligible = Lists.newArrayList();
        for (EvaluatedTrial trial : EvaluatedTrialFactory.create(treatmentMatch, evidence.actinTrials())) {
            if (trial.isPotentiallyEligible() && !trial.isOpen()) {
                closedAndEligible.add(trial);
            }
        }

        String title = evidence.actinSource() + " trials that are closed but considered eligible (" + closedAndEligible.size() + ")";
        return create(closedAndEligible, title, contentWidth);
    }

    @NotNull
    private static EligibleActinTrialsGenerator create(@NotNull List<EvaluatedTrial> trials, @NotNull String title, float width) {
        float trialColWidth = width / 10;
        float acronymColWidth = width / 10;
        float cohortColWidth = width / 3;
        float molecularColWidth = width / 10;
        float checksColWidth = width - (trialColWidth + acronymColWidth + cohortColWidth + molecularColWidth);

        return new EligibleActinTrialsGenerator(trials,
                title,
                trialColWidth,
                acronymColWidth,
                cohortColWidth,
                molecularColWidth,
                checksColWidth);
    }

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

        for (EvaluatedTrial trial : trials) {
            table.addCell(Cells.createContent(trial.trialId()));
            table.addCell(Cells.createContent(trial.acronym()));
            table.addCell(Cells.createContent(trial.cohort() != null ? trial.cohort() : Strings.EMPTY));
            table.addCell(Cells.createContentYesNo(trial.hasMolecularEvidence() ? "Yes" : "No"));
            table.addCell(Cells.createContent(concat(trial.warnings())));
        }

        return Tables.makeWrapping(table);
    }

    @NotNull
    private static String concat(@NotNull Set<String> strings) {
        StringJoiner joiner = Formats.commaJoiner();
        for (String string : strings) {
            joiner.add(string);
        }
        return Formats.valueOrDefault(joiner.toString(), "None");
    }
}
