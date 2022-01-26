package com.hartwig.actin.report.pdf.chapters;

import java.util.Set;
import java.util.StringJoiner;

import com.hartwig.actin.algo.datamodel.CohortEligibility;
import com.hartwig.actin.algo.datamodel.TrialEligibility;
import com.hartwig.actin.algo.interpretation.EvaluationSummarizer;
import com.hartwig.actin.algo.interpretation.EvaluationSummary;
import com.hartwig.actin.algo.interpretation.TreatmentMatchSummarizer;
import com.hartwig.actin.algo.interpretation.TreatmentMatchSummary;
import com.hartwig.actin.algo.util.EligibilityDisplay;
import com.hartwig.actin.report.datamodel.Report;
import com.hartwig.actin.report.pdf.util.Cells;
import com.hartwig.actin.report.pdf.util.Styles;
import com.hartwig.actin.report.pdf.util.Tables;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;

import org.jetbrains.annotations.NotNull;

public class TreatmentSummaryChapter implements ReportChapter {

    @NotNull
    private final Report report;

    public TreatmentSummaryChapter(@NotNull final Report report) {
        this.report = report;
    }

    @NotNull
    @Override
    public String name() {
        return "Treatment Options";
    }

    @NotNull
    @Override
    public PageSize pageSize() {
        return PageSize.A4;
    }

    @Override
    public void render(@NotNull final Document document) {
        addChapterTitle(document);
        addTreatmentSummaryTable(document);
        addTreatmentDetailsTable(document);
    }

    private void addChapterTitle(@NotNull Document document) {
        document.add(new Paragraph(name()).addStyle(Styles.chapterTitleStyle()));
    }

    private void addTreatmentSummaryTable(@NotNull Document document) {
        Table table = Tables.createSingleColWithWidth(contentWidth());
        table.addCell(Cells.createTitle("Trial Eligibility Summary"));
        table.addCell(Cells.create(createTreatmentSummaryTable(TreatmentMatchSummarizer.summarize(report.treatmentMatch()))));

        document.add(table);
    }

    @NotNull
    private Table createTreatmentSummaryTable(@NotNull TreatmentMatchSummary summary) {
        Table table = Tables.createFixedWidthCols(new float[] { 200, contentWidth() - 210 });

        table.addCell(Cells.createKey("# Trials evaluated"));
        table.addCell(Cells.createValue(String.valueOf(summary.trialCount())));

        table.addCell(Cells.createKey("Trials considered potentially eligible"));
        table.addCell(Cells.createValue(eligibleString(summary.eligibleTrials())));

        table.addCell(Cells.createKey("# Cohorts evaluated"));
        table.addCell(Cells.createValue(String.valueOf(summary.cohortCount())));

        table.addCell(Cells.createKey("Cohorts considered potentially eligible"));
        table.addCell(Cells.createValue(eligibleString(summary.eligibleCohorts())));

        table.addCell(Cells.createKey("Open cohorts considered potentially eligible"));
        table.addCell(Cells.createValue(eligibleString(summary.eligibleOpenCohorts())));

        return table;
    }

    @NotNull
    private static String eligibleString(@NotNull Set<String> eligible) {
        if (eligible.isEmpty()) {
            return "None";
        }

        StringJoiner joiner = new StringJoiner(", ");
        for (String string : eligible) {
            joiner.add(string);
        }
        return eligible.size() + " (" + joiner + ")";
    }

    private void addTreatmentDetailsTable(@NotNull Document document) {
        Table table = Tables.createFixedWidthCols(new float[] { 3, 1, 1, 1, 1, 1, 1, 1 }).setWidth(contentWidth());

        table.addHeaderCell(Cells.createHeader("Trial / Cohort"));
        table.addHeaderCell(Cells.createHeader("# Criteria"));
        table.addHeaderCell(Cells.createHeader("# Passed"));
        table.addHeaderCell(Cells.createHeader("# Warnings"));
        table.addHeaderCell(Cells.createHeader("# Failed"));
        table.addHeaderCell(Cells.createHeader("# Undetermined"));
        table.addHeaderCell(Cells.createHeader("# Not evaluated"));
        table.addHeaderCell(Cells.createHeader("# Non-implemented"));

        for (TrialEligibility trial : report.treatmentMatch().trialMatches()) {
            table.addCell(Cells.createContent(EligibilityDisplay.trialName(trial)));
            addSummaryToTable(table, EvaluationSummarizer.summarize(trial.evaluations().values()));
            for (CohortEligibility cohort : trial.cohorts()) {
                table.addCell(Cells.createContent(EligibilityDisplay.cohortName(trial, cohort)));
                addSummaryToTable(table, EvaluationSummarizer.summarize(cohort.evaluations().values()));
            }
        }

        document.add(Tables.makeWrapping(table, "Trial Matching Summary"));
    }

    private static void addSummaryToTable(@NotNull Table table, @NotNull EvaluationSummary summary) {
        table.addCell(Cells.createContent(String.valueOf(summary.count())));
        table.addCell(Cells.createContent(String.valueOf(summary.passedCount())));
        table.addCell(Cells.createContent(String.valueOf(summary.warningCount())));
        table.addCell(Cells.createContent(String.valueOf(summary.failedCount())));
        table.addCell(Cells.createContent(String.valueOf(summary.undeterminedCount())));
        table.addCell(Cells.createContent(String.valueOf(summary.notEvaluatedCount())));
        table.addCell(Cells.createContent(String.valueOf(summary.nonImplementedCount())));
    }
}
