package com.hartwig.actin.report.pdf.chapters;

import java.util.List;

import com.google.common.collect.Lists;
import com.hartwig.actin.report.datamodel.Report;
import com.hartwig.actin.report.pdf.tables.LaboratoryResultsGenerator;
import com.hartwig.actin.report.pdf.tables.PatientCurrentDetailsGenerator;
import com.hartwig.actin.report.pdf.tables.TableGenerator;
import com.hartwig.actin.report.pdf.util.Cells;
import com.hartwig.actin.report.pdf.util.Styles;
import com.hartwig.actin.report.pdf.util.Tables;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;

import org.jetbrains.annotations.NotNull;

public class ClinicalDetailsChapter implements ReportChapter {

    @NotNull
    private final Report report;

    public ClinicalDetailsChapter(@NotNull final Report report) {
        this.report = report;
    }

    @NotNull
    @Override
    public String name() {
        return "Clinical Details";
    }

    @NotNull
    @Override
    public PageSize pageSize() {
        return PageSize.A4;
    }

    @Override
    public void render(@NotNull final Document document) {
        addChapterTitle(document);
        addClinicalDetails(document);
    }

    private void addChapterTitle(@NotNull Document document) {
        document.add(new Paragraph(name()).addStyle(Styles.chapterTitleStyle()));
    }

    private void addClinicalDetails(final Document document) {
        Table table = Tables.createSingleColWithWidth(contentWidth());

        float keyWidth = 210;
        float valueWidth = contentWidth() - keyWidth - 10;
        List<TableGenerator> generators = Lists.newArrayList(new PatientCurrentDetailsGenerator(report.clinical(), keyWidth, valueWidth),
                LaboratoryResultsGenerator.fromRecord(report.clinical(), keyWidth, valueWidth));

        for (int i = 0; i < generators.size(); i++) {
            TableGenerator generator = generators.get(i);
            table.addCell(Cells.createTitle(generator.title()));
            table.addCell(Cells.create(generator.contents()));
            if (i < generators.size() - 1) {
                table.addCell(Cells.createEmpty());
            }
        }

        document.add(table);
    }
}
