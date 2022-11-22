package com.hartwig.actin.report.pdf.chapters;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.hartwig.actin.algo.datamodel.CohortMatch;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.datamodel.ImmutableEvaluation;
import com.hartwig.actin.algo.datamodel.TrialMatch;
import com.hartwig.actin.report.datamodel.Report;
import com.hartwig.actin.report.pdf.util.Cells;
import com.hartwig.actin.report.pdf.util.Formats;
import com.hartwig.actin.report.pdf.util.Styles;
import com.hartwig.actin.report.pdf.util.Tables;
import com.hartwig.actin.treatment.datamodel.CohortMetadata;
import com.hartwig.actin.treatment.datamodel.CriterionReference;
import com.hartwig.actin.treatment.datamodel.Eligibility;
import com.hartwig.actin.treatment.datamodel.TrialIdentification;
import com.hartwig.actin.treatment.sort.CriterionReferenceComparator;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.AreaBreak;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.AreaBreakType;

import org.jetbrains.annotations.NotNull;

public class TrialMatchingDetailsChapter implements ReportChapter {

    private static final float RULE_COL_WIDTH = 30;
    private static final float EVALUATION_COL_WIDTH = 150;

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

        List<TrialMatch> eligible = Lists.newArrayList();
        List<TrialMatch> nonEligible = Lists.newArrayList();
        for (TrialMatch trial : report.treatmentMatch().trialMatches()) {
            if (isOpenAndPotentiallyEligible(trial)) {
                eligible.add(trial);
            } else {
                nonEligible.add(trial);
            }
        }

        if (!eligible.isEmpty()) {
            addTrialMatches(document, eligible, "Potentially eligible open trials & cohorts", true);
        }

