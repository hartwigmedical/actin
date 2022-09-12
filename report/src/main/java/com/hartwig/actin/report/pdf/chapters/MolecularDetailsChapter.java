package com.hartwig.actin.report.pdf.chapters;

import java.util.List;

import com.google.common.collect.Lists;
import com.hartwig.actin.report.datamodel.Report;
import com.hartwig.actin.report.pdf.tables.TableGenerator;
import com.hartwig.actin.report.pdf.tables.molecular.MolecularCharacteristicsGenerator;
import com.hartwig.actin.report.pdf.tables.molecular.MolecularDriversGenerator;
import com.hartwig.actin.report.pdf.util.Cells;
import com.hartwig.actin.report.pdf.util.Styles;
import com.hartwig.actin.report.pdf.util.Tables;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;

import org.jetbrains.annotations.NotNull;

public class MolecularDetailsChapter implements ReportChapter {

    @NotNull
    private final Report report;

    public MolecularDetailsChapter(@NotNull final Report report) {
        this.report = report;
    }

    @NotNull
    @Override
    public String name() {
        return "Molecular Details (" + report.molecular().type() + " performed on " + report.molecular().sampleId() + ")";
    }

    @NotNull
    @Override
    public PageSize pageSize() {
        return PageSize.A4.rotate();
    }

    @Override
    public void render(@NotNull final Document document) {
        addChapterTitle(document);
        addMolecularDetails(document);
    }

    private void addChapterTitle(@NotNull Document document) {
        document.add(new Paragraph(name()).addStyle(Styles.chapterTitleStyle()));
    }

    private void addMolecularDetails(@NotNull Document document) {
        Table table = Tables.createSingleColWithWidth(contentWidth());

        List<TableGenerator> generators = Lists.newArrayList(new MolecularCharacteristicsGenerator(report.molecular(), contentWidth()),
                new MolecularDriversGenerator(report.molecular(), contentWidth()));

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
