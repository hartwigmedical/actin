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

        document.add(new Paragraph(name()).addStyle(Styles.chapterTitleStyle()));

        addClinicalOverviewTable(document);
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

    private void addClinicalOverviewTable(@NotNull Document document) {
        Table table = new Table(UnitValue.createPercentArray(new float[] { 1 })).setWidth(contentWidth());
        float[] subTableWidths = new float[] { 170, table.getWidth().getValue() - 180 };

        String questionnaireDate = questionnaireDate(record.clinical());
        table.addCell(Cells.createTitleCell("Patient clinical history (" + questionnaireDate + ")"));
        table.addCell(Cells.createCell(createClinicalHistoryTable(record.clinical(), subTableWidths)));

        table.addCell(Cells.createEmptyCell());

        table.addCell(Cells.createTitleCell("Patient current details (" + questionnaireDate + ")"));
        table.addCell(Cells.createCell(createCurrentDetailsTable(record.clinical(), subTableWidths)));

        document.add(table);
    }

    @NotNull
    private static String questionnaireDate(@NotNull ClinicalRecord record) {
        LocalDate questionnaireDate = record.patient().questionnaireDate();
        return questionnaireDate != null ? Formats.date(questionnaireDate) : "Unknown";
    }

    @NotNull
    private static Table createClinicalHistoryTable(@NotNull ClinicalRecord record, @NotNull float[] widths) {
        Table table = new Table(UnitValue.createPointArray(widths));
        table.addCell(Cells.createKeyCell("Relevant treatment history"));
        table.addCell(Cells.createValueCell(relevantPreTreatmentHistory(record)));
        table.addCell(Cells.createKeyCell("Other oncological history"));
        table.addCell(Cells.createValueCell(otherOncologicalHistory(record)));
        table.addCell(Cells.createKeyCell("Relevant non-oncological history"));
        table.addCell(Cells.createValueCell(relevantNonOncologicalHistory(record)));
        return table;
    }

    @NotNull
    private static String relevantPreTreatmentHistory(@NotNull ClinicalRecord record) {
        StringJoiner joiner = new StringJoiner(", ");
        for (PriorTumorTreatment priorTumorTreatment : record.priorTumorTreatments()) {
            if (priorTumorTreatment.isSystemic()) {
                joiner.add(priorTumorTreatment.name());
            }
        }
        return valueOrDefault(joiner.toString(), "None");
    }

    @NotNull
    private static String otherOncologicalHistory(@NotNull ClinicalRecord record) {
        StringJoiner joiner = new StringJoiner(", ");
        for (PriorTumorTreatment priorTumorTreatment : record.priorTumorTreatments()) {
            if (!priorTumorTreatment.isSystemic()) {
                joiner.add(priorTumorTreatment.name());
            }
        }

        for (PriorSecondPrimary priorSecondPrimary : record.priorSecondPrimaries()) {
            joiner.add("Previous primary tumor: " + priorSecondPrimary.tumorLocation());
        }

        return valueOrDefault(joiner.toString(), "None");
    }

    @NotNull
    private static String relevantNonOncologicalHistory(@NotNull ClinicalRecord record) {
        StringJoiner joiner = new StringJoiner(", ");
        for (PriorOtherCondition priorOtherCondition : record.priorOtherConditions()) {
            joiner.add(priorOtherCondition.name());
        }
        return valueOrDefault(joiner.toString(), "None");
    }

    @NotNull
    private static Table createCurrentDetailsTable(@NotNull ClinicalRecord record, @NotNull float[] subTableWidths) {
        Table table = new Table(UnitValue.createPointArray(subTableWidths));
        table.addCell(Cells.createKeyCell("WHO status"));
        table.addCell(Cells.createValueCell(String.valueOf(record.clinicalStatus().who())));

        table.addCell(Cells.createKeyCell("Unresolved toxicities grade => 2"));
        table.addCell(Cells.createValueCell(unresolvedToxicities(record)));

        table.addCell(Cells.createKeyCell("Significant infection"));
        Boolean hasActiveInfection = record.clinicalStatus().hasActiveInfection();
        String hasActiveInfectionString = hasActiveInfection != null ? String.valueOf(hasActiveInfection) : "Unknown";
        table.addCell(Cells.createValueCell(hasActiveInfectionString));

        table.addCell(Cells.createKeyCell("Significant aberration on latest ECG"));
        String ecg = record.clinicalStatus().ecgAberrationDescription();
        String ecgString = ecg != null ? ecg : Strings.EMPTY;
        table.addCell(Cells.createValueCell(ecgString));

        table.addCell(Cells.createKeyCell("Cancer-related complications"));
        table.addCell(Cells.createValueCell(cancerRelatedComplications(record)));

        return table;
    }

    @NotNull
    private static String unresolvedToxicities(@NotNull ClinicalRecord record) {
        StringJoiner joiner = new StringJoiner(", ");
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
        StringJoiner joiner = new StringJoiner(", ");
        for (CancerRelatedComplication complication : record.cancerRelatedComplications()) {
            joiner.add(complication.name());
        }
        return valueOrDefault(joiner.toString(), "No");
    }

    @NotNull
    private static String valueOrDefault(@NotNull String value, @NotNull String defaultString) {
        return !value.isEmpty() ? value : defaultString;
    }
}
