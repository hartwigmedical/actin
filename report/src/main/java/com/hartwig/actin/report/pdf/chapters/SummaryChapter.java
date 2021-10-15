package com.hartwig.actin.report.pdf.chapters;

import com.hartwig.actin.datamodel.ActinRecord;
import com.hartwig.actin.report.pdf.util.Cells;
import com.hartwig.actin.report.pdf.util.Styles;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
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
        Table topTable = new Table(UnitValue.createPercentArray(new float[] { 1 })).setWidth(contentWidth() - 5);

        topTable.addCell(Cells.createTitleCell("Patient clinical history"));
        Table table = new Table(UnitValue.createPercentArray(new float[] { 1, 1 })).setBorder(Border.NO_BORDER);
        table.addCell(Cells.createKeyCell("Relevant treatment history"));
        table.addCell(Cells.createValueCell("Cisplatin"));
        table.addCell(Cells.createKeyCell("Other oncological history"));
        table.addCell(Cells.createValueCell("Cisplatin"));
        table.addCell(Cells.createKeyCell("Relevant non-oncological history"));
        table.addCell(Cells.createValueCell("Cisplatin"));

        topTable.addCell(Cells.createCell(table));
        document.add(topTable);
    }
}