        if (!nonEligible.isEmpty()) {
            if (!eligible.isEmpty()) {
                document.add(pageBreak());
            }
            addTrialMatches(document, nonEligible, "Other trials & cohorts", false);
        }
    }

    private static boolean isOpenAndPotentiallyEligible(@NotNull TrialMatch trial) {
        if (!trial.isPotentiallyEligible() || !trial.identification().open()) {
            return false;
        }

        if (trial.cohorts().isEmpty()) {
            return true;
        }

        for (CohortMatch cohort : trial.cohorts()) {
            if (cohort.isPotentiallyEligible() && !cohort.metadata().blacklist() && cohort.metadata().open()) {
                return true;
            }
        }
        return false;
    }

    private void addChapterTitle(@NotNull Document document) {
        document.add(new Paragraph(name()).addStyle(Styles.chapterTitleStyle()));
    }

    private void addTrialMatches(@NotNull Document document, @NotNull List<TrialMatch> trials, @NotNull String title,
            boolean trialsAreEligible) {
        document.add(new Paragraph(title).addStyle(Styles.tableTitleStyle()));

        boolean addBlank = false;
        for (TrialMatch trial : trials) {
            if (addBlank) {
                if (trialsAreEligible) {
                    document.add(pageBreak());
                } else {
                    document.add(blankLine());
                }
            }
            addTrialDetails(document, trial);
            addBlank = true;
        }
    }

    private void addTrialDetails(@NotNull Document document, @NotNull TrialMatch trial) {
        boolean displayFailOnly = !trial.isPotentiallyEligible();
        document.add(createTrialIdentificationTable(trial.identification(), trial.isPotentiallyEligible()));
        document.add(blankLine());

        Map<CriterionReference, Evaluation> trialEvaluationPerCriterion = toWorstEvaluationPerReference(trial.evaluations());
        if (hasDisplayableEvaluations(trialEvaluationPerCriterion, displayFailOnly)) {
            document.add(Tables.makeWrapping(createEvaluationTable(trialEvaluationPerCriterion, displayFailOnly)));
        }

        for (CohortMatch cohort : trial.cohorts()) {
            document.add(blankLine());
            document.add(createCohortIdentificationTable(trial.identification().trialId(),
                    cohort.metadata(),
                    cohort.isPotentiallyEligible()));

            Map<CriterionReference, Evaluation> cohortEvaluationPerCriterion = toWorstEvaluationPerReference(cohort.evaluations());
            if (hasDisplayableEvaluations(cohortEvaluationPerCriterion, displayFailOnly)) {
                document.add(blankLine());
                document.add(Tables.makeWrapping(createEvaluationTable(cohortEvaluationPerCriterion, displayFailOnly)));
            }
        }
    }

    private static boolean hasDisplayableEvaluations(@NotNull Map<CriterionReference, Evaluation> evaluationsPerCriterion,
            boolean displayFailOnly) {
        if (!displayFailOnly) {
            return !evaluationsPerCriterion.isEmpty();
        }

        for (Evaluation evaluation : evaluationsPerCriterion.values()) {
            if (evaluation.result() == EvaluationResult.FAIL) {
                return true;
            }
        }

        return false;
    }

    @NotNull
    private static Map<CriterionReference, Evaluation> toWorstEvaluationPerReference(@NotNull Map<Eligibility, Evaluation> evaluations) {
        Map<CriterionReference, EvaluationResult> worstResultPerCriterion = Maps.newHashMap();
        for (Map.Entry<Eligibility, Evaluation> entry : evaluations.entrySet()) {
            for (CriterionReference reference : entry.getKey().references()) {
                EvaluationResult currentWorst = worstResultPerCriterion.get(reference);
                EvaluationResult evaluation = entry.getValue().result();
                if (currentWorst != null) {
                    EvaluationResult newWorst = currentWorst.isWorseThan(evaluation) ? currentWorst : evaluation;
                    worstResultPerCriterion.put(reference, newWorst);
                } else {
                    worstResultPerCriterion.put(reference, evaluation);
                }
            }
        }

        Map<CriterionReference, Evaluation> worstEvaluationPerCriterion = Maps.newHashMap();
        for (Map.Entry<Eligibility, Evaluation> entry : evaluations.entrySet()) {
            Evaluation evaluation = entry.getValue();
            for (CriterionReference reference : entry.getKey().references()) {
                EvaluationResult worst = worstResultPerCriterion.get(reference);

                Evaluation current = worstEvaluationPerCriterion.get(reference);
                if (current == null) {
                    worstEvaluationPerCriterion.put(reference, ImmutableEvaluation.builder().from(evaluation).result(worst).build());
                } else {
                    ImmutableEvaluation.Builder updatedBuilder = ImmutableEvaluation.builder()
                            .from(current)
                            .addAllPassSpecificMessages(evaluation.passSpecificMessages())
                            .addAllPassGeneralMessages(evaluation.passGeneralMessages())
                            .addAllWarnSpecificMessages(evaluation.warnSpecificMessages())
                            .addAllWarnGeneralMessages(evaluation.warnGeneralMessages())
                            .addAllUndeterminedSpecificMessages(evaluation.undeterminedSpecificMessages())
                            .addAllUndeterminedGeneralMessages(evaluation.undeterminedGeneralMessages())
                            .addAllFailSpecificMessages(evaluation.failSpecificMessages())
                            .addAllFailGeneralMessages(evaluation.failGeneralMessages());

                    if (evaluation.result() == worst) {
                        updatedBuilder.recoverable(current.recoverable() && evaluation.recoverable());
                    }

                    worstEvaluationPerCriterion.put(reference, updatedBuilder.build());
                }
            }
        }

        return worstEvaluationPerCriterion;
    }

    @NotNull
    private Table createTrialIdentificationTable(@NotNull TrialIdentification identification, boolean isPotentiallyEligible) {
        float indentWidth = 10;
        float keyWidth = 90;
        float valueWidth = contentWidth() - (keyWidth + indentWidth + 10);

        Table table = Tables.createFixedWidthCols(indentWidth, keyWidth, valueWidth).setWidth(contentWidth()).setKeepTogether(true);

        table.addCell(Cells.createSpanningTitle(identification.trialId(), table));

        table.addCell(Cells.createEmpty());
        table.addCell(Cells.createKey("Potentially eligible"));
        table.addCell(Cells.createValueYesNo(Formats.yesNoUnknown(isPotentiallyEligible)));

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
            boolean isPotentiallyEligible) {
        float indentWidth = 10;
        float keyWidth = 90;
        float valueWidth = contentWidth() - (keyWidth + indentWidth + 10);

        Table table = Tables.createFixedWidthCols(indentWidth, keyWidth, valueWidth).setWidth(contentWidth()).setKeepTogether(true);

        table.addCell(Cells.createSpanningTitle(trialId + " - " + metadata.description(), table));

        table.addCell(Cells.createEmpty());
        table.addCell(Cells.createKey("Cohort ID"));
        table.addCell(Cells.createValue(metadata.cohortId()));

        table.addCell(Cells.createEmpty());
        table.addCell(Cells.createKey("Potentially eligible?"));
        table.addCell(Cells.createValueYesNo(Formats.yesNoUnknown(isPotentiallyEligible)));

        table.addCell(Cells.createEmpty());
        table.addCell(Cells.createKey("Open for inclusion?"));
        table.addCell(Cells.createValue(Formats.yesNoUnknown(metadata.open())));

        table.addCell(Cells.createEmpty());
        table.addCell(Cells.createKey("Has slots available?"));
        table.addCell(Cells.createValue(Formats.yesNoUnknown(metadata.slotsAvailable())));

        if (metadata.blacklist()) {
            table.addCell(Cells.createEmpty());
            table.addCell(Cells.createKey("Blacklisted for eligibility?"));
            table.addCell(Cells.createValue(Formats.yesNoUnknown(metadata.blacklist())));
        }

        return table;
    }

    @NotNull
    private Table createEvaluationTable(@NotNull Map<CriterionReference, Evaluation> evaluations, boolean displayFailOnly) {
        float referenceWidth = contentWidth() - (RULE_COL_WIDTH + EVALUATION_COL_WIDTH);
        Table table = Tables.createFixedWidthCols(RULE_COL_WIDTH, referenceWidth, EVALUATION_COL_WIDTH).setWidth(contentWidth());

        table.addHeaderCell(Cells.createHeader("Rule"));
        table.addHeaderCell(Cells.createHeader("Reference"));
        table.addHeaderCell(Cells.createHeader("Evaluation"));

        Set<CriterionReference> references = Sets.newTreeSet(new CriterionReferenceComparator());
        references.addAll(evaluations.keySet());

        addEvaluationsOfType(table, references, evaluations, EvaluationResult.NOT_IMPLEMENTED);
        addEvaluationsOfType(table, references, evaluations, EvaluationResult.FAIL);
        if (!displayFailOnly) {
            addEvaluationsOfType(table, references, evaluations, EvaluationResult.WARN);
            addEvaluationsOfType(table, references, evaluations, EvaluationResult.UNDETERMINED);
            addEvaluationsOfType(table, references, evaluations, EvaluationResult.PASS);
            addEvaluationsOfType(table, references, evaluations, EvaluationResult.NOT_EVALUATED);
        }

        return table;
    }

    private static void addEvaluationsOfType(@NotNull Table table, Set<CriterionReference> references,
            @NotNull Map<CriterionReference, Evaluation> evaluations, @NotNull EvaluationResult resultToRender) {
        for (CriterionReference reference : references) {
            Evaluation evaluation = evaluations.get(reference);
            if (evaluation.result() == resultToRender) {
                table.addCell(Cells.createContent(reference.id()));
                table.addCell(Cells.createContent(reference.text()));
                Table evalTable = Tables.createSingleColWithWidth(EVALUATION_COL_WIDTH).setKeepTogether(true);
                evalTable.addCell(Cells.createEvaluation(evaluation));
                if (evaluation.result() == EvaluationResult.PASS || evaluation.result() == EvaluationResult.NOT_EVALUATED) {
                    for (String passMessage : evaluation.passSpecificMessages()) {
                        evalTable.addCell(Cells.create(new Paragraph(passMessage)));
                    }
                } else if (evaluation.result() == EvaluationResult.WARN) {
                    for (String warnMessage : evaluation.warnSpecificMessages()) {
                        evalTable.addCell(Cells.create(new Paragraph(warnMessage)));
                    }

                    if (!evaluation.undeterminedSpecificMessages().isEmpty()) {
                        evalTable.addCell(Cells.createEvaluationResult(EvaluationResult.UNDETERMINED));
                        for (String undeterminedMessage : evaluation.undeterminedSpecificMessages()) {
                            evalTable.addCell(Cells.create(new Paragraph(undeterminedMessage)));
                        }
                    }
                } else if (evaluation.result() == EvaluationResult.UNDETERMINED) {
                    for (String undeterminedMessage : evaluation.undeterminedSpecificMessages()) {
                        evalTable.addCell(Cells.create(new Paragraph(undeterminedMessage)));
                    }
                } else if (evaluation.result() == EvaluationResult.FAIL) {
                    for (String failMessage : evaluation.failSpecificMessages()) {
                        evalTable.addCell(Cells.create(new Paragraph(failMessage)));
                    }
                    if (evaluation.recoverable()) {
                        if (!evaluation.warnSpecificMessages().isEmpty()) {
                            evalTable.addCell(Cells.createEvaluationResult(EvaluationResult.WARN));
                            for (String warnMessage : evaluation.warnSpecificMessages()) {
                                evalTable.addCell(Cells.create(new Paragraph(warnMessage)));
                            }
                        }
                        if (!evaluation.undeterminedSpecificMessages().isEmpty()) {
                            evalTable.addCell(Cells.createEvaluationResult(EvaluationResult.UNDETERMINED));
                            for (String undeterminedMessage : evaluation.undeterminedSpecificMessages()) {
                                evalTable.addCell(Cells.create(new Paragraph(undeterminedMessage)));
                            }
                        }
                    }
                }
                table.addCell(Cells.createContent(evalTable));
            }
        }
    }

    @NotNull
    private static Paragraph blankLine() {
        return new Paragraph(" ");
    }

    @NotNull
    private static AreaBreak pageBreak() {
        return new AreaBreak(AreaBreakType.NEXT_PAGE);
    }
}
