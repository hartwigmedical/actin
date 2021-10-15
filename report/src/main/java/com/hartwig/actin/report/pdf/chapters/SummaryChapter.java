package com.hartwig.actin.report.pdf.chapters;

import com.hartwig.actin.datamodel.ActinRecord;
import com.hartwig.actin.report.pdf.ReportResources;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Text;
import com.itextpdf.layout.property.TextAlignment;

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

        document.add(new Paragraph(name()).addStyle(ReportResources.chapterTitleStyle()));

    }

    private void addPatientDetails(@NotNull Document document) {
        Paragraph patientDetailsLine = new Paragraph();
        patientDetailsLine.add(new Text("Sample ID: ").addStyle(ReportResources.labelStyle()));
        patientDetailsLine.add(new Text(record.sampleId()).addStyle(ReportResources.valueStyle()));
        patientDetailsLine.add(new Text(" | Sex: ").addStyle(ReportResources.labelStyle()));
        patientDetailsLine.add(new Text(record.clinical().patient().sex().display()).addStyle(ReportResources.valueStyle()));
        patientDetailsLine.add(new Text(" | Birth year: ").addStyle(ReportResources.labelStyle()));
        patientDetailsLine.add(new Text(String.valueOf(record.clinical().patient().birthYear())).addStyle(ReportResources.valueStyle()));

        document.add(patientDetailsLine.setWidth(contentWidth()).setTextAlignment(TextAlignment.RIGHT));
    }

    private void addClinicalDates(@NotNull Document document) {
        Paragraph clinicalDatesLine = new Paragraph();
        clinicalDatesLine.add(new Text("Clinical data: ").addStyle(ReportResources.labelStyle()));
        clinicalDatesLine.add(new Text("???").addStyle(ReportResources.valueStyle()));
        clinicalDatesLine.add(new Text(" until ").addStyle(ReportResources.labelStyle()));
        clinicalDatesLine.add(new Text("???").addStyle(ReportResources.valueStyle()));

        document.add(clinicalDatesLine.setWidth(contentWidth()).setTextAlignment(TextAlignment.RIGHT));
    }
}
