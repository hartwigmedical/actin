package com.hartwig.actin.report.pdf.chapters;

import com.hartwig.actin.report.datamodel.Report;
import com.hartwig.actin.report.pdf.util.Cells;
import com.hartwig.actin.report.pdf.util.Styles;
import com.hartwig.actin.report.pdf.util.Tables;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;

import org.jetbrains.annotations.NotNull;

public class TreatmentChapter implements ReportChapter {

    @NotNull
    private final Report report;

    public TreatmentChapter(@NotNull final Report report) {
        this.report = report;
    }

    @NotNull
    @Override
    public String name() {
        return "Treatment Options";
    }

    @NotNull
    @Override
    public PageSize pageSize() {
        return PageSize.A4;
    }

    @Override
    public void render(@NotNull final Document document) {
        addChapterTitle(document);
        addTreatmentSummaryTable(document);
        addTreatmentDetailsTable(document);
    }

    private void addChapterTitle(@NotNull Document document) {
        document.add(new Paragraph(name()).addStyle(Styles.chapterTitleStyle()));
    }

    private void addTreatmentSummaryTable(@NotNull Document document) {
        Table table = Tables.createFixedWidthCols(new float[] { 1, 1, 1, 1, 1 }).setWidth(contentWidth());

        table.addCell(Cells.createHeader("# Trials evaluated"));
        table.addCell(Cells.createHeader("# Eligible trials"));
        table.addCell(Cells.createHeader("# Cohorts evaluated"));
        table.addCell(Cells.createHeader("# Eligible cohorts"));
        table.addCell(Cells.createHeader("# Eligible open cohorts"));

        table.addCell(Cells.createContent(""));
        table.addCell(Cells.createContent(""));
        table.addCell(Cells.createContent(""));
        table.addCell(Cells.createContent(""));
        table.addCell(Cells.createContent(""));

        document.add(Tables.addTitle(table, "Treatment option summary"));
    }

    private void addTreatmentDetailsTable(@NotNull Document document) {

    }
}
