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

    @NotNull
    public static IneligibleActinTrialsGenerator fromTreatmentMatch(@NotNull TreatmentMatch treatmentMatch,
            @NotNull MolecularEvidence evidence, float contentWidth) {
        List<EvaluatedTrial> ineligibleTrials = Lists.newArrayList();
        for (EvaluatedTrial trial : EvaluatedTrialFactory.create(treatmentMatch, evidence.actinTrials())) {
            if (!trial.isPotentiallyEligible()) {
                ineligibleTrials.add(trial);
            }
        }

        float trialColWidth = contentWidth / 10;
        float acronymColWidth = contentWidth / 10;
        float cohortColWidth = contentWidth / 3;
        float recruitingColWidth = contentWidth / 10;
        float ineligibilityReasonColWidth = contentWidth - (trialColWidth + acronymColWidth + cohortColWidth + recruitingColWidth);

        return new IneligibleActinTrialsGenerator(ineligibleTrials,
                evidence.actinSource(),
                trialColWidth,
                acronymColWidth,
                cohortColWidth,
                recruitingColWidth,
                ineligibilityReasonColWidth);
    }

    private IneligibleActinTrialsGenerator(@NotNull final List<EvaluatedTrial> trials, @NotNull final String source,
            final float trialColWidth, final float acronymColWidth, final float cohortColWidth, final float recruitingColWidth,
            final float ineligibilityReasonColWith) {
        this.trials = trials;
        this.source = source;
        this.trialColWidth = trialColWidth;
        this.acronymColWidth = acronymColWidth;
        this.cohortColWidth = cohortColWidth;
        this.recruitingColWidth = recruitingColWidth;
        this.ineligibilityReasonColWith = ineligibilityReasonColWith;
    }

    @NotNull
    @Override
    public String title() {
        return source + " trials not considered eligible (" + trials.size() + ")";
    }

    @NotNull
    @Override
    public Table contents() {
        Table table =
                Tables.createFixedWidthCols(trialColWidth, acronymColWidth, cohortColWidth, recruitingColWidth, ineligibilityReasonColWith);

        table.addHeaderCell(Cells.createHeader("Trial"));
        table.addHeaderCell(Cells.createHeader("Acronym"));
        table.addHeaderCell(Cells.createHeader("Cohort"));
        table.addHeaderCell(Cells.createHeader("Recruiting"));
        table.addHeaderCell(Cells.createHeader("Ineligibility reasons"));

        for (EvaluatedTrial trial : trials) {
            table.addCell(Cells.createContent(trial.trialId()));
            table.addCell(Cells.createContent(trial.acronym()));
            table.addCell(Cells.createContent(trial.cohort() != null ? trial.cohort() : Strings.EMPTY));
            table.addCell(Cells.createContentYesNo(trial.isOpenAndHasSlotsAvailable() ? "Yes" : "No"));
            table.addCell(Cells.createContent(concat(trial.fails())));
        }

        return Tables.makeWrapping(table);
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
