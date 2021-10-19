package com.hartwig.actin.report.pdf.chapters;

import com.hartwig.actin.report.pdf.util.Constants;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.layout.Document;

import org.jetbrains.annotations.NotNull;

public interface ReportChapter {

    @NotNull
    String name();

    @NotNull
    PageSize pageSize();

    default float contentWidth() {
        return pageSize().getWidth() - (5 + Constants.PAGE_MARGIN_LEFT + Constants.PAGE_MARGIN_RIGHT);
    }

    void render(@NotNull Document document);

}
