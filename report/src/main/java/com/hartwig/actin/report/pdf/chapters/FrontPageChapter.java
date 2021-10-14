package com.hartwig.actin.report.pdf.chapters;

import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;

import org.jetbrains.annotations.NotNull;

public class FrontPageChapter implements ReportChapter {

    @NotNull
    @Override
    public String name() {
        return "Front Page";
    }

    @NotNull
    @Override
    public PageSize pageSize() {
        return PageSize.A4;
    }

    @Override
    public void render(@NotNull final Document document) {
        document.add(new Paragraph("hello world"));
    }
}
