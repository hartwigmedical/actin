package com.hartwig.actin.report.pdf.chapters;

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

public class TrialMatchingSummaryChapter implements ReportChapter {

    @NotNull
    private final Report report;

    public TrialMatchingSummaryChapter(@NotNull final Report report) {
        this.report = report;
    }

    @NotNull
    @Override
    public String name() {
        return "Trial Matching Summary";
    }

    @NotNull
    @Override
    public PageSize pageSize() {
        return PageSize.A4;
    }

    @Override
    public void render(@NotNull Document document) {
        addChapterTitle(document);
        addTrialMatchingOverview(document);
        addTrialMatchingSummaryTable(document);
    }

    private void addChapterTitle(@NotNull Document document) {
        document.add(new Paragraph(name()).addStyle(Styles.chapterTitleStyle()));
    }

    private void addTrialMatchingOverview(@NotNull Document document) {
        TreatmentMatchSummary summary = TreatmentMatchSummarizer.summarize(report.treatmentMatch());

        float keyWidth = 210;
        float valueWidth = contentWidth() - keyWidth - 10;
        Table table = Tables.createFixedWidthCols(keyWidth, valueWidth);

        table.addCell(Cells.createKey("Trials evaluated"));
        table.addCell(Cells.createValue(String.valueOf(summary.trialCount())));

        table.addCell(Cells.createKey("Cohorts evaluated"));
        table.addCell(Cells.createValue(String.valueOf(summary.cohortCount())));

        document.add(Tables.makeWrapping(table, "Trial counts"));
    }

    private void addTrialMatchingSummaryTable(@NotNull Document document) {
        Table table = Tables.createFixedWidthCols(4, 1, 1, 1, 1, 1, 1).setWidth(contentWidth());

        table.addHeaderCell(Cells.createHeader("Trial / Cohort"));
        table.addHeaderCell(Cells.createHeader("# Criteria"));
        table.addHeaderCell(Cells.createHeader("# Pass"));
        table.addHeaderCell(Cells.createHeader("# Warn"));
        table.addHeaderCell(Cells.createHeader("# Fail"));
        table.addHeaderCell(Cells.createHeader("# Undet."));
        table.addHeaderCell(Cells.createHeader("# No eval"));

        for (TrialEligibility trial : report.treatmentMatch().trialMatches()) {
            table.addCell(Cells.createContent(EligibilityDisplay.trialName(trial.identification())));
            addSummaryToTable(table, EvaluationSummarizer.summarize(trial.evaluations().values()));
            for (CohortEligibility cohort : trial.cohorts()) {
                table.addCell(Cells.createContent(EligibilityDisplay.cohortName(trial.identification(), cohort.metadata())));
                addSummaryToTable(table, EvaluationSummarizer.summarize(cohort.evaluations().values()));
            }
        }

        document.add(Tables.makeWrapping(table, "Evaluation results per trial & cohort"));
    }

    private static void addSummaryToTable(@NotNull Table table, @NotNull EvaluationSummary summary) {
        table.addCell(Cells.createContent(String.valueOf(summary.count())));
        table.addCell(Cells.createContent(String.valueOf(summary.passedCount())));
        table.addCell(Cells.createContent(String.valueOf(summary.warningCount())));
        table.addCell(Cells.createContent(String.valueOf(summary.failedCount())));
        table.addCell(Cells.createContent(String.valueOf(summary.undeterminedCount())));
        table.addCell(Cells.createContent(String.valueOf(summary.notEvaluatedCount())));
    }
}
