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

public class EligibleActinTrialsGenerator implements TableGenerator {

    @NotNull
    private final List<EvaluatedTrial> trials;
    @NotNull
    private final String source;
    private final float trialIdColWidth;
    private final float acronymColWidth;
    private final float cohortColWidth;
    private final float molecularEventColWidth;
    private final float criteriaToCheckColWidth;
    private final float mainCheckColWidth;
    private final float isOpenColWidth;


    @NotNull
    public static EligibleActinTrialsGenerator fromTreatmentMatch(@NotNull TreatmentMatch treatmentMatch, @NotNull String source,
            float contentWidth) {
        float trialIdColWidth = contentWidth / 10;
        float acronymColWidth = contentWidth / 10;
        float cohortColWidth = contentWidth / 10;
        float molecularColWidth = contentWidth / 10;
        float criteriaToCheckColWidth = contentWidth / 10;
        float isOpenColWidth = contentWidth / 10;
        float mainCheckColWidth =
                contentWidth - (trialIdColWidth + acronymColWidth + cohortColWidth + molecularColWidth + criteriaToCheckColWidth
                        + isOpenColWidth);

        List<EvaluatedTrial> trials = EvaluatedTrialExtractor.extract(treatmentMatch);
        List<EvaluatedTrial> filtered = Lists.newArrayList();
        for (EvaluatedTrial trial : trials) {
            if (trial.isPotentiallyEligible()) {
                filtered.add(trial);
            }
        }
        return new EligibleActinTrialsGenerator(filtered,
                source,
                trialIdColWidth,
                acronymColWidth,
                cohortColWidth,
                molecularColWidth,
                criteriaToCheckColWidth,
                mainCheckColWidth,
                isOpenColWidth);
    }

    public EligibleActinTrialsGenerator(@NotNull final List<EvaluatedTrial> trials, @NotNull final String source,
            final float trialIdColWidth, final float acronymColWidth, final float cohortColWidth, final float molecularEventColWidth,
            final float criteriaToCheckColWidth, final float mainCheckColWidth, final float isOpenColWidth) {
        this.trials = trials;
        this.source = source;
        this.trialIdColWidth = trialIdColWidth;
        this.acronymColWidth = acronymColWidth;
        this.cohortColWidth = cohortColWidth;
        this.molecularEventColWidth = molecularEventColWidth;
        this.criteriaToCheckColWidth = criteriaToCheckColWidth;
        this.mainCheckColWidth = mainCheckColWidth;
        this.isOpenColWidth = isOpenColWidth;
    }

    @NotNull
    @Override
    public String title() {
        return source + " trials and cohorts considered potentially eligible (" + eligibleCount(trials) + ")";
    }

    private static int eligibleCount(@NotNull List<EvaluatedTrial> trials) {
        int eligibleCount = 0;
        for (EvaluatedTrial trial : trials) {
            if (trial.isPotentiallyEligible()) {
                eligibleCount++;
            }
        }
        return eligibleCount;
    }

    @NotNull
    @Override
    public Table contents() {
        Table table = Tables.createFixedWidthCols(trialIdColWidth,
                acronymColWidth,
                cohortColWidth,
                molecularEventColWidth,
                criteriaToCheckColWidth,
                mainCheckColWidth,
                isOpenColWidth);

        table.addHeaderCell(Cells.createHeader("Trial ID"));
        table.addHeaderCell(Cells.createHeader("Acronym"));
        table.addHeaderCell(Cells.createHeader("Cohort"));
        table.addHeaderCell(Cells.createHeader("Molecular event"));
        table.addHeaderCell(Cells.createHeader("# Criteria to check"));
        table.addHeaderCell(Cells.createHeader("# Main check"));
        table.addHeaderCell(Cells.createHeader("Cohort open"));

        for (EvaluatedTrial trial : trials) {
            table.addCell(Cells.createContent(trial.trialId()));
            table.addCell(Cells.createContent(trial.acronym()));
            table.addCell(Cells.createContent(trial.cohort() != null ? trial.cohort() : Strings.EMPTY));
            table.addCell(Cells.createContent("TODO"));
            table.addCell(Cells.createContent(String.valueOf(trial.evaluationsToCheckCount())));
            table.addCell(Cells.createContent(concat(trial.evaluationsToCheckMessages())));
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
        return Formats.valueOrDefault(joiner.toString(), "None");
    }
}
