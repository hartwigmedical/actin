package com.hartwig.actin.report.pdf.util;

import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.property.UnitValue;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class Tables {

    private Tables() {
    }

    @NotNull
    public static Table createFixedWidthCols(@NotNull float[] widths) {
        return new Table(UnitValue.createPointArray(widths));
    }

    @NotNull
    public static Table createSingleColWithWidth(float width) {
        return new Table(UnitValue.createPercentArray(new float[] { 1 })).setWidth(width);
    }

    @NotNull
    public static Table addTitle(@NotNull Table contentTable, @Nullable String title) {
        Table table = new Table(1).setMinWidth(contentTable.getWidth());

        table.addCell(Cells.createTitle(title));
        table.addCell(Cells.create(contentTable));

        return table;
    }
}
