package com.hartwig.actin.report.pdf.chapters;

import java.time.LocalDate;
import java.util.StringJoiner;

import com.hartwig.actin.datamodel.ActinRecord;
import com.hartwig.actin.datamodel.clinical.CancerRelatedComplication;
import com.hartwig.actin.datamodel.clinical.ClinicalRecord;
import com.hartwig.actin.datamodel.clinical.PriorOtherCondition;
import com.hartwig.actin.datamodel.clinical.PriorSecondPrimary;
import com.hartwig.actin.datamodel.clinical.PriorTumorTreatment;
import com.hartwig.actin.datamodel.clinical.Toxicity;
import com.hartwig.actin.datamodel.clinical.ToxicitySource;
import com.hartwig.actin.datamodel.molecular.MolecularRecord;
import com.hartwig.actin.report.pdf.util.Cells;
import com.hartwig.actin.report.pdf.util.Formats;
import com.hartwig.actin.report.pdf.util.Styles;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.element.Text;
import com.itextpdf.layout.property.TextAlignment;
import com.itextpdf.layout.property.UnitValue;

import org.apache.logging.log4j.util.Strings;
import org.jetbrains.annotations.NotNull;

public class SummaryChapter implements ReportChapter {

    @NotNull
    private final ActinRecord record;

    public SummaryChapter(@NotNull final ActinRecord record) {
        this.record = record;
    }

    @NotNull
    @Override
    public String name() {
        return "Summary";
    }

    @NotNull
    @Override
    public PageSize pageSize() {
        return PageSize.A4;
    }

    @Override
    public void render(@NotNull Document document) {
        addPatientDetails(document);
        addChapterTitle(document);
        addSummaryTable(document);
    }

    private void addPatientDetails(@NotNull Document document) {
        Paragraph patientDetailsLine = new Paragraph();
        patientDetailsLine.add(new Text("Sample ID: ").addStyle(Styles.labelStyle()));
        patientDetailsLine.add(new Text(record.sampleId()).addStyle(Styles.valueStyle()));
        patientDetailsLine.add(new Text(" | Gender: ").addStyle(Styles.labelStyle()));
        patientDetailsLine.add(new Text(record.clinical().patient().gender().display()).addStyle(Styles.valueStyle()));
        patientDetailsLine.add(new Text(" | Birth year: ").addStyle(Styles.labelStyle()));
        patientDetailsLine.add(new Text(String.valueOf(record.clinical().patient().birthYear())).addStyle(Styles.valueStyle()));

        document.add(patientDetailsLine.setWidth(contentWidth()).setTextAlignment(TextAlignment.RIGHT));
    }

    private void addChapterTitle(@NotNull Document document) {
        document.add(new Paragraph(name()).addStyle(Styles.chapterTitleStyle()));
    }

    private void addSummaryTable(@NotNull Document document) {
        Table table = new Table(UnitValue.createPercentArray(new float[] { 1 })).setWidth(contentWidth());
        float[] subTableWidths = new float[] { 170, table.getWidth().getValue() - 180 };

        String questionnaireDate = questionnaireDate(record.clinical());
        table.addCell(Cells.createTitleCell("Patient clinical history (" + questionnaireDate + ")"));
        table.addCell(Cells.createCell(createPatientClinicalHistoryTable(record.clinical(), subTableWidths)));
        table.addCell(Cells.createEmptyCell());

        table.addCell(Cells.createTitleCell("Patient current details (" + questionnaireDate + ")"));
        table.addCell(Cells.createCell(createPatientCurrentDetailsTable(record.clinical(), subTableWidths)));
        table.addCell(Cells.createEmptyCell());

        table.addCell(Cells.createTitleCell("Molecular results"));
        table.addCell(Cells.createCell(createMolecularResultsTable(record.molecular(), subTableWidths)));

        document.add(table);
    }

    @NotNull
    private static String questionnaireDate(@NotNull ClinicalRecord record) {
        LocalDate questionnaireDate = record.patient().questionnaireDate();
        return questionnaireDate != null ? Formats.date(questionnaireDate) : "Unknown";
    }

    @NotNull
    private static Table createPatientClinicalHistoryTable(@NotNull ClinicalRecord record, @NotNull float[] widths) {
        Table table = new Table(UnitValue.createPointArray(widths));
        table.addCell(Cells.createKeyCell("Relevant systemic treatment history"));
        table.addCell(Cells.createValueCell(relevantSystemicPreTreatmentHistory(record)));
        table.addCell(Cells.createKeyCell("Other oncological history"));
        table.addCell(Cells.createValueCell(otherOncologicalHistory(record)));
        table.addCell(Cells.createKeyCell("Relevant non-oncological history"));
        table.addCell(Cells.createValueCell(relevantNonOncologicalHistory(record)));
        return table;
    }

    @NotNull
    private static String relevantSystemicPreTreatmentHistory(@NotNull ClinicalRecord record) {
        StringJoiner joiner = Formats.stringJoiner();
        for (PriorTumorTreatment priorTumorTreatment : record.priorTumorTreatments()) {
            if (priorTumorTreatment.isSystemic()) {
                joiner.add(priorTumorTreatment.name());
            }
        }
        return valueOrDefault(joiner.toString(), "None");
    }

