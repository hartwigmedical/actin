package com.hartwig.actin.report.pdf.tables;

import java.util.List;
import java.util.Set;
import java.util.StringJoiner;

import com.google.common.collect.Lists;
import com.hartwig.actin.algo.datamodel.TreatmentMatch;
import com.hartwig.actin.algo.interpretation.EvaluatedTrial;
import com.hartwig.actin.algo.interpretation.EvaluatedTrialExtractor;
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
    private final float molecularEventColWidth;
    private final float ineligibilityReasonColWith;
    private final float cohortOpenColWidth;

    @NotNull
    public static IneligibleActinTrialsGenerator fromTreatmentMatch(@NotNull TreatmentMatch treatmentMatch, @NotNull String source,
            float contentWidth) {
        List<EvaluatedTrial> ineligibleTrials = Lists.newArrayList();
        for (EvaluatedTrial trial : EvaluatedTrialExtractor.extract(treatmentMatch)) {
            if (!trial.isPotentiallyEligible()) {
                ineligibleTrials.add(trial);
            }
        }

        float trialColWidth = contentWidth / 10;
        float acronymColWidth = contentWidth / 10;
        float cohortColWidth = contentWidth / 4;
        float molecularColWidth = contentWidth / 10;
        float cohortOpenColWidth = contentWidth / 10;
        float ineligibilityReasonColWidth =
                contentWidth - (trialColWidth + acronymColWidth + cohortColWidth + molecularColWidth + cohortOpenColWidth);

        return new IneligibleActinTrialsGenerator(ineligibleTrials,
                source,
                trialColWidth,
                acronymColWidth,
                cohortColWidth,
                molecularColWidth,
                ineligibilityReasonColWidth,
                cohortOpenColWidth);
    }

    private IneligibleActinTrialsGenerator(@NotNull final List<EvaluatedTrial> trials, @NotNull final String source,
            final float trialColWidth, final float acronymColWidth, final float cohortColWidth, final float molecularEventColWidth,
            final float ineligibilityReasonColWith, final float cohortOpenColWidth) {
        this.trials = trials;
        this.source = source;
        this.trialColWidth = trialColWidth;
        this.acronymColWidth = acronymColWidth;
        this.cohortColWidth = cohortColWidth;
        this.molecularEventColWidth = molecularEventColWidth;
        this.ineligibilityReasonColWith = ineligibilityReasonColWith;
        this.cohortOpenColWidth = cohortOpenColWidth;
    }

    @NotNull
    @Override
    public String title() {
        return source + " trials not considered eligible (" + trials.size() + ")";
    }

    @NotNull
    @Override
    public Table contents() {
        Table table = Tables.createFixedWidthCols(trialColWidth,
                acronymColWidth,
                cohortColWidth,
                molecularEventColWidth,
                ineligibilityReasonColWith,
                cohortOpenColWidth);

        table.addHeaderCell(Cells.createHeader("Trial"));
        table.addHeaderCell(Cells.createHeader("Acronym"));
        table.addHeaderCell(Cells.createHeader("Cohort"));
        table.addHeaderCell(Cells.createHeader("Molecular event"));
        table.addHeaderCell(Cells.createHeader("Ineligibility reasons"));
        table.addHeaderCell(Cells.createHeader("Cohort open"));

        for (EvaluatedTrial trial : trials) {
            table.addCell(Cells.createContent(trial.trialId()));
            table.addCell(Cells.createContent(trial.acronym()));
            table.addCell(Cells.createContent(trial.cohort() != null ? trial.cohort() : Strings.EMPTY));
            table.addCell(Cells.createContent("TODO"));
            table.addCell(Cells.createContent(concat(trial.fails())));
            table.addCell(Cells.createContentYesNo(trial.isOpen() ? "Yes" : "No"));
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
