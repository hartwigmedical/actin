package com.hartwig.actin.report.pdf.chapters;

import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.hartwig.actin.algo.datamodel.CohortEligibility;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.TrialEligibility;
import com.hartwig.actin.report.datamodel.Report;
import com.hartwig.actin.report.pdf.util.Cells;
import com.hartwig.actin.report.pdf.util.Formats;
import com.hartwig.actin.report.pdf.util.Styles;
import com.hartwig.actin.report.pdf.util.Tables;
import com.hartwig.actin.treatment.datamodel.CohortMetadata;
import com.hartwig.actin.treatment.datamodel.CriterionReference;
import com.hartwig.actin.treatment.datamodel.Eligibility;
import com.hartwig.actin.treatment.datamodel.TrialIdentification;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.AreaBreak;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.property.AreaBreakType;

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
    public void render(@NotNull Document document) {
        addChapterTitle(document);

        List<TrialEligibility> eligible = Lists.newArrayList();
        List<TrialEligibility> nonEligible = Lists.newArrayList();
        for (TrialEligibility trial : report.treatmentMatch().trialMatches()) {
            if (isEligible(trial)) {
                eligible.add(trial);
            } else {
                nonEligible.add(trial);
            }
        }

        if (!eligible.isEmpty()) {
            addTrialMatches(document, eligible, "Potentially eligible trials & cohorts", true);
            document.add(new AreaBreak(AreaBreakType.NEXT_PAGE));
        }

        if (!nonEligible.isEmpty()) {
            addTrialMatches(document, nonEligible, "Other trials & cohorts", false);
        }
    }

    private static boolean isEligible(@NotNull TrialEligibility trial) {
        if (trial.overallEvaluation().isPass()) {
            // Either a trial has no cohorts, or at least one cohort has to pass.
            if (trial.cohorts().isEmpty()) {
                return true;
            } else {
                for (CohortEligibility cohort : trial.cohorts()) {
                    if (cohort.overallEvaluation().isPass()) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    private void addChapterTitle(@NotNull Document document) {
        document.add(new Paragraph(name()).addStyle(Styles.chapterTitleStyle()));
    }

    private void addTrialMatches(@NotNull Document document, @NotNull List<TrialEligibility> trials, @NotNull String title,
            boolean trialsAreEligible) {
        document.add(new Paragraph(title).addStyle(Styles.tableTitleStyle()));

        boolean addBlank = false;
        for (TrialEligibility trial : trials) {
            if (addBlank) {
                if (trialsAreEligible) {
                    document.add(new AreaBreak(AreaBreakType.NEXT_PAGE));
                } else {
                    document.add(blankLine());
                }
            }
            addTrialDetails(document, trial);
            addBlank = true;
        }
    }

    private void addTrialDetails(@NotNull Document document, @NotNull TrialEligibility trial) {
        boolean displayFailOnly = !isEligible(trial);
        document.add(createTrialIdentificationTable(trial.identification(), trial.overallEvaluation()));
        document.add(blankLine());
        Map<Eligibility, Evaluation> trialEvaluations = removeEligibilityWithoutReference(trial.evaluations());
        document.add(Tables.makeWrapping(createEvaluationTable(trialEvaluations, displayFailOnly)));

        for (CohortEligibility cohort : trial.cohorts()) {
            document.add(blankLine());
            document.add(createCohortIdentificationTable(trial.identification().trialId(), cohort.metadata(), cohort.overallEvaluation()));
            Map<Eligibility, Evaluation> cohortEvaluations = removeEligibilityWithoutReference(cohort.evaluations());
            if (!cohortEvaluations.isEmpty()) {
                document.add(blankLine());
                document.add(Tables.makeWrapping(createEvaluationTable(cohortEvaluations, displayFailOnly)));
            }
        }
    }

    @NotNull
    private static Map<Eligibility, Evaluation> removeEligibilityWithoutReference(@NotNull Map<Eligibility, Evaluation> evaluations) {
        Map<Eligibility, Evaluation> filtered = Maps.newHashMap();
        for (Map.Entry<Eligibility, Evaluation> entry : evaluations.entrySet()) {
            if (!entry.getKey().references().isEmpty()) {
                filtered.put(entry.getKey(), entry.getValue());
            }
        }
        return filtered;
    }

    @NotNull
    private Table createTrialIdentificationTable(@NotNull TrialIdentification identification, @NotNull Evaluation overallEvaluation) {
        float indentWidth = 10;
        float keyWidth = 70;
        float valueWidth = contentWidth() - (keyWidth + indentWidth + 10);

        Table table = Tables.createFixedWidthCols(indentWidth, keyWidth, valueWidth).setWidth(contentWidth());

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
        float indentWidth = 10;
        float keyWidth = 210;
        float valueWidth = contentWidth() - (keyWidth + indentWidth + 10);

        Table table = Tables.createFixedWidthCols(indentWidth, keyWidth, valueWidth).setWidth(contentWidth());

        table.addCell(Cells.createSpanningTitle(trialId + " - " + metadata.description(), table));

        table.addCell(Cells.createEmpty());
        table.addCell(Cells.createKey("Cohort ID"));
        table.addCell(Cells.createValue(metadata.cohortId()));

        table.addCell(Cells.createEmpty());
        table.addCell(Cells.createKey("Evaluation"));
        table.addCell(Cells.createValue(overallEvaluation));

        table.addCell(Cells.createEmpty());
        table.addCell(Cells.createKey("Open for inclusion?"));
        table.addCell(Cells.createValue(Formats.yesNoUnknown(metadata.open())));

        return table;
    }

    @NotNull
    private Table createEvaluationTable(@NotNull Map<Eligibility, Evaluation> evaluations, boolean displayFailOnly) {
        float ruleWidth = 30;
        float evaluationWidth = 100;
        float referenceWidth = contentWidth() - (ruleWidth + evaluationWidth + 10);
        Table table = Tables.createFixedWidthCols(ruleWidth, referenceWidth, evaluationWidth).setWidth(contentWidth());

        table.addHeaderCell(Cells.createHeader("Rule"));
        table.addHeaderCell(Cells.createHeader("Reference"));
        table.addHeaderCell(Cells.createHeader("Evaluation"));

        addEvaluationsOfType(table, evaluations, Evaluation.FAIL);
        if (!displayFailOnly) {
            addEvaluationsOfType(table, evaluations, Evaluation.UNDETERMINED);
            addEvaluationsOfType(table, evaluations, Evaluation.NOT_IMPLEMENTED);
            addEvaluationsOfType(table, evaluations, Evaluation.PASS_BUT_WARN);
            addEvaluationsOfType(table, evaluations, Evaluation.NOT_EVALUATED);
            addEvaluationsOfType(table, evaluations, Evaluation.PASS);
        }

        return table;
    }

    private static void addEvaluationsOfType(@NotNull Table table, @NotNull Map<Eligibility, Evaluation> evaluations,
            @NotNull Evaluation evaluationToRender) {
        for (Map.Entry<Eligibility, Evaluation> entry : evaluations.entrySet()) {
            Evaluation evaluation = entry.getValue();
            if (evaluation == evaluationToRender) {
                boolean hasAddedEvaluation = false;
                for (CriterionReference reference : entry.getKey().references()) {
                    table.addCell(Cells.createContent(reference.id()));
                    table.addCell(Cells.createContent(reference.text()));
                    if (!hasAddedEvaluation) {
                        table.addCell(Cells.createContent(evaluation));
                        hasAddedEvaluation = true;
                    } else {
                        table.addCell(Cells.createContent(Strings.EMPTY));
                    }
                }
            }
        }
    }

    @NotNull
    private static Paragraph blankLine() {
        return new Paragraph(" ");
    }
}
