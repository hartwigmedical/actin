package com.hartwig.actin.report.pdf.util;

import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.UnitValue;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class Tables {

    private Tables() {
    }

    @NotNull
    public static Table createFixedWidthCols(@NotNull float... widths) {
        return new Table(UnitValue.createPointArray(widths));
    }

    @NotNull
    public static Table createSingleColWithWidth(float width) {
        return new Table(UnitValue.createPercentArray(new float[] { 1 })).setWidth(width);
    }

    @NotNull
    public static Table makeWrapping(@NotNull Table table) {
        return makeWrapping(table, null);
    }

    @NotNull
    public static Table makeWrapping(@NotNull Table table, @Nullable String title) {
        if (table.getNumberOfRows() == 0) {
            table.addCell(Cells.createSpanningNoneEntry(table));
        }

        table.addFooterCell(Cells.createSpanningSubNote("The table continues on the next page", table));
        table.setSkipLastFooter(true);

        Table wrappingTable = new Table(1).setMinWidth(table.getWidth())
                .addHeaderCell(Cells.createSubNote("Continued from the previous page"))
                .setSkipFirstHeader(true)
                .addCell(Cells.create(table));

        Table finalTable = new Table(1).setMinWidth(table.getWidth());
        if (title != null) {
            finalTable.addHeaderCell(Cells.createTitle(title));
        }
        finalTable.addCell(Cells.create(wrappingTable));

        return finalTable;
    }
}
