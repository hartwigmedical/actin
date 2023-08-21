package com.hartwig.actin.report.pdf.util;

import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.UnitValue;

import org.jetbrains.annotations.NotNull;

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
        return makeWrapping(table, true);
    }

    @NotNull
    public static Table makeWrapping(@NotNull Table table, boolean printSubNotes) {
        if (table.getNumberOfRows() == 0) {
            table.addCell(Cells.createSpanningNoneEntry(table));
        }

        table.addFooterCell(Cells.createSpanningSubNote(printSubNotes ? "The table continues on the next page" : "", table));
        table.setSkipLastFooter(true);

        Table wrappingTable = new Table(1).setMinWidth(table.getWidth());
        if (printSubNotes) {
            wrappingTable.addHeaderCell(Cells.createSubNote("Continued from the previous page"));
        }
        wrappingTable.setSkipFirstHeader(true).addCell(Cells.create(table).setPadding(0));

        return wrappingTable;
    }
}
