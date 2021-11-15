package com.hartwig.actin.report.pdf.chapters;

import com.hartwig.actin.report.pdf.util.Styles;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;

import org.jetbrains.annotations.NotNull;

public class TrialMatchingDetailsChapter implements ReportChapter {

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
    public void render(@NotNull final Document document) {
        addChapterTitle(document);

        document.add(new Paragraph("Coming soon").addStyle(Styles.highlightStyle()));
    }

    private void addChapterTitle(@NotNull Document document) {
        document.add(new Paragraph(name()).addStyle(Styles.chapterTitleStyle()));
    }
}
