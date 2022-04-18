package com.hartwig.actin.report.pdf.chapters;

import com.hartwig.actin.report.datamodel.Report;
import com.hartwig.actin.report.pdf.util.Styles;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;

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
        return "Molecular Details";
    }

    @NotNull
    @Override
    public PageSize pageSize() {
        return PageSize.A4.rotate();
    }

    @Override
    public void render(@NotNull final Document document) {
        addChapterTitle(document);
    }

    private void addChapterTitle(@NotNull Document document) {
        document.add(new Paragraph(name()).addStyle(Styles.chapterTitleStyle()));
    }
}
