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
import com.hartwig.actin.treatment.util.EligibilityFunctionDisplay;
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
            document.add(blankLine());
            addTrialDetails(document, trial);
        }
    }

    private void addChapterTitle(@NotNull Document document) {
        document.add(new Paragraph(name()).addStyle(Styles.chapterTitleStyle()));
    }

    private void addTrialDetails(@NotNull Document document, @NotNull TrialEligibility trial) {
        document.add(createTrialIdentificationTable(trial.identification(), trial.overallEvaluation()));
        document.add(blankLine());
        document.add(Tables.makeWrapping(createEvaluationTable(trial.evaluations())));

        for (CohortEligibility cohort : trial.cohorts()) {
            document.add(blankLine());
            document.add(createCohortIdentificationTable(trial.identification().trialId(), cohort.metadata(), cohort.overallEvaluation()));
            if (!cohort.evaluations().isEmpty()) {
                document.add(blankLine());
                document.add(Tables.makeWrapping(createEvaluationTable(cohort.evaluations())));
            }
        }
    }

    @NotNull
    private Table createTrialIdentificationTable(@NotNull TrialIdentification identification, @NotNull Evaluation overallEvaluation) {
        Table table = Tables.createFixedWidthCols(new float[] { 1, 2, 3 });

        table.addCell(Cells.createSpanningTitle(identification.trialId(), table));

        table.addCell(Cells.createEmpty());
        table.addCell(Cells.createKey("Evaluation"));
        table.addCell(Cells.createValue(overallEvaluation));

        table.addCell(Cells.createEmpty());
        table.addCell(Cells.createKey("Acronym"));
        table.addCell(Cells.createValue(identification.acronym()));

        table.addCell(Cells.createEmpty());
        table.addCell(Cells.createKey("Title"));
        table.addCell(Cells.createValue(identification.title()));

        return table;
    }

    @NotNull
    private Table createCohortIdentificationTable(@NotNull String trialId, @NotNull CohortMetadata metadata,
            @NotNull Evaluation overallEvaluation) {
        Table table = Tables.createFixedWidthCols(new float[] { 1, 3, 3 });

        table.addCell(Cells.createSpanningTitle(trialId + " - " + metadata.description(), table));

        table.addCell(Cells.createEmpty());
        table.addCell(Cells.createKey("Cohort ID"));
        table.addCell(Cells.createValue(metadata.cohortId()));

        table.addCell(Cells.createEmpty());
        table.addCell(Cells.createKey("Evaluation"));
        table.addCell(Cells.createValue(overallEvaluation));

        table.addCell(Cells.createEmpty());
        table.addCell(Cells.createKey("Open for inclusion?"));
        table.addCell(Cells.createValue(metadata.open() ? "Yes" : "No"));

        return table;
    }

    @NotNull
    private Table createEvaluationTable(@NotNull Map<Eligibility, Evaluation> evaluations) {
        Table table = Tables.createFixedWidthCols(new float[] { 1, 4, 1 }).setWidth(contentWidth());

        table.addHeaderCell(Cells.createHeader("Rule"));
        table.addHeaderCell(Cells.createHeader("Implementation & Reference"));
        table.addHeaderCell(Cells.createHeader("Evaluation"));

        for (Map.Entry<Eligibility, Evaluation> entry : evaluations.entrySet()) {
            boolean hasAddedEvaluation = false;
            String implementation = EligibilityFunctionDisplay.format(entry.getKey().function());
            for (CriterionReference reference : entry.getKey().references()) {
                table.addCell(Cells.createContent(reference.id()));
                table.addCell(Cells.createContent(implementation + "\n" + reference.text()));
                if (!hasAddedEvaluation) {
                    table.addCell(Cells.createContent(entry.getValue()));
                    hasAddedEvaluation = true;
                } else {
                    table.addCell(Cells.createContent(Strings.EMPTY));
                }
            }
        }

        return table;
    }

    @NotNull
    private static Paragraph blankLine() {
        return new Paragraph(" ");
    }
}
