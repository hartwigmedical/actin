package com.hartwig.actin.report.pdf.chapters;

import java.util.Map;

import com.hartwig.actin.algo.datamodel.CohortEligibility;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.TrialEligibility;
import com.hartwig.actin.report.datamodel.Report;
import com.hartwig.actin.report.pdf.util.Cells;
import com.hartwig.actin.report.pdf.util.Styles;
import com.hartwig.actin.report.pdf.util.Tables;
import com.hartwig.actin.treatment.datamodel.CohortMetadata;
import com.hartwig.actin.treatment.datamodel.CriterionReference;
import com.hartwig.actin.treatment.datamodel.Eligibility;
import com.hartwig.actin.treatment.datamodel.TrialIdentification;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;

import org.apache.logging.log4j.util.Strings;
import org.jetbrains.annotations.NotNull;

public class TrialMatchingDetailsChapter implements ReportChapter {

    @NotNull
    private final Report report;

    public TrialMatchingDetailsChapter(@NotNull final Report report) {
        this.report = report;
    }

    @NotNull
    @Override
    public String name() {
        return "Trial Matching Details";
    }

    @NotNull
    @Override
    public PageSize pageSize() {
        return PageSize.A4;
    }

    @Override
    public void render(@NotNull final Document document) {
        addChapterTitle(document);

        for (TrialEligibility trial : report.treatmentMatch().trialMatches()) {
            addTrialDetails(document, trial);
        }
    }

    private void addChapterTitle(@NotNull Document document) {
        document.add(new Paragraph(name()).addStyle(Styles.chapterTitleStyle()));
    }

    private void addTrialDetails(@NotNull Document document, @NotNull TrialEligibility trial) {
        String trialIdentification = trialIdentificationString(trial.identification());
        Table trialTable = Tables.addTitle(createEvaluationTable(trial.evaluations()), trialIdentification + " - General");
        document.add(Tables.makeWrapping(trialTable));

        for (CohortEligibility cohort : trial.cohorts()) {
            String cohortIdentification = cohortIdentificationString(cohort.metadata());
            Table cohortTable = new Table(1).setMinWidth(contentWidth());

            cohortTable.addCell(Cells.createHeader(trialIdentification + " - " + cohortIdentification));
            cohortTable.addCell(Cells.create(createEvaluationTable(cohort.evaluations())));

            document.add(Tables.makeWrapping(cohortTable));
        }
    }

    @NotNull
    private Table createEvaluationTable(@NotNull Map<Eligibility, Evaluation> evaluations) {
        Table table = Tables.createFixedWidthCols(new float[] { 1, 4, 1 }).setWidth(contentWidth());

        table.addHeaderCell(Cells.createHeader("Rule"));
        table.addHeaderCell(Cells.createHeader("Text"));
        table.addHeaderCell(Cells.createHeader("Evaluation"));

        for (Map.Entry<Eligibility, Evaluation> entry : evaluations.entrySet()) {
            boolean hasAddedEvaluation = false;
            for (CriterionReference reference : entry.getKey().references()) {
                table.addCell(Cells.createContent(reference.id()));
                table.addCell(Cells.createContent(reference.text()));
                if (!hasAddedEvaluation) {
                    table.addCell(Cells.createContent(entry.getValue().toString()));
                    hasAddedEvaluation = true;
                } else {
                    table.addCell(Cells.createContent(Strings.EMPTY));
                }
            }
        }
        return table;
    }

    @NotNull
    private static String trialIdentificationString(@NotNull TrialIdentification identification) {
        return identification.trialId() + " (" + identification.acronym() + ")";
    }

    @NotNull
    private static String cohortIdentificationString(@NotNull CohortMetadata metadata) {
        String open = metadata.open() ? "Open" : "Closed";
        return metadata.cohortId() + " (" + metadata.description() + " - " + open + ")";
    }
}
