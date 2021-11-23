package com.hartwig.actin.report.pdf.chapters;

import com.hartwig.actin.algo.datamodel.CohortEligibility;
import com.hartwig.actin.algo.datamodel.TrialEligibility;
import com.hartwig.actin.algo.interpretation.EvaluationSummarizer;
import com.hartwig.actin.algo.interpretation.EvaluationSummary;
import com.hartwig.actin.algo.interpretation.TreatmentMatchSummarizer;
import com.hartwig.actin.algo.interpretation.TreatmentMatchSummary;
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

        table.addCell(Cells.createKey("Number of trials evaluated"));
        table.addCell(Cells.createValue(String.valueOf(summary.trialCount())));

        table.addCell(Cells.createKey("Number of trials considered potentially eligible"));
        table.addCell(Cells.createValue(String.valueOf(summary.eligibleTrialCount())));

        table.addCell(Cells.createKey("Number of cohorts evaluated"));
        table.addCell(Cells.createValue(String.valueOf(summary.cohortCount())));

        table.addCell(Cells.createKey("Number of cohorts considered potentially eligible"));
        table.addCell(Cells.createValue(String.valueOf(summary.eligibleCohortCount())));

        table.addCell(Cells.createKey("Number of open cohorts considered potentially eligible"));
        table.addCell(Cells.createValue(String.valueOf(summary.eligibleOpenCohortCount())));

        return table;
    }

    private void addTreatmentDetailsTable(@NotNull Document document) {
        Table table = Tables.createFixedWidthCols(new float[] { 3, 1, 1, 1, 1, 1, 1, 1 }).setWidth(contentWidth());

        table.addHeaderCell(Cells.createHeader("Trial / Cohort"));
        table.addHeaderCell(Cells.createHeader("# Criteria"));
        table.addHeaderCell(Cells.createHeader("# Passed"));
        table.addHeaderCell(Cells.createHeader("# Warnings"));
        table.addHeaderCell(Cells.createHeader("# Failed"));
        table.addHeaderCell(Cells.createHeader("# Undetermined"));
        table.addHeaderCell(Cells.createHeader("# Ignored"));
        table.addHeaderCell(Cells.createHeader("# Non-implemented"));

        for (TrialEligibility trial : report.treatmentMatch().trialMatches()) {
            String trialId = trial.identification().trialId();
            table.addCell(Cells.createContent(trialId));
            addSummaryToTable(table, EvaluationSummarizer.summarize(trial.evaluations().values()));
            for (CohortEligibility cohort : trial.cohorts()) {
                table.addCell(Cells.createContent(trialId + " - " + cohort.metadata().description()));
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
        table.addCell(Cells.createContent(String.valueOf(summary.ignoredCount())));
        table.addCell(Cells.createContent(String.valueOf(summary.nonImplementedCount())));
    }
}