    @NotNull
    private static String otherOncologicalHistory(@NotNull ClinicalRecord record) {
        StringJoiner otherOncologyHistories = Formats.stringJoiner();
        for (PriorTumorTreatment priorTumorTreatment : record.priorTumorTreatments()) {
            if (!priorTumorTreatment.isSystemic()) {
                otherOncologyHistories.add(priorTumorTreatment.name());
            }
        }

        StringJoiner secondPrimaries = Formats.stringJoiner();
        for (PriorSecondPrimary priorSecondPrimary : record.priorSecondPrimaries()) {
            String secondPrimaryString = priorSecondPrimary.tumorLocation();
            if (priorSecondPrimary.diagnosedYear() != null) {
                secondPrimaryString = secondPrimaryString + " (" + priorSecondPrimary.diagnosedYear() + ")";
            }
            secondPrimaries.add(secondPrimaryString);
        }

        if (record.priorSecondPrimaries().size() > 1) {
            otherOncologyHistories.add("Previous primary tumors: " + secondPrimaries);
        } else if (!record.priorSecondPrimaries().isEmpty()) {
            otherOncologyHistories.add("Previous primary tumor: " + secondPrimaries);
        }

        return valueOrDefault(otherOncologyHistories.toString(), "None");
    }

    @NotNull
    private static String relevantNonOncologicalHistory(@NotNull ClinicalRecord record) {
        StringJoiner joiner = Formats.stringJoiner();
        for (PriorOtherCondition priorOtherCondition : record.priorOtherConditions()) {
            joiner.add(priorOtherCondition.name());
        }
        return valueOrDefault(joiner.toString(), "None");
    }

    @NotNull
    private static Table createPatientCurrentDetailsTable(@NotNull ClinicalRecord record, @NotNull float[] subTableWidths) {
        Table table = new Table(UnitValue.createPointArray(subTableWidths));
        table.addCell(Cells.createKeyCell("WHO status"));
        table.addCell(Cells.createValueCell(String.valueOf(record.clinicalStatus().who())));

        table.addCell(Cells.createKeyCell("Unresolved toxicities grade => 2"));
        table.addCell(Cells.createValueCell(unresolvedToxicities(record)));

        table.addCell(Cells.createKeyCell("Significant infection"));
        table.addCell(Cells.createValueCell(Formats.yesNoUnknown(record.clinicalStatus().hasActiveInfection())));

        Boolean hasAberration = record.clinicalStatus().hasSigAberrationLatestEcg();
        if (hasAberration != null && hasAberration) {
            table.addCell(Cells.createKeyCell("Significant aberration on latest ECG"));
            String ecg = record.clinicalStatus().ecgAberrationDescription();
            table.addCell(Cells.createValueCell(ecg != null ? ecg : Strings.EMPTY));
        }

        table.addCell(Cells.createKeyCell("Cancer-related complications"));
        table.addCell(Cells.createValueCell(cancerRelatedComplications(record)));

        return table;
    }

    @NotNull
    private static String unresolvedToxicities(@NotNull ClinicalRecord record) {
        StringJoiner joiner = Formats.stringJoiner();
        for (Toxicity toxicity : record.toxicities()) {
            Integer grade = toxicity.grade();
            if ((grade != null && grade >= 2) || toxicity.source() == ToxicitySource.QUESTIONNAIRE) {
                String gradeString = grade != null ? " (" + grade + ")" : Strings.EMPTY;
                joiner.add(toxicity.name() + gradeString);
            }
        }
        return valueOrDefault(joiner.toString(), "None");
    }

    @NotNull
    private static String cancerRelatedComplications(@NotNull ClinicalRecord record) {
        StringJoiner joiner = Formats.stringJoiner();
        for (CancerRelatedComplication complication : record.cancerRelatedComplications()) {
            joiner.add(complication.name());
        }
        return valueOrDefault(joiner.toString(), "No");
    }

    @NotNull
    private static Table createMolecularResultsTable(@NotNull MolecularRecord record, @NotNull float[] widths) {
        Table table = new Table(UnitValue.createPointArray(widths));

        table.addCell(Cells.createKeyCell("Molecular results have reliable quality"));
        table.addCell(Cells.createValueCell(Formats.yesNoUnknown(record.hasReliableQuality())));

        table.addCell(Cells.createKeyCell("Tumor sample has reliable and sufficient purity"));
        table.addCell(Cells.createValueCell(Formats.yesNoUnknown(record.hasReliablePurity())));

        table.addCell(Cells.createKeyCell("Actionable molecular events"));
        StringJoiner joiner = Formats.stringJoiner();
        for (String string : record.actionableGenomicEvents()) {
            joiner.add(string);
        }
        table.addCell(Cells.createValueCell(valueOrDefault(joiner.toString(), "None")));
        return table;
    }

    @NotNull
    private static String valueOrDefault(@NotNull String value, @NotNull String defaultString) {
        return !value.isEmpty() ? value : defaultString;
    }
}
