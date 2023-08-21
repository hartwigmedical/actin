package com.hartwig.actin.report.pdf.tables;

import com.itextpdf.layout.element.Table;

import org.jetbrains.annotations.NotNull;

public interface TableGenerator {

    @NotNull
    String title();

    @NotNull
    Table contents();
}
