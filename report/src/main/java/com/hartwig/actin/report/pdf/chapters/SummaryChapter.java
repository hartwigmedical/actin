package com.hartwig.actin.report.pdf.chapters;

import java.util.StringJoiner;

import com.hartwig.actin.datamodel.ActinRecord;
import com.hartwig.actin.datamodel.clinical.CancerRelatedComplication;
import com.hartwig.actin.datamodel.clinical.ClinicalRecord;
import com.hartwig.actin.datamodel.clinical.PriorOtherCondition;
import com.hartwig.actin.datamodel.clinical.PriorSecondPrimary;
import com.hartwig.actin.datamodel.clinical.PriorTumorTreatment;
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
        addClinicalDates(document);

        document.add(new Paragraph(name()).addStyle(Styles.chapterTitleStyle()));

        addClinicalOverviewTable(document);
    }

    private void addPatientDetails(@NotNull Document document) {
        Paragraph patientDetailsLine = new Paragraph();
        patientDetailsLine.add(new Text("Sample ID: ").addStyle(Styles.labelStyle()));
        patientDetailsLine.add(new Text(record.sampleId()).addStyle(Styles.valueStyle()));
        patientDetailsLine.add(new Text(" | Sex: ").addStyle(Styles.labelStyle()));
        patientDetailsLine.add(new Text(record.clinical().patient().sex().display()).addStyle(Styles.valueStyle()));
        patientDetailsLine.add(new Text(" | Birth year: ").addStyle(Styles.labelStyle()));
        patientDetailsLine.add(new Text(String.valueOf(record.clinical().patient().birthYear())).addStyle(Styles.valueStyle()));

        document.add(patientDetailsLine.setWidth(contentWidth()).setTextAlignment(TextAlignment.RIGHT));
    }

    private void addClinicalDates(@NotNull Document document) {
        Paragraph clinicalDatesLine = new Paragraph();
        clinicalDatesLine.add(new Text("Clinical data: ").addStyle(Styles.labelStyle()));
        clinicalDatesLine.add(new Text("???").addStyle(Styles.valueStyle()));
        clinicalDatesLine.add(new Text(" until ").addStyle(Styles.labelStyle()));
        clinicalDatesLine.add(new Text("???").addStyle(Styles.valueStyle()));

        document.add(clinicalDatesLine.setWidth(contentWidth()).setTextAlignment(TextAlignment.RIGHT));
    }

    private void addClinicalOverviewTable(@NotNull Document document) {
        Table overviewTable = new Table(UnitValue.createPercentArray(new float[] { 1 })).setWidth(contentWidth() - 5);

        String questionnaireDate = Formats.date(record.clinical().patient().questionnaireDate());
        overviewTable.addCell(Cells.createTitleCell("Patient clinical history (" + questionnaireDate + ")"));

        float[] widths = new float[] { 170, overviewTable.getWidth().getValue() - 180 };
        Table clinicalHistoryTable = new Table(UnitValue.createPointArray(widths));
        clinicalHistoryTable.addCell(Cells.createKeyCell("Relevant treatment history"));
        clinicalHistoryTable.addCell(Cells.createValueCell(relevantPreTreatmentHistory(record.clinical())));
        clinicalHistoryTable.addCell(Cells.createKeyCell("Other oncological history"));
        clinicalHistoryTable.addCell(Cells.createValueCell(otherOncologicalHistory(record.clinical())));
        clinicalHistoryTable.addCell(Cells.createKeyCell("Relevant non-oncological history"));
        clinicalHistoryTable.addCell(Cells.createValueCell(relevantNonOncologicalHistory(record.clinical())));

        overviewTable.addCell(Cells.createCell(clinicalHistoryTable));
        overviewTable.addCell(Cells.createEmptyCell());
        overviewTable.addCell(Cells.createTitleCell("Patient current details (" + questionnaireDate + ")"));

        Table currentDetailsTable = new Table(UnitValue.createPointArray(widths));
        currentDetailsTable.addCell(Cells.createKeyCell("WHO status"));
        currentDetailsTable.addCell(Cells.createValueCell(String.valueOf(record.clinical().clinicalStatus().who())));
        currentDetailsTable.addCell(Cells.createKeyCell("Unresolved toxicities grade => 2"));
        currentDetailsTable.addCell(Cells.createValueCell("TODO"));
        currentDetailsTable.addCell(Cells.createKeyCell("Significant infection"));
        currentDetailsTable.addCell(Cells.createValueCell(String.valueOf(record.clinical().clinicalStatus().hasActiveInfection())));
        currentDetailsTable.addCell(Cells.createKeyCell("Significant aberration on latest ECG"));
        currentDetailsTable.addCell(Cells.createValueCell(record.clinical().clinicalStatus().ecgAberrationDescription()));
        currentDetailsTable.addCell(Cells.createKeyCell("Cancer-related complications"));
        currentDetailsTable.addCell(Cells.createValueCell(cancerRelatedComplications(record.clinical())));
        overviewTable.addCell(Cells.createCell(currentDetailsTable));

        document.add(overviewTable);
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
    private String otherOncologicalHistory(@NotNull ClinicalRecord record) {
        StringJoiner joiner = new StringJoiner(", ");
        for (PriorTumorTreatment priorTumorTreatment : record.priorTumorTreatments()) {
            if (!priorTumorTreatment.isSystemic()) {
                joiner.add(priorTumorTreatment.name());
            }
        }

        for (PriorSecondPrimary priorSecondPrimary : record.priorSecondPrimaries()) {
            joiner.add("Second primary: " + priorSecondPrimary.tumorLocation());
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
